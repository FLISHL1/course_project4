package ru.flish1.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.flish1.client.OneCApiClient;
import ru.flish1.dto.NomenclatureDto;
import ru.flish1.entity.Part;
import ru.flish1.entity.PartType;
import ru.flish1.repository.CustomerRepository;
import ru.flish1.repository.PartRepository;
import ru.flish1.repository.PartTypeRepository;
import ru.flish1.repository.ServiceRepository;

import java.util.List;

/**
 * Сервис для синхронизации данных с 1C
 */
@Service
public class SyncService {

    private static final Logger log = LoggerFactory.getLogger(SyncService.class);

    private final OneCApiClient oneCApiClient;
    private final PartRepository partRepository;
    private final PartTypeRepository partTypeRepository;
    private final CustomerRepository customerRepository;
    private final ServiceRepository serviceRepository;

    public SyncService(OneCApiClient oneCApiClient, PartRepository partRepository,
                       PartTypeRepository partTypeRepository, CustomerRepository customerRepository,
                       ServiceRepository serviceRepository) {
        this.oneCApiClient = oneCApiClient;
        this.partRepository = partRepository;
        this.partTypeRepository = partTypeRepository;
        this.customerRepository = customerRepository;
        this.serviceRepository = serviceRepository;
    }

    /**
     * Синхронизирует номенклатуру из 1C в локальную БД
     * Услуги сохраняются в таблицу services, запчасти (материалы) - в таблицу parts
     */
    @Transactional
    public void syncNomenclature() {
        log.info("Начало синхронизации номенклатуры из 1C");
        try {
            List<NomenclatureDto> nomenclatureList = oneCApiClient.getNomenclature();

            if (nomenclatureList == null || nomenclatureList.isEmpty()) {
                log.warn("Список номенклатуры из 1C пуст");
                return;
            }

            // Получаем или создаем тип запчастей (материалов)
            PartType materialType = partTypeRepository.findAll().stream()
                    .filter(t -> "material".equalsIgnoreCase(t.getName()) || "Материал".equalsIgnoreCase(t.getName()))
                    .findFirst()
                    .orElseGet(() -> {
                        PartType type = new PartType();
                        type.setName("material");
                        return partTypeRepository.save(type);
                    });

            int servicesProcessed = 0;
            int partsProcessed = 0;

            // Разделяем услуги и запчасти при парсинге
            for (NomenclatureDto dto : nomenclatureList) {
                boolean isService = "service".equalsIgnoreCase(dto.getType());

                if (isService) {
                    // Сохраняем в таблицу services
                    ru.flish1.entity.ServiceEntity service = serviceRepository.findAll().stream()
                            .filter(s -> dto.getId().equals(s.getNomenclatureId()) ||
                                    (dto.getArticle() != null && dto.getArticle().equals(s.getSku())))
                            .findFirst()
                            .orElse(new ru.flish1.entity.ServiceEntity());

                    service.setName(dto.getName());
                    service.setNomenclatureId(dto.getId());
                    service.setPrice(dto.getPrice());
                    service.setUnit(dto.getUnit());

                    serviceRepository.save(service);
                    servicesProcessed++;
                } else {
                    // Сохраняем в таблицу parts (материалы)
                    Part part = partRepository.findAll().stream()
                            .filter(p -> dto.getId().equals(p.getNomenclatureId()) ||
                                    (dto.getArticle() != null && dto.getArticle().equals(p.getSku())))
                            .findFirst()
                            .orElse(new Part());

                    part.setName(dto.getName());
                    part.setSku(dto.getArticle() != null ? dto.getArticle() : dto.getId());
                    part.setNomenclatureId(dto.getId());
                    part.setPrice(dto.getPrice());
                    part.setUnit(dto.getUnit());
                    part.setType(materialType);

                    // Если quantity не установлен, устанавливаем 0
                    if (part.getQuantity() == null) {
                        part.setQuantity(0);
                    }

                    partRepository.save(part);
                    partsProcessed++;
                }
            }

            log.info("Синхронизация номенклатуры завершена. Обработано услуг: {}, запчастей: {}",
                    servicesProcessed, partsProcessed);
        } catch (Exception e) {
            log.error("Ошибка при синхронизации номенклатуры", e);
            throw new RuntimeException("Не удалось синхронизировать номенклатуру: " + e.getMessage(), e);
        }
    }
}
