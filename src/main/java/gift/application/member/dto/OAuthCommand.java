package gift.application.member.dto;

import com.fasterxml.jackson.databind.JsonNode;
import gift.application.member.dto.MemberCommand.Login;
import gift.model.member.Provider;
import gift.model.member.Role;

public class OAuthCommand {

    public record Login(String authorizationCode) {

    }

    public record MemberInfo(
        String email,
        String nickname,
        Provider provider) {

        public static MemberInfo from(JsonNode userInfo) {
            return new MemberInfo(
                userInfo.get("kakao_account").get("email").asText(),
                userInfo.get("properties").get("nickname").asText(),
                Provider.KAKAO
            );
        }

        public MemberCommand.Create toCreateCommand() {
            return new MemberCommand.Create(
                email,
                provider.name(),
                nickname,
                Role.USER,
                provider
            );
        }
    }
}
