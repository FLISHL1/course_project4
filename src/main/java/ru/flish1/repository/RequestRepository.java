package ru.flish1.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.flish1.entity.Request;

import java.util.List;
import java.util.Optional;

/**
 * Репозиторий для работы с заявками
 */
@Repository
public interface RequestRepository extends JpaRepository<Request, Integer> {
    List<Request> findAllByOrderByCreatedAtDesc();

    List<Request> findByCustomerIdOrderByCreatedAtDesc(String customerId);

    List<Request> findByStatusOrderByCreatedAtDesc(String status);

    List<Request> findByEngineerIdOrderByCreatedAtDesc(Integer engineerId);

    @Query("SELECT r FROM Request r LEFT JOIN FETCH r.engineer WHERE r.id = :id")
    Optional<Request> findByIdWithEngineer(@Param("id") Integer id);

    @Query("SELECT r FROM Request r LEFT JOIN FETCH r.engineer ORDER BY r.createdAt DESC")
    List<Request> findAllWithEngineerOrderByCreatedAtDesc();
}
