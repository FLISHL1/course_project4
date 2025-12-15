package ru.flish1.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.flish1.entity.Part;
import ru.flish1.entity.Request;
import ru.flish1.entity.ReservePart;
import ru.flish1.repository.CustomerRepository;
import ru.flish1.repository.PartRepository;
import ru.flish1.repository.ReservePartRepository;
import ru.flish1.repository.ServiceRepository;
import ru.flish1.repository.UserRepository;
import ru.flish1.service.OrderService;
import ru.flish1.service.RequestService;
import ru.flish1.service.UserService;

import java.util.ArrayList;
import java.util.List;

/**
 * Контроллер для работы с заявками
 */
@Controller
@RequestMapping("/requests")
public class RequestController {

    private static final Logger log = LoggerFactory.getLogger(RequestController.class);

    private final RequestService requestService;
    private final CustomerRepository customerRepository;
    private final UserRepository userRepository;
    private final UserService userService;
    private final ReservePartRepository reservePartRepository;
    private final OrderService orderService;
    private final PartRepository partRepository;
    private final ServiceRepository serviceRepository;

    public RequestController(RequestService requestService, CustomerRepository customerRepository,
                             UserRepository userRepository, UserService userService,
                             ReservePartRepository reservePartRepository,
                             OrderService orderService, PartRepository partRepository,
                             ServiceRepository serviceRepository) {
        this.requestService = requestService;
        this.customerRepository = customerRepository;
        this.userRepository = userRepository;
        this.userService = userService;
        this.reservePartRepository = reservePartRepository;
        this.orderService = orderService;
        this.partRepository = partRepository;
        this.serviceRepository = serviceRepository;
    }


    /**
     * Создание новой заявки
     */
    @PostMapping("/create")
    public String createRequest(
            @RequestParam("customerId") String customerId,
            @RequestParam("address") String address,
            RedirectAttributes redirectAttributes
    ) {
        try {
            Request request = requestService.createRequest(customerId, address);
            redirectAttributes.addFlashAttribute("successMessage", "Заявка успешно создана (ID: " + request.getId() + ")");
            return "redirect:/requests/" + request.getId();
        } catch (Exception e) {
            log.error("Ошибка при создании заявки", e);
            redirectAttributes.addFlashAttribute("errorMessage", "Ошибка при создании заявки: " + e.getMessage());
            return "redirect:/requests/new";
        }
    }


    /**
     * Обновление заявки (только для админа)
     */
    @PostMapping("/{id}/update")
    @PreAuthorize("hasRole('ADMIN')")
    public String updateRequest(
            @PathVariable Integer id,
            @RequestParam("customerId") String customerId,
            @RequestParam("address") String address,
            @RequestParam("status") String status,
            @RequestParam(value = "engineerId", required = false) Integer engineerId,
            RedirectAttributes redirectAttributes
    ) {
        try {
            requestService.updateRequest(id, customerId, address, status, engineerId);
            redirectAttributes.addFlashAttribute("successMessage", "Заявка успешно обновлена");
            return "redirect:/requests/" + id;
        } catch (Exception e) {
            log.error("Ошибка при обновлении заявки", e);
            redirectAttributes.addFlashAttribute("errorMessage", "Ошибка при обновлении заявки: " + e.getMessage());
            return "redirect:/requests/" + id + "/edit";
        }
    }

    /**
     * Назначение заявки инженеру (new -> assigned)
     */
    @PostMapping("/{id}/assign")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public String assignRequest(
            @PathVariable Integer id,
            @RequestParam("engineerId") Integer engineerId,
            RedirectAttributes redirectAttributes
    ) {
        try {
            requestService.assignRequest(id, engineerId);
            redirectAttributes.addFlashAttribute("successMessage", "Заявка успешно назначена инженеру");
            return "redirect:/requests/" + id;
        } catch (Exception e) {
            log.error("Ошибка при назначении заявки", e);
            redirectAttributes.addFlashAttribute("errorMessage", "Ошибка при назначении заявки: " + e.getMessage());
            return "redirect:/requests/" + id;
        }
    }

    /**
     * Начало работы над заявкой (assigned -> in_progress)
     */
    @PostMapping("/{id}/start")
    @PreAuthorize("hasRole('ENGINEER')")
    public String startRequest(
            @PathVariable Integer id,
            RedirectAttributes redirectAttributes
    ) {
        try {
            // Переводим заявку в работу
            requestService.startRequest(id);
            redirectAttributes.addFlashAttribute("successMessage", "Заявка переведена в работу");
            return "redirect:/requests/" + id;
        } catch (Exception e) {
            log.error("Ошибка при начале работы над заявкой", e);
            redirectAttributes.addFlashAttribute("errorMessage", "Ошибка: " + e.getMessage());
            return "redirect:/requests/" + id;
        }
    }

    /**
     * Резервирование запчастей для заявки
     */
    @PostMapping("/{id}/reserve-parts")
    @PreAuthorize("hasRole('ENGINEER')")
    public String reserveParts(
            @PathVariable Integer id,
            @RequestParam(value = "partIds", required = false) List<Integer> partIds,
            @RequestParam(value = "quantities", required = false) List<Integer> quantities,
            RedirectAttributes redirectAttributes
    ) {
        try {
            Request request = requestService.getRequestById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Заявка не найдена: " + id));

            if (!"assigned".equals(request.getStatus()) && !"in_progress".equals(request.getStatus())) {
                redirectAttributes.addFlashAttribute("errorMessage", "Заявка должна быть в статусе 'Назначена' или 'В работе'");
                return "redirect:/requests/" + id;
            }

            // Создаем резервирования запчастей, если они указаны
            if (partIds != null && quantities != null) {
                for (int i = 0; i < partIds.size(); i++) {
                    Integer partId = partIds.get(i);
                    Integer quantity = i < quantities.size() ? quantities.get(i) : 0;

                    if (partId != null && quantity != null && quantity > 0) {
                        Part part = partRepository.findById(partId)
                                .orElseThrow(() -> new IllegalArgumentException("Запчасть не найдена: " + partId));

                        ReservePart reservePart = new ReservePart();
                        reservePart.setRequest(request);
                        reservePart.setPart(part);
                        reservePart.setQuantity(quantity);
                        reservePart.setUsedQuantity(0);
                        reservePart.setStatus("active");
                        reservePartRepository.save(reservePart);
                    }
                }
                redirectAttributes.addFlashAttribute("successMessage", "Запчасти успешно зарезервированы");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Необходимо указать хотя бы одну запчасть");
            }

            return "redirect:/requests/" + id;
        } catch (Exception e) {
            log.error("Ошибка при резервировании запчастей", e);
            redirectAttributes.addFlashAttribute("errorMessage", "Ошибка: " + e.getMessage());
            return "redirect:/requests/" + id;
        }
    }


    /**
     * Завершение заявки с отправкой заказа в 1С (in_progress -> completed)
     */
    @PostMapping("/{id}/complete")
    @PreAuthorize("hasRole('ENGINEER')")
    public String completeRequest(
            @PathVariable Integer id,
            @RequestParam("sourceOrderId") String sourceOrderId,
            @RequestParam("paymentMethod") String paymentMethod,
            @RequestParam(value = "serviceIds", required = false) List<Integer> serviceIds,
            @RequestParam(value = "serviceQuantities", required = false) List<Double> serviceQuantities,
            @RequestParam(value = "reservePartIds", required = false) List<Long> reservePartIds,
            @RequestParam(value = "usedQuantities", required = false) List<Integer> usedQuantities,
            @RequestParam(value = "newPartIds", required = false) List<Integer> newPartIds,
            @RequestParam(value = "newPartQuantities", required = false) List<Integer> newPartQuantities,
            @RequestParam(value = "removeReservePartIds", required = false) List<Long> removeReservePartIds,
            RedirectAttributes redirectAttributes
    ) {
        try {
            Request request = requestService.getRequestById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Заявка не найдена: " + id));

            // Обновляем использованные количества для существующих резервирований
            if (reservePartIds != null && usedQuantities != null) {
                for (int i = 0; i < reservePartIds.size(); i++) {
                    Long reservePartId = reservePartIds.get(i);
                    Integer usedQuantity = i < usedQuantities.size() ? usedQuantities.get(i) : 0;

                    if (reservePartId != null) {
                        ReservePart reservePart = reservePartRepository.findById(reservePartId)
                                .orElse(null);
                        if (reservePart != null) {
                            reservePart.setUsedQuantity(usedQuantity != null ? usedQuantity : 0);
                            reservePartRepository.save(reservePart);
                        }
                    }
                }
            }

            // Удаляем резервирования, которые нужно удалить
            if (removeReservePartIds != null) {
                for (Long reservePartId : removeReservePartIds) {
                    if (reservePartId != null) {
                        reservePartRepository.deleteById(reservePartId);
                    }
                }
            }

            // Добавляем новые резервирования
            if (newPartIds != null && newPartQuantities != null) {
                for (int i = 0; i < newPartIds.size(); i++) {
                    Integer partId = newPartIds.get(i);
                    Integer quantity = i < newPartQuantities.size() ? newPartQuantities.get(i) : 0;

                    if (partId != null && quantity != null && quantity > 0) {
                        Part part = partRepository.findById(partId)
                                .orElseThrow(() -> new IllegalArgumentException("Запчасть не найдена: " + partId));

                        ReservePart reservePart = new ReservePart();
                        reservePart.setRequest(request);
                        reservePart.setPart(part);
                        reservePart.setQuantity(quantity);
                        reservePart.setUsedQuantity(quantity); // Новые резервирования сразу используются
                        reservePart.setStatus("active");
                        reservePartRepository.save(reservePart);
                    }
                }
            }

            // Сначала переводим заявку в статус completed
            requestService.completeRequestStatus(id);

            // Затем отправляем заказ в 1С (включая использованные запчасти из резервирований)
            // Материалы берутся из зарезервированных запчастей (использованные)
            OrderService.OrderResult result = orderService.createAndSendOrderFromRequest(
                    id,
                    sourceOrderId,
                    serviceIds != null ? serviceIds : new ArrayList<>(),
                    serviceQuantities != null ? serviceQuantities : new ArrayList<>(),
                    new ArrayList<>(), // Материалы не передаются отдельно - они берутся из резервирований
                    new ArrayList<>(),
                    paymentMethod
            );

            if (result.isSuccess()) {
                redirectAttributes.addFlashAttribute("successMessage",
                        "Заявка завершена. Заказ успешно отправлен в 1С. Документ: " + result.getDocument1cNumber());
            } else {
                redirectAttributes.addFlashAttribute("errorMessage",
                        "Заявка завершена, но ошибка при отправке заказа в 1С: " + result.getErrorMessage());
            }

            return "redirect:/requests/" + id;
        } catch (Exception e) {
            log.error("Ошибка при завершении заявки", e);
            redirectAttributes.addFlashAttribute("errorMessage", "Ошибка при завершении заявки: " + e.getMessage());
            return "redirect:/requests/" + id;
        }
    }

    /**
     * Отмена заявки
     */
    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public String cancelRequest(
            @PathVariable Integer id,
            RedirectAttributes redirectAttributes
    ) {
        try {
            requestService.cancelRequest(id);
            redirectAttributes.addFlashAttribute("successMessage", "Заявка отменена");
            return "redirect:/requests/" + id;
        } catch (Exception e) {
            log.error("Ошибка при отмене заявки", e);
            redirectAttributes.addFlashAttribute("errorMessage", "Ошибка при отмене заявки: " + e.getMessage());
            return "redirect:/requests/" + id;
        }
    }


    /**
     * Создание и отправка заказа в 1C из завершенной заявки
     */
    @PostMapping("/{requestId}/order/create")
    public String createOrderFromRequest(
            @PathVariable Integer requestId,
            @RequestParam("sourceOrderId") String sourceOrderId,
            @RequestParam("paymentMethod") String paymentMethod,
            @RequestParam(value = "serviceIds", required = false) List<Integer> serviceIds,
            @RequestParam(value = "serviceQuantities", required = false) List<Double> serviceQuantities,
            Model model
    ) {
        try {
            // Создаем и отправляем заказ из заявки (цены берутся из услуг и запчастей в БД)
            // Заказ не сохраняется в БД, только отправляется в 1C
            // Материалы берутся из зарезервированных запчастей (использованные)
            OrderService.OrderResult result = orderService.createAndSendOrderFromRequest(
                    requestId,
                    sourceOrderId,
                    serviceIds != null ? serviceIds : new ArrayList<>(),
                    serviceQuantities != null ? serviceQuantities : new ArrayList<>(),
                    new ArrayList<>(), // Материалы не передаются отдельно - они берутся из резервирований
                    new ArrayList<>(),
                    paymentMethod
            );

            if (result.isSuccess()) {
                model.addAttribute("success", true);
                String docNumber = result.getDocument1cNumber() != null ? result.getDocument1cNumber() : "не указан";
                model.addAttribute("message", "Документ '" + docNumber + "' успешно создан и проведен в 1C.");
                model.addAttribute("documentNumber", result.getDocument1cNumber());
                model.addAttribute("documentId", result.getDocument1cId());
                model.addAttribute("requestId", requestId);
            } else {
                model.addAttribute("success", false);
                model.addAttribute("errorMessage", result.getErrorMessage() != null ? result.getErrorMessage() : "Неизвестная ошибка");
            }
        } catch (Exception e) {
            log.error("Ошибка при создании заказа", e);
            model.addAttribute("success", false);
            model.addAttribute("errorMessage", "Ошибка при создании заказа: " + e.getMessage());
        }
        return "order-result";
    }


}

