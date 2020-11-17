package eu.xenit.gradle.enterprise.publish;

import java.net.URI;

public class BlockedPublishRepositoryException extends RuntimeException {

    public BlockedPublishRepositoryException(URI blockedRepo, String reason) {
        super("Publishing to " + blockedRepo.toASCIIString() + " is not allowed: " + reason);
    }
}
