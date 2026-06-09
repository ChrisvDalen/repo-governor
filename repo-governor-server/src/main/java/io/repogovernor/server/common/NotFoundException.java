package io.repogovernor.server.common;

import java.util.UUID;

public class NotFoundException extends RuntimeException {

    public NotFoundException(String resource, UUID id) {
        super(resource + " not found: " + id);
    }

    public NotFoundException(String message) {
        super(message);
    }
}
