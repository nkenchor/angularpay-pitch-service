package io.angularpay.pitch.adapters.inbound;

import io.angularpay.pitch.adapters.outbound.CipherServiceAdapter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CipherFilterRegistrar {

    @ConditionalOnProperty(
            value = "angularpay.cipher.enabled",
            havingValue = "true",
            matchIfMissing = true)
    @Bean
    public FilterRegistrationBean<CipherFilter> registerPostCommentsRateLimiter(CipherServiceAdapter cipherServiceAdapter) {
        FilterRegistrationBean<CipherFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new CipherFilter(cipherServiceAdapter));
        registrationBean.addUrlPatterns(
                "/pitch/requests",
                "/pitch/requests/*/summary",
                "/pitch/requests/*/amount",
                "/pitch/requests/*/equity",
                "/pitch/requests/*/investors",
                "/pitch/requests/*/bargains",
                "/pitch/requests/*/investors/*/amount",
                "/pitch/requests/*/investors/*/payment"
        );
        return registrationBean;
    }
}
