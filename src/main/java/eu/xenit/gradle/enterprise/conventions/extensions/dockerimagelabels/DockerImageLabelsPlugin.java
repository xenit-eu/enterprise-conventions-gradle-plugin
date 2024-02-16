package eu.xenit.gradle.enterprise.conventions.extensions.dockerimagelabels;

import java.util.ServiceLoader;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.provider.Provider;

public class DockerImageLabelsPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        var contextInformationSupplierLoader = ServiceLoader.load(BuildContextInformationSupplier.class);
        Provider<BuildContextInformation> buildContextInformationProvider = project.provider(
                () -> contextInformationSupplierLoader
                        .stream()
                        .flatMap(supplier -> supplier.get().find(project).stream())
                        .findFirst()
                        .orElse(null)
        );
        ServiceLoader.load(BuildContextInformationApplier.class)
                .forEach(buildContextInformationApplier -> {
                    buildContextInformationApplier.accept(project, buildContextInformationProvider);
                });

    }
}
