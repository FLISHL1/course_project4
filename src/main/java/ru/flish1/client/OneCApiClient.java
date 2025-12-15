package ru.flish1.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import ru.flish1.dto.CompletedOrderPayload;
import ru.flish1.dto.NomenclatureDto;
import ru.flish1.dto.SuccessResponse;

import java.util.List;

/**
 * Клиент для взаимодействия с API 1C
 */
@Service
public class OneCApiClient {

    private static final Logger log = LoggerFactory.getLogger(OneCApiClient.class);

    private final RestTemplate restTemplate;

    @Value("${integration.api.base-url}")
    private String baseUrl;

    @Value("${integration.api.key}")
    private String apiKey;

    public OneCApiClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Создает заголовки для запросов к API 1C с API ключом
     */
    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-API-Key", apiKey);
        return headers;
    }

    /**
     * Получает список номенклатуры из 1C
     *
     * @return список номенклатуры
     */
    public List<NomenclatureDto> getNomenclature() {
        try {
            String url = baseUrl + "/nomenclature";
            log.info("Запрос номенклатуры из 1C: {}", url);

            HttpEntity<String> entity = new HttpEntity<>(createHeaders());
            ResponseEntity<List<NomenclatureDto>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    new ParameterizedTypeReference<List<NomenclatureDto>>() {
                    }
            );

            log.info("Получено элементов номенклатуры: {}", response.getBody() != null ? response.getBody().size() : 0);
            return response.getBody();
        } catch (RestClientException e) {
            log.error("Ошибка при получении номенклатуры из 1C", e);
            throw new RuntimeException("Не удалось получить номенклатуру из 1C: " + e.getMessage(), e);
        }
    }

    /**
     * Отправляет данные о выполненном заказе в 1C
     *
     * @param payload данные о выполненном заказе
     * @return ответ от 1C с информацией о созданном документе
     */
    public SuccessResponse sendCompletedOrder(CompletedOrderPayload payload) {
        try {
            String url = baseUrl + "/completed-orders";
            log.info("Отправка выполненного заказа в 1C: {}", url);
            log.debug("Данные заказа: {}", payload);

            HttpEntity<CompletedOrderPayload> entity = new HttpEntity<>(payload, createHeaders());
            ResponseEntity<SuccessResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    SuccessResponse.class
            );

            log.info("Заказ успешно отправлен в 1C. Документ: {}", response.getBody() != null ? response.getBody().getDocument1cNumber() : "N/A");
            return response.getBody();
        } catch (RestClientException e) {
            log.error("Ошибка при отправке заказа в 1C", e);
            throw new RuntimeException("Не удалось отправить заказ в 1C: " + e.getMessage(), e);
        }
    }
}
