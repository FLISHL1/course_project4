package ru.flish1.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.flish1.entity.PartType;

/**
 * Репозиторий для работы с типами запчастей
 */
@Repository
public interface PartTypeRepository extends JpaRepository<PartType, Integer> {
}

