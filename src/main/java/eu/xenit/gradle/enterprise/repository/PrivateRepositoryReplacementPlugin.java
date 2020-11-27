package eu.xenit.gradle.enterprise.repository;

import eu.xenit.gradle.enterprise.internal.ArtifactoryCredentialsUtil;
import eu.xenit.gradle.enterprise.internal.StringConstants;
import eu.xenit.gradle.enterprise.violations.ViolationHandler;
import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.gradle.api.Project;
import org.gradle.api.artifacts.repositories.MavenArtifactRepository;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

/**
 * Replaces specific public and private repositories with an internal artifactory proxy.
 */
public class PrivateRepositoryReplacementPlugin extends AbstractRepositoryPlugin {

    private static final Map<URI, String> replacements;

    static {
        Map<URI, String> replacementsMap = new HashMap<>();

        replacementsMap
                .put(URI.create("https://artifacts.alfresco.com/nexus/content/groups/public"), "alfresco-community");
        replacementsMap
                .put(URI.create("https://artifacts.alfresco.com/nexus/content/repositories/enterprise-releases"),
                        "alfresco-enterprise");
        replacementsMap
                .put(URI.create("https://artifactory.arondor.cloud/artifactory/arondor-release"), "arondor-release");
        replacementsMap.put(URI.create("https://clojars.org/repo"), "clojars");
        replacementsMap.put(URI.create("https://maven.springframework.org/release"), "spring-release");
        replacementsMap.put(URI.create("http://repo.typesafe.com/typesafe/releases"), "typesafe");

        withEndingSlash(replacementsMap);
        replacements = Collections.unmodifiableMap(replacementsMap);
    }

    private static final Logger LOGGER = Logging.getLogger(PrivateRepositoryPlugin.class);

    @Override
    protected ValidationResult validateRepository(MavenArtifactRepository repository, Project project,
            ViolationHandler violationHandler) {
        if (repository.getUrl().toString().startsWith(StringConstants.XENIT_BASE_URL)) {
            LOGGER.debug("Allowing enterprise repository: {}", repository.getUrl());
            return ValidationResult.ALLOWED;
        }

        ValidationResult validationResult = super.validateRepository(repository, project, violationHandler);
        if (validationResult.isFinal()) {
            return validationResult;
        }

        String replacement = replacements.get(repository.getUrl());

        if (replacement != null) {
            if (ArtifactoryCredentialsUtil.hasArtifactoryCredentials(project)) {
                LOGGER.debug("Replacing repository {} with enterprise repository", repository.getUrl());
                repository.setUrl(URI.create(StringConstants.XENIT_BASE_URL + replacement));
                repository.credentials(ArtifactoryCredentialsUtil.configureArtifactoryCredentials(project));
                return ValidationResult.ALLOWED;
            } else {
                LOGGER.info(
                        "Xenit Artifactory credentials were not provided. Not replacing repositories with internal proxy.");
                // Return here. Repository was not replaced, but it is not explicitly disallowed per our policy (as we are proxying it)
                return ValidationResult.NEUTRAL;
            }
        }

        return ValidationResult.NEUTRAL;
    }
}
