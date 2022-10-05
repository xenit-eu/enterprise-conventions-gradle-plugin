package eu.xenit.gradle.enterprise.conventions.internal;

import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.credentials.PasswordCredentials;

public final class ArtifactoryCredentialsUtil {

    public static final String USERNAME_PROPERTY = "eu.xenit.artifactory.username";
    public static final String PASSWORD_PROPERTY = "eu.xenit.artifactory.password";

    private ArtifactoryCredentialsUtil() {
    }

    public static Action<? super PasswordCredentials> configureArtifactoryCredentials(Project project) {
        return configureArtifactoryCredentials(PropertyReader.from(project));
    }

    public static Action<? super PasswordCredentials> configureArtifactoryCredentials(PropertyReader propertyReader) {
        String username = (String) propertyReader.findProperty(USERNAME_PROPERTY);
        String password = (String) propertyReader.findProperty(PASSWORD_PROPERTY);

        if (username != null && password != null) {
            return (passwordCredentials) -> {
                passwordCredentials.setUsername(username);
                passwordCredentials.setPassword(password);
            };
        } else {
            return (passwordCredentials -> {
            });
        }
    }

    public static boolean hasArtifactoryCredentials(Project project) {
        return hasArtifactoryCredentials(PropertyReader.from(project));
    }

    public static boolean hasArtifactoryCredentials(PropertyReader propertyReader) {
        return propertyReader.hasProperty(USERNAME_PROPERTY) && propertyReader.hasProperty(PASSWORD_PROPERTY);
    }
}
