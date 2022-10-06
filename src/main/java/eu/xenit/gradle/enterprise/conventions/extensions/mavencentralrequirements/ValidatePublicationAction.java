package eu.xenit.gradle.enterprise.conventions.extensions.mavencentralrequirements;

import eu.xenit.gradle.enterprise.conventions.extensions.mavencentralrequirements.PomValidationException.ErrorType;
import eu.xenit.gradle.enterprise.conventions.violations.FatalViolation;
import eu.xenit.gradle.enterprise.conventions.violations.ViolationHandler;
import java.net.URI;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.gradle.api.Action;
import org.gradle.api.Task;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.provider.Provider;
import org.gradle.api.publish.internal.PublicationArtifactSet;
import org.gradle.api.publish.maven.MavenArtifact;
import org.gradle.api.publish.maven.MavenPublication;
import org.gradle.api.publish.maven.internal.publication.MavenPomInternal;
import org.gradle.api.publish.maven.internal.publication.MavenPublicationInternal;
import org.gradle.api.publish.maven.tasks.PublishToMavenRepository;

@RequiredArgsConstructor
public class ValidatePublicationAction implements Action<Task> {
    private static final Logger LOGGER = Logging.getLogger(ValidatePublicationAction.class);
    private final Supplier<MavenPublication> publication;
    private final ViolationHandler violationHandler;

    @Override
    public void execute(Task task) {
        if(task instanceof PublishToMavenRepository ) {
            var isSonatypeRepo = Optional.ofNullable(((PublishToMavenRepository)task).getRepository().getUrl())
                    .map(URI::getHost)
                    .map(host -> host.endsWith("oss.sonatype.org"))
                    .orElse(false);

            if (Boolean.FALSE.equals(isSonatypeRepo)) {
                return;
            }
        }
        try {
            var publicationInternal = (MavenPublicationInternal) this.publication.get();
            checkPomRequirements(publicationInternal.getPom());
            String artifactPrefix =
                    publicationInternal.getCoordinates().getName() + "-" + publicationInternal.getCoordinates().getVersion();
            if (publicationInternal.getPublishableArtifacts().stream()
                    .anyMatch(artifact -> artifact.getClassifier() == null && artifact.getExtension().equals("jar"))) {
                checkArtifactWithClassifierPresent(artifactPrefix, "sources", publicationInternal.getPublishableArtifacts());
                checkArtifactWithClassifierPresent(artifactPrefix, "javadoc", publicationInternal.getPublishableArtifacts());
            }

            checkArtifactsSigned(artifactPrefix, publicationInternal.getPublishableArtifacts());
        } catch(FatalViolation e) {
            // Do not swallow fatal violations
            throw e;
        } catch(Throwable e) {
            // We are using internal APIs in here, catch potential errors thrown in future versions
            // if these internals have moved around/changed
            LOGGER.warn("Failed to perform publication validation on task {}: Unexpected exception (Gradle internals changed?)", task.getName(), e);
        }
    }

    private void checkArtifactWithClassifierPresent(String artifactPrefix, String classifier, PublicationArtifactSet<MavenArtifact> publishableArtifacts) {
        if(publishableArtifacts.stream().noneMatch(artifact -> Objects.equals(artifact.getClassifier(), classifier) && artifact.getExtension().equals("jar"))) {
            violationHandler.handleViolation(new ArtifactValidationException(publication.get(), artifactPrefix+"-"+classifier+".jar"));
        }
    }

    private static String createClassifierAndExt(MavenArtifact artifact) {
        if(artifact.getClassifier() != null) {
            return "-"+artifact.getClassifier()+"."+artifact.getExtension();
        }
        return "."+artifact.getExtension();
    }

    private void checkArtifactsSigned(String artifactPrefix, PublicationArtifactSet<MavenArtifact> publicationArtifactSet) {
        var allArtifactIdentifiers = publicationArtifactSet.stream()
                .map(ValidatePublicationAction::createClassifierAndExt)
                .collect(Collectors.toSet());
        var signatureArtifactIdentifiers = publicationArtifactSet.stream()
                .filter(artifact -> artifact.getExtension().endsWith(".asc"))
                .map(ValidatePublicationAction::createClassifierAndExt)
                .collect(Collectors.toUnmodifiableSet());

        var nonSignatureArtifactIdentifiers = new HashSet<>(allArtifactIdentifiers);
        nonSignatureArtifactIdentifiers.removeAll(signatureArtifactIdentifiers);

        for (String nonSignatureArtifactIdentifier : nonSignatureArtifactIdentifiers) {
            if(!signatureArtifactIdentifiers.contains(nonSignatureArtifactIdentifier+".asc")) {
                violationHandler.handleViolation(
                        new SignatureValidationException(
                                publication.get(),
                                artifactPrefix+nonSignatureArtifactIdentifier,
                                artifactPrefix+nonSignatureArtifactIdentifier+".asc"
                        )
                );
            }
        }
    }

    private void checkPomRequirements(MavenPomInternal pom) {
        validateStringProperty("description", pom.getDescription());
        validateStringProperty("name", pom.getName());
        validateStringProperty("url", pom.getUrl());
        validateListProperty("licenses", pom.getLicenses(), license -> {
            validateStringPropertyThrowing("name", license.getName());
            validateStringPropertyThrowing("url", license.getUrl());
        });

        validateListProperty("developers", pom.getDevelopers(), developer ->
                validateStringPropertyThrowing("name", developer.getName())
        );

        validateStringPropertyThrowing("scm.connection", pom.getScm().getConnection());
        validateStringPropertyThrowing("scm.developerConnection", pom.getScm().getDeveloperConnection());
        validateStringPropertyThrowing("scm.url", pom.getScm().getUrl());
    }

    private void validateStringProperty(String name, Provider<String> prop) {
        try {
            validateStringPropertyThrowing(name, prop);
        } catch(PomValidationException e) {
            violationHandler.handleViolation(e);
        }
    }

    private void validateStringPropertyThrowing(String name, Provider<String> prop) {
        if(!prop.isPresent()) {
            throw new PomValidationException(publication.get(), name, ErrorType.ABSENT);
        }
        if(prop.get().isBlank()) {
            throw new PomValidationException(publication.get(), name, ErrorType.EMPTY);
        }
    }

    private <T> void validateListProperty(String name, List<T> items, Action<T> validator) {
        if(items.isEmpty()) {
            violationHandler.handleViolation(new PomValidationException(publication.get(), name, ErrorType.EMPTY));
            return;
        }
        for (int i = 0; i < items.size(); i++) {
            try {
                validator.execute(items.get(i));
            } catch(PomValidationException pomValidationException) {
                violationHandler.handleViolation(new PomValidationException(
                        publication.get(),
                        name+"["+i+"]",
                        ErrorType.INVALID,
                        new PomValidationException(
                                publication.get(),
                                name+"["+i+"]"+pomValidationException.getName(),
                                pomValidationException.getErrorType()
                        )
                ));
            }
        }
    }
}
