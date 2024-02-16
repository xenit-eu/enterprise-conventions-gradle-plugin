package eu.xenit.gradle.enterprise.conventions.extensions.dockerimagelabels;

import java.util.Objects;
import java.util.Optional;
import org.gradle.api.Project;

public class GithubActionsBuildContextInformationSupplier implements BuildContextInformationSupplier {

    @Override
    public Optional<BuildContextInformation> find(Project project) {
        if (!Objects.equals(System.getenv("GITHUB_ACTIONS"), "true")) {
            return Optional.empty();
        }

        return Optional.of(new BuildContextInformation(
                String.format("%s/%s", System.getenv("GITHUB_SERVER_URL"), System.getenv("GITHUB_REPOSITORY")),
                System.getenv("GITHUB_SHA")
        ));
    }
}
