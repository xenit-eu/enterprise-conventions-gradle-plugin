package eu.xenit.gradle.enterprise.conventions.extensions.dockerimagelabels;

import java.util.function.BiConsumer;
import org.gradle.api.Project;
import org.gradle.api.provider.Provider;

public interface BuildContextInformationApplier extends BiConsumer<Project, Provider<BuildContextInformation>> {

}
