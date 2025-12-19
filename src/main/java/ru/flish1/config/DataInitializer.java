package ru.flish1.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import ru.flish1.entity.EquipmentType;
import ru.flish1.entity.UserRole;
import ru.flish1.repository.EquipmentTypeRepository;
import ru.flish1.repository.UserRoleRepository;
import ru.flish1.service.UserService;

/**
 * Инициализация начальных данных (роли, пользователи, типы оборудования)
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRoleRepository userRoleRepository;
    private final UserService userService;
    private final EquipmentTypeRepository equipmentTypeRepository;

    public DataInitializer(UserRoleRepository userRoleRepository, UserService userService,
                           EquipmentTypeRepository equipmentTypeRepository) {
        this.userRoleRepository = userRoleRepository;
        this.userService = userService;
        this.equipmentTypeRepository = equipmentTypeRepository;
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

        // Создаем типы оборудования, если их нет
        createEquipmentTypeIfNotExists("Компьютер", false);
        createEquipmentTypeIfNotExists("Ноутбук", false);
        createEquipmentTypeIfNotExists("Принтер", false);
        createEquipmentTypeIfNotExists("МФУ", false);
        createEquipmentTypeIfNotExists("Сервер", false);
        createEquipmentTypeIfNotExists("Сетевое оборудование", false);
        createEquipmentTypeIfNotExists("Монитор", false);
        createEquipmentTypeIfNotExists("Телефон/Смартфон", false);
        createEquipmentTypeIfNotExists("Планшет", false);
        createEquipmentTypeIfNotExists("Другое", true); // Тип "Другое" для пользовательского ввода
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

    private void createEquipmentTypeIfNotExists(String name, boolean isOther) {
        if (equipmentTypeRepository.findByName(name).isEmpty()) {
            EquipmentType equipmentType = new EquipmentType();
            equipmentType.setName(name);
            equipmentType.setIsOther(isOther);
            equipmentTypeRepository.save(equipmentType);
            log.info("Создан тип оборудования: {} (isOther={})", name, isOther);
        }
    }
}
