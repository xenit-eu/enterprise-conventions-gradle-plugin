package eu.xenit.gradle.enterprise.repository;

import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.credentials.PasswordCredentials;

final class CredentialsUtil {

    public static final String USERNAME_PROPERTY = "eu.xenit.artifactory.username";
    public static final String PASSWORD_PROPERTY = "eu.xenit.artifactory.password";

    private CredentialsUtil() {
    }

    public static Action<? super PasswordCredentials> configureArtifactoryCredentials(Project project) {
        String username = (String) project.findProperty(USERNAME_PROPERTY);
        String password = (String) project.findProperty(PASSWORD_PROPERTY);

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
        return project.hasProperty(USERNAME_PROPERTY) && project.hasProperty(PASSWORD_PROPERTY);
    }
}
