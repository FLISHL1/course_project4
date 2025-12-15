package ru.flish1.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import ru.flish1.entity.UserRole;
import ru.flish1.repository.UserRoleRepository;
import ru.flish1.service.UserService;

/**
 * Инициализация начальных данных (роли и пользователи)
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRoleRepository userRoleRepository;
    private final UserService userService;

    public DataInitializer(UserRoleRepository userRoleRepository, UserService userService) {
        this.userRoleRepository = userRoleRepository;
        this.userService = userService;
    }

    @Override
    public void run(String... args) {
        // Создаем роли, если их нет
        createRoleIfNotExists("ADMIN", "Администратор");
        createRoleIfNotExists("MANAGER", "Менеджер");
        createRoleIfNotExists("ENGINEER", "Инженер");

        // Создаем тестового администратора, если его нет
        createUserIfNotExists("admin", "admin", "Администратор", "ADMIN");
        createUserIfNotExists("engineer1", "engineer1", "Иванов Иван Иванович", "ENGINEER");
    }

    private void createUserIfNotExists(
            String login,
            String password,
            String fullName,
            String role
    ) {
        if (userService.findByLogin(login).isEmpty()) {
            userService.createUser(login, password, fullName, role);
            log.info("Создан пользователь {} с паролем {}", login, password);
        }
    }

    private void createRoleIfNotExists(String name, String displayName) {
        if (userRoleRepository.findByName(name).isEmpty()) {
            UserRole role = new UserRole();
            role.setName(name);
            userRoleRepository.save(role);
            log.info("Создана роль: {}", name);
        }
    }
}
