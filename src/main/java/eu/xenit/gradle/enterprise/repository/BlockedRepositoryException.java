package eu.xenit.gradle.enterprise.repository;

import java.net.URI;

public class BlockedRepositoryException extends RuntimeException {

    public BlockedRepositoryException(URI blockedRepo, String reason) {
        super("Repository " + blockedRepo.toASCIIString() + " is blocked: " + reason);
    }
}
