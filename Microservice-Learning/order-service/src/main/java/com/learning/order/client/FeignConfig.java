package com.learning.order.client;

import com.learning.order.exception.RemoteServiceException;
import feign.Util;
import feign.codec.ErrorDecoder;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Enables Feign and converts error responses from other services into our own exception. */
@Configuration
@EnableFeignClients
public class FeignConfig {

    /** Turn any 4xx/5xx from the other service into a RemoteServiceException. */
    @Bean
    ErrorDecoder errorDecoder(RemoteErrorParser parser) {
        return (methodKey, response) -> {
            byte[] body = new byte[0];
            try {
                if (response.body() != null) {
                    body = Util.toByteArray(response.body().asInputStream());
                }
            } catch (Exception ignored) {
                // fall back to the generic message below
            }
            String message = parser.extractMessage(body,
                    methodKey + " returned HTTP " + response.status());
            return new RemoteServiceException(response.status(), message);
        };
    }
}
