package ru.flish1.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.flish1.entity.Part;

import java.util.List;
import java.util.Optional;

/**
 * Репозиторий для работы с запчастями
 */
@Repository
public interface PartRepository extends JpaRepository<Part, Integer> {
    Optional<Part> findBySku(String sku);

    Optional<Part> findByNomenclatureId(String nomenclatureId);

    List<Part> findByTypeId(Integer typeId);
}

