package gift.oauth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import gift.oauth.ConfigPropertiesTest.TestConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.LinkedMultiValueMap;

@SpringBootTest
public class OauthServiceTest {

    @Autowired
    private OauthService oauthService;

    @Value("${kakao.client-id}")
    String clientId;
    @Value("${kakao.redirect-url}")
    String redirectUri;

    private LinkedMultiValueMap<String, String> body;

    @BeforeEach
    void setBody() {
        body = new LinkedMultiValueMap<String, String>();
        body.add("grant_type", "authorization_code");
        body.add("client_id", clientId);
        body.add("redirect_uri", redirectUri);
        body.add("code", "code");
    }

    /*
    - [] requsetBody에 secret값이 잘 들어가는 지 테스트
    - [] kakaoToken을 잘 받아오는지 테스트
     */

    @DisplayName("requsetBody에 secret값이 잘 들어가는 지 테스트")
    @Test
    void getRequestBody() {
        var expect = body;
        var actual = oauthService.getRequestBody("code");

        assertAll(
            () -> assertThat(expect.get("client_id")).isEqualTo(actual.get("client_id")),
            () -> assertThat(actual.get("redirect_uri").get(0)).isEqualTo(redirectUri)
        );
    }

    @DisplayName("kakaoToken을 잘 받아오는지 테스트")
    @Test
    void getKakaoToken() {
        /*
        \code값을 매번 설정해주어야 테스트를 할 수 있을 것 같습니다.
        다만 그렇게 하면 자동테스트는 진행하지 못할 것 같습니다.
        어떻게 하는게 좋을지 생각이 나지 않습니다..
        */
        var response = oauthService.getKakaoToken("code");
    }
}
