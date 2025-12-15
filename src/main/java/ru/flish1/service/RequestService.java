package ru.flish1.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.flish1.entity.Request;
import ru.flish1.entity.User;
import ru.flish1.repository.RequestRepository;
import ru.flish1.repository.UserRepository;

import java.util.List;
import java.util.Optional;

/**
 * Сервис для работы с заявками
 */
@Service
public class RequestService {

    private static final Logger log = LoggerFactory.getLogger(RequestService.class);

    private final RequestRepository requestRepository;
    private final UserRepository userRepository;

    public RequestService(RequestRepository requestRepository, UserRepository userRepository) {
        this.requestRepository = requestRepository;
        this.userRepository = userRepository;
    }

    /**
     * Получает все заявки, отсортированные по дате создания (новые сначала)
     */
    public List<Request> getAllRequests() {
        return requestRepository.findAllWithEngineerOrderByCreatedAtDesc();
    }

    /**
     * Получает заявку по ID
     */
    public Optional<Request> getRequestById(Integer id) {
        return requestRepository.findByIdWithEngineer(id);
    }

    /**
     * Создает новую заявку
     *
     * @param customerId ID клиента (строка, может быть phoneNumber или другой идентификатор)
     * @param address    адрес выполнения работ
     */
    @Transactional
    public Request createRequest(String customerId, String address) {
        Request request = new Request();
        request.setCustomerId(customerId);
        request.setAddress(address);
        request.setStatus("new");

        Request saved = requestRepository.save(request);
        log.info("Создана новая заявка: ID={}, клиент={}, адрес={}", saved.getId(), customerId, address);
        return saved;
    }

    /**
     * Обновляет заявку (только для админа)
     */
    @Transactional
    public Request updateRequest(Integer id, String customerId, String address, String status, Integer engineerId) {
        Request request = requestRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Заявка не найдена: " + id));

        request.setCustomerId(customerId);
        request.setAddress(address);
        request.setStatus(status);

        if (engineerId != null) {
            User engineer = userRepository.findById(engineerId)
                    .orElseThrow(() -> new IllegalArgumentException("Инженер не найден: " + engineerId));
            request.setEngineer(engineer);
        } else {
            request.setEngineer(null);
        }

        Request saved = requestRepository.save(request);
        log.info("Заявка обновлена: ID={}, статус={}", saved.getId(), status);
        return saved;
    }

    /**
     * Завершает заявку (для инженера)
     */
    @Transactional
    public Request completeRequest(Integer requestId, Integer engineerId) {
        Request request = requestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Заявка не найдена: " + requestId));

        User engineer = userRepository.findById(engineerId)
                .orElseThrow(() -> new IllegalArgumentException("Инженер не найден: " + engineerId));

        request.setEngineer(engineer);
        request.setStatus("completed");

        Request saved = requestRepository.save(request);
        log.info("Заявка завершена: ID={}, инженер={}", saved.getId(), engineer.getFullName());
        return saved;
    }

    /**
     * Назначает инженера на заявку
     */
    @Transactional
    public Request assignEngineer(Integer requestId, Integer engineerId) {
        Request request = requestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Заявка не найдена: " + requestId));

        User engineer = userRepository.findById(engineerId)
                .orElseThrow(() -> new IllegalArgumentException("Инженер не найден: " + engineerId));

        request.setEngineer(engineer);
        request.setStatus("assigned");

        Request saved = requestRepository.save(request);
        log.info("Инженер назначен на заявку: ID={}, инженер={}", saved.getId(), engineer.getFullName());
        return saved;
    }

    /**
     * Получает заявки по статусу
     */
    public List<Request> getRequestsByStatus(String status) {
        return requestRepository.findByStatusOrderByCreatedAtDesc(status);
    }

    /**
     * Получает заявки инженера
     */
    public List<Request> getRequestsByEngineer(Integer engineerId) {
        return requestRepository.findByEngineerIdOrderByCreatedAtDesc(engineerId);
    }

    /**
     * Переводит заявку в статус "assigned" (назначена)
     */
    @Transactional
    public Request assignRequest(Integer requestId, Integer engineerId) {
        Request request = requestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Заявка не найдена: " + requestId));

        if (!"new".equals(request.getStatus())) {
            throw new IllegalArgumentException("Заявка должна быть в статусе 'new' для назначения");
        }

        User engineer = userRepository.findById(engineerId)
                .orElseThrow(() -> new IllegalArgumentException("Инженер не найден: " + engineerId));

        request.setEngineer(engineer);
        request.setStatus("assigned");

        Request saved = requestRepository.save(request);
        log.info("Заявка назначена: ID={}, инженер={}", saved.getId(), engineer.getFullName());
        return saved;
    }

    /**
     * Переводит заявку в статус "in_progress" (в работе)
     */
    @Transactional
    public Request startRequest(Integer requestId) {
        Request request = requestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Заявка не найдена: " + requestId));

        if (!"assigned".equals(request.getStatus())) {
            throw new IllegalArgumentException("Заявка должна быть в статусе 'assigned' для начала работы");
        }

        if (request.getEngineer() == null) {
            throw new IllegalArgumentException("Инженер должен быть назначен перед началом работы");
        }

        request.setStatus("in_progress");

        Request saved = requestRepository.save(request);
        log.info("Заявка переведена в работу: ID={}", saved.getId());
        return saved;
    }

    /**
     * Переводит заявку в статус "completed" (завершена)
     */
    @Transactional
    public Request completeRequestStatus(Integer requestId) {
        Request request = requestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Заявка не найдена: " + requestId));

        if (!"in_progress".equals(request.getStatus())) {
            throw new IllegalArgumentException("Заявка должна быть в статусе 'in_progress' для завершения");
        }

        request.setStatus("completed");

        Request saved = requestRepository.save(request);
        log.info("Заявка завершена: ID={}", saved.getId());
        return saved;
    }

    /**
     * Отменяет заявку
     */
    @Transactional
    public Request cancelRequest(Integer requestId) {
        Request request = requestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Заявка не найдена: " + requestId));

        if ("completed".equals(request.getStatus()) || "canceled".equals(request.getStatus())) {
            throw new IllegalArgumentException("Нельзя отменить заявку в статусе '" + request.getStatus() + "'");
        }

        request.setStatus("canceled");

        Request saved = requestRepository.save(request);
        log.info("Заявка отменена: ID={}", saved.getId());
        return saved;
    }
}
