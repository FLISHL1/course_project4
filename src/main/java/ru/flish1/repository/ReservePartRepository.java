package ru.flish1.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.flish1.entity.ReservePart;

import java.util.List;

/**
 * Репозиторий для работы с зарезервированными запчастями
 */
@Repository
public interface ReservePartRepository extends JpaRepository<ReservePart, Long> {
    List<ReservePart> findByRequestId(Integer requestId);

    List<ReservePart> findByRequestIdAndStatus(Integer requestId, String status);
}

