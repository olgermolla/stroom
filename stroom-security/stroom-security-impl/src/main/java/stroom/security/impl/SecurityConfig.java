package stroom.security.impl;

import com.fasterxml.jackson.annotation.JsonProperty;
import stroom.util.shared.IsConfig;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class SecurityConfig implements IsConfig {

    private AuthenticationConfig authenticationConfig;

    public SecurityConfig() {
        this.authenticationConfig = new AuthenticationConfig();
    }

    @Inject
    SecurityConfig(final AuthenticationConfig authenticationConfig) {
        this.authenticationConfig = authenticationConfig;
    }

    @JsonProperty("authentication")
    public AuthenticationConfig getAuthenticationConfig() {
        return authenticationConfig;
    }

    public void setAuthenticationConfig(final AuthenticationConfig authenticationConfig) {
        this.authenticationConfig = authenticationConfig;
    }
}
