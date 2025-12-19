package ru.flish1.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.flish1.client.OneCApiClient;
import ru.flish1.dto.CompletedOrderItem;
import ru.flish1.dto.CompletedOrderPayload;
import ru.flish1.dto.SuccessResponse;
import ru.flish1.entity.Part;
import ru.flish1.entity.Request;
import ru.flish1.entity.ReservePart;
import ru.flish1.entity.ServiceEntity;
import ru.flish1.repository.CustomerRepository;
import ru.flish1.repository.PartRepository;
import ru.flish1.repository.RequestRepository;
import ru.flish1.repository.ReservePartRepository;
import ru.flish1.repository.ServiceRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Сервис для работы с заказами
 * Заказ отправляется в 1C, но не хранится в БД
 * Информация о заказе собирается автоматически из заявки
 */
@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    private final OneCApiClient oneCApiClient;
    private final RequestRepository requestRepository;
    private final CustomerRepository customerRepository;
    private final ReservePartRepository reservePartRepository;
    private final PartRepository partRepository;
    private final ServiceRepository serviceRepository;

    public OrderService(OneCApiClient oneCApiClient,
                        RequestRepository requestRepository,
                        CustomerRepository customerRepository,
                        ReservePartRepository reservePartRepository,
                        PartRepository partRepository,
                        ServiceRepository serviceRepository) {
        this.oneCApiClient = oneCApiClient;
        this.requestRepository = requestRepository;
        this.customerRepository = customerRepository;
        this.reservePartRepository = reservePartRepository;
        this.partRepository = partRepository;
        this.serviceRepository = serviceRepository;
    }

    /**
     * Создает и отправляет выполненный заказ в 1C из завершенной заявки
     * Цены берутся из номенклатуры в БД
     * Поддерживает множественный выбор услуг и материалов
     * Заказ не сохраняется в БД, только отправляется в 1C
     *
     * @param requestId          ID завершенной заявки
     * @param sourceOrderId      идентификатор заказа в системе-источнике
     * @param serviceIds         список идентификаторов услуг
     * @param serviceQuantities  список количеств услуг
     * @param materialPartIds    список идентификаторов запчастей-материалов
     * @param materialQuantities список количеств материалов
     * @param paymentMethod      метод оплаты (cash, card, transfer)
     * @return результат отправки заказа (SuccessResponse или null при ошибке)
     */
    @Transactional
    public OrderResult createAndSendOrderFromRequest(
            Integer requestId,
            String sourceOrderId,
            List<Integer> serviceIds,
            List<Double> serviceQuantities,
            List<Integer> materialPartIds,
            List<Double> materialQuantities,
            String paymentMethod
    ) {
        // Получаем заявку
        Request request = requestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Заявка не найдена: " + requestId));

        // Разрешаем создание заказа для заявок в статусе "in_progress" или "completed"
        if (!"in_progress".equals(request.getStatus()) && !"completed".equals(request.getStatus())) {
            throw new IllegalArgumentException("Заявка должна быть в статусе 'В работе' или 'Завершена' для создания заказа");
        }

        // Получаем клиента по customerId (phoneNumber)
        ru.flish1.entity.Customer customer = customerRepository.findByPhoneNumber(request.getCustomerId())
                .orElse(null);

        if (customer == null) {
            // Если клиент не найден, используем customerId как есть (phoneNumber)
            log.warn("Клиент не найден по phoneNumber: {}, используем customerId напрямую", request.getCustomerId());
        }

        String customerPhoneNumber = customer != null ? customer.getPhoneNumber() : request.getCustomerId();

        log.info("Создание заказа из заявки: requestId={}, sourceOrderId={}, клиент={}, услуг: {}, материалов: {}",
                requestId, sourceOrderId, customerPhoneNumber,
                serviceIds != null ? serviceIds.size() : 0,
                materialPartIds != null ? materialPartIds.size() : 0);

        LocalDateTime completionDateTime = LocalDateTime.now();
        // Формируем дату завершения в формате ISO 8601
        String completionDate = completionDateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

        // Создаем список услуг
        List<CompletedOrderItem> services = new ArrayList<>();
        if (serviceIds != null && serviceQuantities != null) {
            for (int i = 0; i < serviceIds.size(); i++) {
                Integer serviceId = serviceIds.get(i);
                double quantity = i < serviceQuantities.size() ? serviceQuantities.get(i) : 0;

                if (serviceId != null && quantity > 0) {
                    ServiceEntity serviceEntity = serviceRepository.findById(serviceId)
                            .orElseThrow(() -> new IllegalArgumentException("Услуга не найдена: " + serviceId));

                    if (serviceEntity.getPrice() == null || serviceEntity.getPrice() <= 0) {
                        throw new IllegalArgumentException("Цена для услуги '" + serviceEntity.getName() + "' не установлена в 1C");
                    }

                    if (serviceEntity.getNomenclatureId() == null || serviceEntity.getNomenclatureId().isEmpty()) {
                        throw new IllegalArgumentException("ID номенклатуры для услуги '" + serviceEntity.getName() + "' не установлен");
                    }

                    CompletedOrderItem service = new CompletedOrderItem();
                    service.setNomenclatureId(serviceEntity.getNomenclatureId());
                    service.setQuantity(quantity);
                    service.setPricePerUnit(serviceEntity.getPrice());
                    service.setTotalPrice(quantity * serviceEntity.getPrice());
                    services.add(service);
                }
            }
        }

        // Проверяем, что есть хотя бы одна услуга
        if (services.isEmpty()) {
            throw new IllegalArgumentException("Необходимо указать хотя бы одну услугу");
        }

        // Создаем список материалов
        List<CompletedOrderItem> materials = new ArrayList<>();
        if (materialPartIds != null && materialQuantities != null) {
            for (int i = 0; i < materialPartIds.size(); i++) {
                Integer materialPartId = materialPartIds.get(i);
                double quantity = i < materialQuantities.size() ? materialQuantities.get(i) : 0;

                if (materialPartId != null && quantity > 0) {
                    Part materialPart = partRepository.findById(materialPartId)
                            .orElseThrow(() -> new IllegalArgumentException("Запчасть-материал не найдена: " + materialPartId));

                    if (materialPart.getPrice() == null || materialPart.getPrice() <= 0) {
                        throw new IllegalArgumentException("Цена для материала '" + materialPart.getName() + "' не установлена в 1C");
                    }

                    if (materialPart.getNomenclatureId() == null || materialPart.getNomenclatureId().isEmpty()) {
                        throw new IllegalArgumentException("ID номенклатуры для материала '" + materialPart.getName() + "' не установлен");
                    }

                    CompletedOrderItem material = new CompletedOrderItem();
                    material.setNomenclatureId(materialPart.getNomenclatureId());
                    material.setQuantity(quantity);
                    material.setPricePerUnit(materialPart.getPrice());
                    material.setTotalPrice(quantity * materialPart.getPrice());
                    materials.add(material);
                }
            }
        }

        // Добавляем использованные запчасти из резервирований
        List<ReservePart> reserveParts = reservePartRepository.findByRequestIdAndStatus(requestId, "active");
        for (ReservePart reservePart : reserveParts) {
            if (reservePart.getUsedQuantity() != null && reservePart.getUsedQuantity() > 0) {
                Part part = reservePart.getPart();
                if (part != null) {
                    if (part.getPrice() == null || part.getPrice() <= 0) {
                        log.warn("Цена для запчасти '{}' не установлена в 1C, пропускаем", part.getName());
                        continue;
                    }

                    if (part.getNomenclatureId() == null || part.getNomenclatureId().isEmpty()) {
                        log.warn("ID номенклатуры для запчасти '{}' не установлен, пропускаем", part.getName());
                        continue;
                    }

                    CompletedOrderItem material = new CompletedOrderItem();
                    material.setNomenclatureId(part.getNomenclatureId());
                    material.setQuantity(reservePart.getUsedQuantity());
                    material.setPricePerUnit(part.getPrice());
                    material.setTotalPrice(reservePart.getUsedQuantity() * part.getPrice());
                    materials.add(material);

                    log.info("Добавлена использованная запчасть в заказ: {} (количество: {})",
                            part.getName(), reservePart.getUsedQuantity());
                }
            }
        }

        // Создаем payload для отправки в 1C
        // Для физ лиц используем phoneNumber как идентификатор (вместо ИНН)
        CompletedOrderPayload payload = new CompletedOrderPayload();
        payload.setSourceOrderId(sourceOrderId);
        payload.setCompletionDate(completionDate);
        payload.setCustomerTaxId(customerPhoneNumber); // Используем телефон как идентификатор для 1C
        payload.setServices(services);
        payload.setMaterials(materials);
        payload.setPaymentMethod(paymentMethod);
        payload.setIsPaid(false); // По умолчанию документ не оплачен, оплата подтверждается отдельно

        // Отправляем в 1C (не сохраняем в БД)
        try {
            SuccessResponse response = oneCApiClient.sendCompletedOrder(payload);

            if (response == null) {
                throw new RuntimeException("Получен пустой ответ от 1C");
            }

            log.info("Заказ успешно отправлен в 1C. Документ: {}, ID: {}",
                    response.getDocument1cNumber(), response.getDocument1cId());

            return new OrderResult(true, response.getDocument1cId(), response.getDocument1cNumber(), null);
        } catch (Exception e) {
            log.error("Ошибка при отправке заказа в 1C", e);
            return new OrderResult(false, null, null, "Ошибка: " + e.getMessage());
        }
    }

    /**
     * Результат отправки заказа в 1C
     */
    public static class OrderResult {
        private final boolean success;
        private final String document1cId;
        private final String document1cNumber;
        private final String errorMessage;

        public OrderResult(boolean success, String document1cId, String document1cNumber, String errorMessage) {
            this.success = success;
            this.document1cId = document1cId;
            this.document1cNumber = document1cNumber;
            this.errorMessage = errorMessage;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getDocument1cId() {
            return document1cId;
        }

        public String getDocument1cNumber() {
            return document1cNumber;
        }

        public String getErrorMessage() {
            return errorMessage;
        }
    }
}
