package ru.flish1.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.flish1.entity.User;

import java.util.List;
import java.util.Optional;

/**
 * Репозиторий для работы с пользователями
 */
@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.role WHERE u.login = :login")
    Optional<User> findByLogin(@Param("login") String login);
    
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.role WHERE u.role.name = :roleName")
    List<User> findByRoleName(@Param("roleName") String roleName);
}

