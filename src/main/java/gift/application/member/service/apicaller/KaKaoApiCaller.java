package gift.application.member.service.apicaller;

import static io.jsonwebtoken.Header.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED_VALUE;

import com.fasterxml.jackson.databind.JsonNode;
import gift.global.validate.TimeOutException;
import java.net.URI;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

@Component
public class KaKaoApiCaller {

    private static final String KAKAO_USER_INFO_URL = "https://kapi.kakao.com/v2/user/me";
    private static final String KAKAO_TOKEN_URL = "https://kauth.kakao.com/oauth/token";
    private static final String GRANT_TYPE = "authorization_code";
    private static final Duration TIMEOUT = Duration.ofSeconds(2);
    @Value("${kakao.client_id}")
    private String CLIENT_ID;
    @Value("${kakao.redirect_uri}")
    private String REDIRECT_URI;


    private final RestTemplate restTemplate;

    public KaKaoApiCaller(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder
            .setConnectTimeout(TIMEOUT)
            .setReadTimeout(TIMEOUT)
            .build();
    }

    /**
     * 인가 코드를 사용해서 토큰을 가져옴
     */
    public String getAccessToken(String authorizationCode) {
        var headers = new HttpHeaders();
        headers.add(CONTENT_TYPE, APPLICATION_FORM_URLENCODED_VALUE);
        var body = createGetAccessTokenBody(authorizationCode);
        System.out.println(body);
        var request = new RequestEntity<>(body, headers, HttpMethod.POST,
            URI.create(KAKAO_TOKEN_URL));
        try {
            ResponseEntity<JsonNode> response = restTemplate.exchange(request, JsonNode.class);
            return response.getBody().get("access_token").asText();
        } catch (ResourceAccessException e) {
            throw new TimeOutException("네트워크 연결이 불안정 합니다.", e);
        } catch (HttpClientErrorException e) {
            throw new IllegalArgumentException("카카오 인가 코드가 유효하지 않습니다.", e);
        }
    }

    /**
     * 토큰을 사용해서 사용자 정보를 가져옴
     */
    public JsonNode getMemberInfo(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        headers.add(CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE);
        var request = new RequestEntity<>(headers, HttpMethod.GET, URI.create(KAKAO_USER_INFO_URL));

        try {
            ResponseEntity<JsonNode> responseNode = restTemplate.exchange(request, JsonNode.class);
            return responseNode.getBody();
        } catch (ResourceAccessException e) {
            throw new TimeOutException("네트워크 연결이 불안정 합니다.", e);
        }
    }

    private LinkedMultiValueMap<String, String> createGetAccessTokenBody(
        String authorizationCode) {
        var body = new LinkedMultiValueMap<String, String>();

        body.add("grant_type", GRANT_TYPE);
        body.add("client_id", CLIENT_ID);
        body.add("redirect_uri", REDIRECT_URI);
        body.add("code", authorizationCode);
        return body;
    }
}
