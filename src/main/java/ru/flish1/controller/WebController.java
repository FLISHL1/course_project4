package ru.flish1.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.flish1.entity.Customer;
import ru.flish1.entity.EquipmentType;
import ru.flish1.entity.Part;
import ru.flish1.entity.PartType;
import ru.flish1.entity.Request;
import ru.flish1.entity.ReservePart;
import ru.flish1.entity.ServiceEntity;
import ru.flish1.entity.User;
import ru.flish1.repository.CustomerRepository;
import ru.flish1.repository.EquipmentTypeRepository;
import ru.flish1.repository.PartRepository;
import ru.flish1.repository.PartTypeRepository;
import ru.flish1.repository.ReservePartRepository;
import ru.flish1.repository.ServiceRepository;
import ru.flish1.service.RequestService;
import ru.flish1.service.SyncService;
import ru.flish1.service.UserService;

import java.util.List;
import java.util.Optional;

/**
 * Контроллер для обработки веб-запросов
 */
@Controller
public class WebController {

    private static final Logger log = LoggerFactory.getLogger(WebController.class);

    private final CustomerRepository customerRepository;
    private final SyncService syncService;
    private final RequestService requestService;
    private final UserService userService;
    private final PartRepository partRepository;
    private final PartTypeRepository partTypeRepository;
    private final ReservePartRepository reservePartRepository;
    private final ServiceRepository serviceRepository;
    private final EquipmentTypeRepository equipmentTypeRepository;

    public WebController(CustomerRepository customerRepository,
                         SyncService syncService, RequestService requestService, UserService userService,
                         PartRepository partRepository, PartTypeRepository partTypeRepository,
                         ReservePartRepository reservePartRepository, ServiceRepository serviceRepository,
                         EquipmentTypeRepository equipmentTypeRepository) {
        this.customerRepository = customerRepository;
        this.syncService = syncService;
        this.requestService = requestService;
        this.userService = userService;
        this.partRepository = partRepository;
        this.partTypeRepository = partTypeRepository;
        this.reservePartRepository = reservePartRepository;
        this.serviceRepository = serviceRepository;
        this.equipmentTypeRepository = equipmentTypeRepository;
    }

    /**
     * Вспомогательный метод для поиска клиента по customerId
     * customerId может быть как phoneNumber, так и ID клиента (строка)
     */
    private Optional<Customer> findCustomerByIdOrPhone(String customerId) {
        if (customerId == null) {
            return Optional.empty();
        }

        // Сначала пытаемся найти по номеру телефона
        Optional<Customer> customer = customerRepository.findByPhoneNumber(customerId);
        if (customer.isPresent()) {
            return customer;
        }

        // Если не найдено по телефону, пытаемся найти по ID
        try {
            Integer customerIdInt = Integer.parseInt(customerId);
            return customerRepository.findById(customerIdInt);
        } catch (NumberFormatException e) {
            // customerId не является числом, возвращаем пустой Optional
            return Optional.empty();
        }
    }

    /**
     * Главная страница
     */
    @GetMapping("/")
    public String index() {
        return "index";
    }

    /**
     * Страница со списком номенклатуры (услуг и запчастей)
     */
    @GetMapping("/nomenclature")
    public String nomenclature(Model model) {
        List<ServiceEntity> servicesList = serviceRepository.findAll();
        List<Part> partsList = partRepository.findAll();
        model.addAttribute("servicesList", servicesList);
        model.addAttribute("partsList", partsList);
        return "nomenclature";
    }

    /**
     * Синхронизация номенклатуры из 1C
     */
    @PostMapping("/nomenclature/sync")
    public String syncNomenclature(RedirectAttributes redirectAttributes) {
        try {
            syncService.syncNomenclature();
            redirectAttributes.addFlashAttribute("successMessage", "Номенклатура успешно синхронизирована из 1C");
        } catch (Exception e) {
            log.error("Ошибка при синхронизации номенклатуры", e);
            redirectAttributes.addFlashAttribute("errorMessage", "Ошибка при синхронизации: " + e.getMessage());
        }
        return "redirect:/nomenclature";
    }

    /**
     * Страница со списком контрагентов
     */
    @GetMapping("/customers")
    public String customers(Model model) {
        List<Customer> customerList = customerRepository.findAll();
        model.addAttribute("customerList", customerList);
        return "customers";
    }

    /**
     * GET запрос на создание клиента - редирект на страницу списка
     */
    @GetMapping("/customers/create")
    public String createCustomerGet() {
        return "redirect:/customers";
    }

    /**
     * Создание нового клиента
     */
    @PostMapping("/customers/create")
    public String createCustomer(
            @RequestParam("fullName") String fullName,
            @RequestParam("phoneNumber") String phoneNumber,
            @RequestParam(value = "redirectTo", required = false) String redirectTo,
            RedirectAttributes redirectAttributes
    ) {
        try {
            // Проверяем, не существует ли уже клиент с таким телефоном
            if (customerRepository.findByPhoneNumber(phoneNumber).isPresent()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Клиент с таким номером телефона уже существует");
                if (redirectTo != null && redirectTo.equals("/requests/new")) {
                    return "redirect:" + redirectTo;
                }
                return "redirect:/customers";
            }

            Customer customer = new Customer();
            customer.setFullName(fullName);
            customer.setPhoneNumber(phoneNumber);
            Customer savedCustomer = customerRepository.save(customer);

            redirectAttributes.addFlashAttribute("successMessage", "Клиент успешно создан");

            // Если редирект на форму создания заявки, передаем ID созданного клиента
            if (redirectTo != null && redirectTo.equals("/requests/new")) {
                redirectAttributes.addFlashAttribute("selectedCustomerId", savedCustomer.getId());
                return "redirect:" + redirectTo;
            }

            return "redirect:/customers";
        } catch (Exception e) {
            log.error("Ошибка при создании клиента", e);
            redirectAttributes.addFlashAttribute("errorMessage", "Ошибка при создании клиента: " + e.getMessage());
            if (redirectTo != null && redirectTo.equals("/requests/new")) {
                return "redirect:" + redirectTo;
            }
            return "redirect:/customers";
        }
    }

    /**
     * Список всех заявок
     * Заказ отправляется в 1C, но не хранится в БД
     */
    @GetMapping("/requests")
    public String listRequests(Model model, Authentication authentication) {
        List<Request> requests = requestService.getAllRequests();

        // Загружаем клиентов для каждой заявки
        // customerId может быть как phoneNumber, так и ID клиента (строка)
        java.util.Map<Integer, Customer> requestIdToCustomerMap = new java.util.HashMap<>();
        for (Request request : requests) {
            findCustomerByIdOrPhone(request.getCustomerId())
                    .ifPresent(customer -> requestIdToCustomerMap.put(request.getId(), customer));
        }

        model.addAttribute("requests", requests);
        model.addAttribute("requestIdToCustomerMap", requestIdToCustomerMap);
        return "requests/list";
    }

    /**
     * Форма создания новой заявки
     */
    @GetMapping("/requests/new")
    public String newRequestForm(Model model) {
        List<Customer> customers = customerRepository.findAll();
        List<EquipmentType> equipmentTypes = equipmentTypeRepository.findAll();
        model.addAttribute("customers", customers);
        model.addAttribute("equipmentTypes", equipmentTypes);
        model.addAttribute("request", new Request());
        return "requests/new";
    }

    /**
     * Просмотр заявки
     */
    @GetMapping("/requests/{id}")
    public String viewRequest(@PathVariable Integer id, Model model, Authentication authentication) {
        Optional<Request> requestOpt = requestService.getRequestById(id);
        if (requestOpt.isEmpty()) {
            return "redirect:/requests";
        }

        Request request = requestOpt.get();

        // Получаем клиента по customerId (может быть phoneNumber или ID)
        Customer customer = findCustomerByIdOrPhone(request.getCustomerId()).orElse(null);

        // Загружаем зарезервированные запчасти
        List<ReservePart> reserveParts = reservePartRepository.findByRequestId(request.getId());

        // Проверяем права доступа
        boolean canEdit = authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"));
        boolean canComplete = authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ENGINEER"));

        // Получаем текущего пользователя
        String currentUsername = authentication.getName();
        Optional<User> currentUserOpt = userService.findByLogin(currentUsername);
        Integer currentUserId = currentUserOpt.map(User::getId).orElse(null);
        // Получаем список инженеров для назначения
        List<User> engineers = userService.findAllByRole("ENGINEER");

        model.addAttribute("request", request);
        model.addAttribute("customer", customer);
        model.addAttribute("reserveParts", reserveParts);
        model.addAttribute("canEdit", canEdit);
        model.addAttribute("canComplete", canComplete);
        model.addAttribute("currentUserId", currentUserId);
        model.addAttribute("engineers", engineers);

        return "requests/view";
    }


    /**
     * Форма редактирования заявки (только для админа)
     */
    @GetMapping("/requests/{id}/edit")
    @PreAuthorize("hasRole('ADMIN')")
    public String editRequestForm(@PathVariable Integer id, Model model) {
        Optional<Request> requestOpt = requestService.getRequestById(id);
        if (requestOpt.isEmpty()) {
            return "redirect:/requests";
        }

        Request request = requestOpt.get();

        List<Customer> customers = customerRepository.findAll();
        List<User> engineers = userService.findAllByRole("ENGINEER");
        List<EquipmentType> equipmentTypes = equipmentTypeRepository.findAll();

        // Получаем текущего клиента заявки (может быть phoneNumber или ID)
        Customer currentCustomer = findCustomerByIdOrPhone(request.getCustomerId()).orElse(null);

        // Инициализируем тип оборудования, если он есть
        if (request.getEquipmentType() != null) {
            request.getEquipmentType().getName(); // Инициализируем прокси
        }

        model.addAttribute("request", request);
        model.addAttribute("customers", customers);
        model.addAttribute("engineers", engineers);
        model.addAttribute("equipmentTypes", equipmentTypes);
        model.addAttribute("currentCustomer", currentCustomer);

        return "requests/edit";
    }

    /**
     * Форма резервирования запчастей (для assigned и in_progress)
     */
    @GetMapping("/requests/{id}/reserve-parts")
    @PreAuthorize("hasRole('ENGINEER')")
    public String reservePartsForm(@PathVariable Integer id, Model model, RedirectAttributes redirectAttributes) {
        Optional<Request> requestOpt = requestService.getRequestById(id);
        if (requestOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Заявка не найдена");
            return "redirect:/requests";
        }

        Request request = requestOpt.get();

        if (!"assigned".equals(request.getStatus()) && !"in_progress".equals(request.getStatus())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Заявка должна быть в статусе 'Назначена' или 'В работе' для резервирования запчастей");
            return "redirect:/requests/" + id;
        }

        // Получаем список доступных запчастей
        List<Part> availableParts = partRepository.findAll();
        // Инициализируем PartType для каждой запчасти, чтобы избежать проблем с Hibernate прокси при сериализации
        for (Part part : availableParts) {
            if (part.getType() != null) {
                part.getType().getName(); // Инициализируем прокси
            }
        }

        // Получаем уже зарезервированные запчасти для этой заявки
        List<ReservePart> existingReserveParts = reservePartRepository.findByRequestIdAndStatus(id, "active");
        // Инициализируем PartType для зарезервированных запчастей
        for (ReservePart reservePart : existingReserveParts) {
            if (reservePart.getPart() != null && reservePart.getPart().getType() != null) {
                reservePart.getPart().getType().getName(); // Инициализируем прокси
            }
        }

        model.addAttribute("request", request);
        model.addAttribute("availableParts", availableParts);
        model.addAttribute("existingReserveParts", existingReserveParts);

        return "requests/reserve-parts-form";
    }

    /**
     * Форма завершения заявки с заполнением заказа (in_progress -> completed)
     */
    @GetMapping("/requests/{id}/complete-form")
    @PreAuthorize("hasRole('ENGINEER')")
    public String completeRequestForm(@PathVariable Integer id, Model model, RedirectAttributes redirectAttributes) {
        Optional<Request> requestOpt = requestService.getRequestById(id);
        if (requestOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Заявка не найдена");
            return "redirect:/requests";
        }

        Request request = requestOpt.get();

        if (!"in_progress".equals(request.getStatus())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Заявка должна быть в статусе 'В работе' для завершения");
            return "redirect:/requests/" + id;
        }

        // Получаем клиента (может быть phoneNumber или ID)
        Customer customer = findCustomerByIdOrPhone(request.getCustomerId()).orElse(null);

        // Получаем тип запчастей (материалов)
        PartType materialType = partTypeRepository.findAll().stream()
                .filter(t -> "material".equalsIgnoreCase(t.getName()))
                .findFirst()
                .orElse(null);

        // Получаем услуги из таблицы services
        List<ServiceEntity> services = serviceRepository.findAll();

        // Получаем зарезервированные запчасти (материалы)
        List<ReservePart> reserveParts = reservePartRepository.findByRequestIdAndStatus(id, "active");
        // Инициализируем PartType для зарезервированных запчастей
        for (ReservePart reservePart : reserveParts) {
            if (reservePart.getPart() != null && reservePart.getPart().getType() != null) {
                reservePart.getPart().getType().getName(); // Инициализируем прокси
            }
        }

        // Получаем список доступных запчастей для добавления новых (только материалы)
        List<Part> allParts = partRepository.findAll();
        // Инициализируем PartType для всех запчастей
        for (Part part : allParts) {
            if (part.getType() != null) {
                part.getType().getName(); // Инициализируем прокси
            }
        }

        List<Part> availableParts = allParts.stream()
                .filter(p -> materialType != null && p.getType() != null && materialType.getId().equals(p.getType().getId()))
                .toList();

        model.addAttribute("request", request);
        model.addAttribute("customer", customer);
        model.addAttribute("services", services);
        model.addAttribute("reserveParts", reserveParts);
        model.addAttribute("availableParts", availableParts);

        return "requests/complete-form";
    }

    /**
     * Форма создания заказа из завершенной заявки
     */
    @GetMapping("/requests/{requestId}/order/new")
    public String newOrderFromRequest(@PathVariable Integer requestId, Model model, RedirectAttributes redirectAttributes) {
        // Получаем заявку
        ru.flish1.entity.Request request = requestService.getRequestById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Заявка не найдена: " + requestId));

        if (!"completed".equals(request.getStatus())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Заявка должна быть завершена перед созданием заказа");
            return "redirect:/requests/" + requestId;
        }

        // Получаем услуги из таблицы services
        List<ServiceEntity> services = serviceRepository.findAll();

        // Получаем клиента по customerId (может быть phoneNumber или ID)
        Customer customer = findCustomerByIdOrPhone(request.getCustomerId()).orElse(null);

        model.addAttribute("request", request);
        model.addAttribute("customer", customer);
        model.addAttribute("services", services);
        return "order-form-from-request";
    }


}
