package ru.flish1.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.flish1.entity.UserRole;

import java.util.Optional;

/**
 * Репозиторий для работы с ролями пользователей
 */
@Repository
public interface UserRoleRepository extends JpaRepository<UserRole, Integer> {
    Optional<UserRole> findByName(String name);
}

