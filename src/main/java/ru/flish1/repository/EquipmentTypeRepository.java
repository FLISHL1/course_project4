package ru.flish1.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.flish1.entity.EquipmentType;

import java.util.Optional;

/**
 * Репозиторий для работы с типами оборудования
 */
@Repository
public interface EquipmentTypeRepository extends JpaRepository<EquipmentType, Integer> {
    
    /**
     * Находит тип оборудования "Другое"
     */
    Optional<EquipmentType> findByIsOtherTrue();
    
    /**
     * Находит тип оборудования по названию
     */
    Optional<EquipmentType> findByName(String name);
}
