package ru.flish1.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.flish1.entity.ServiceEntity;

import java.util.Optional;

/**
 * Репозиторий для работы с услугами
 */
@Repository
public interface ServiceRepository extends JpaRepository<ServiceEntity, Integer> {
    Optional<ServiceEntity> findBySku(String sku);

    Optional<ServiceEntity> findByNomenclatureId(String nomenclatureId);
}


