package gift.api.member.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "kakao")
public record KakaoProperties(
    KakaoUrl url,
    String grantType,
    String clientId
) {}
