package com.learning.order.client;

import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

/**
 * Reads the "message" field from another service's error JSON, e.g.
 * {"status":404,"message":"Book not found with id 9", ...}
 */
@Component
public class RemoteErrorParser {

    private record RemoteError(String message) {
    }

    private final JsonMapper jsonMapper;

    public RemoteErrorParser(JsonMapper jsonMapper) {
        this.jsonMapper = jsonMapper;
    }

    public String extractMessage(byte[] body, String fallback) {
        try {
            RemoteError error = jsonMapper.readValue(body, RemoteError.class);
            return error.message() != null ? error.message() : fallback;
        } catch (Exception e) {
            return fallback;
        }
    }
}
