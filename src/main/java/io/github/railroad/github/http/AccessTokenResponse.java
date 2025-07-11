package io.github.railroad.github.http;

import com.google.gson.JsonObject;
import lombok.Getter;

import javax.lang.model.type.ErrorType;
import java.util.Locale;

public abstract sealed class AccessTokenResponse permits AccessTokenResponse.SuccessResponse, AccessTokenResponse.ErrorResponse {
    public static AccessTokenResponse fromJson(JsonObject json) {
        if (json.has("error")) {
            String error = json.get("error").getAsString();
            ErrorType errorType;
            try {
                errorType = ErrorType.valueOf(error.toUpperCase(Locale.ROOT).replace('-', '_'));
            } catch (IllegalArgumentException exception) {
                errorType = null;
            }

            return new ErrorResponse(errorType);
        } else {
            return new SuccessResponse(
                    json.get("access_token").getAsString(),
                    json.get("expires_in").getAsInt(),
                    json.has("refresh_token") ? json.get("refresh_token").getAsString() : null,
                    json.has("refresh_expires_in") ? json.get("refresh_expires_in").getAsInt() : 0,
                    json.get("scope").getAsString(),
                    json.get("token_type").getAsString()
            );
        }
    }

    @Getter
    public static final class SuccessResponse extends AccessTokenResponse {
        private final String accessToken;
        private final int accessTokenExpiresIn;
        private final String refreshToken;
        private final int refreshTokenExpiresIn;
        private final String scope;
        private final String tokenType;

        public SuccessResponse(String accessToken, int accessTokenExpiresIn, String refreshToken, int refreshTokenExpiresIn, String scope, String tokenType) {
            this.accessToken = accessToken;
            this.accessTokenExpiresIn = accessTokenExpiresIn;
            this.refreshToken = refreshToken;
            this.refreshTokenExpiresIn = refreshTokenExpiresIn;
            this.scope = scope;
            this.tokenType = tokenType;
        }
    }

    @Getter
    public static final class ErrorResponse extends AccessTokenResponse {
        private final AccessTokenResponse.ErrorType errorType;

        public ErrorResponse(AccessTokenResponse.ErrorType errorType) {
            this.errorType = errorType;
        }
    }

    public enum ErrorType {
        AUTHORIZATION_PENDING,
        SLOW_DOWN,
        EXPIRED_TOKEN,
        UNSUPPORTED_GRANT_TYPE,
        INCORRECT_CLIENT_CREDENTIALS,
        INCORRECT_DEVICE_CODE,
        ACCESS_DENIED,
        DEVICE_FLOW_DISABLED
    }
}
