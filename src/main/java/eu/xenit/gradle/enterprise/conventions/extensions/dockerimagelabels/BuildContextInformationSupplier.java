package eu.xenit.gradle.enterprise.conventions.extensions.dockerimagelabels;

import java.util.Optional;
import org.gradle.api.Project;

public interface BuildContextInformationSupplier {

    Optional<BuildContextInformation> find(Project project);
}
