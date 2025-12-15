package ru.flish1.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.flish1.entity.User;
import ru.flish1.entity.UserRole;
import ru.flish1.repository.UserRepository;
import ru.flish1.repository.UserRoleRepository;

import java.util.List;
import java.util.Optional;

/**
 * Сервис для работы с пользователями
 */
@Service
public class UserService implements UserDetailsService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, UserRoleRepository userRoleRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.userRoleRepository = userRoleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByLogin(username)
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден: " + username));

        // Роль уже загружена через JOIN FETCH в репозитории
        String roleName = user.getRole().getName().toUpperCase();

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getLogin())
                .password(user.getPasswordHash())
                .roles(roleName)
                .build();
    }

    /**
     * Создает пользователя
     */
    @Transactional
    public User createUser(String login, String password, String fullName, String roleName) {
        UserRole role = userRoleRepository.findByName(roleName)
                .orElseThrow(() -> new IllegalArgumentException("Роль не найдена: " + roleName));

        User user = new User();
        user.setLogin(login);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setFullName(fullName);
        user.setRole(role);

        return userRepository.save(user);
    }

    public List<User> findAllByRole(String roleName) {
        return userRepository.findByRoleName(roleName);
    }

    /**
     * Получает пользователя по логину
     */
    public Optional<User> findByLogin(String login) {
        return userRepository.findByLogin(login);
    }
}
