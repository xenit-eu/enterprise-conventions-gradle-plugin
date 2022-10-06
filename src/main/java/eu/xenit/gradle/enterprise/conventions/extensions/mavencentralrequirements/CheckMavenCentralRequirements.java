package eu.xenit.gradle.enterprise.conventions.extensions.mavencentralrequirements;

import java.util.LinkedList;
import java.util.List;
import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.provider.Property;
import org.gradle.api.publish.maven.MavenPublication;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.TaskAction;

public abstract class CheckMavenCentralRequirements extends DefaultTask {
    @Internal
    public abstract Property<MavenPublication> getPublication();

    @TaskAction
    public void check() {
        List<RuntimeException> violations = new LinkedList<>();
        var checkAction = new ValidatePublicationAction(getPublication()::get, violations::add);
        checkAction.execute(this);

        if(!violations.isEmpty()) {
            violations.forEach(violation -> getLogger().error("Failed requirement: {}", violation.getMessage()));

            throw new GradleException("Maven Central requirements are not met");
        } else {
            getLogger().info("Maven Central requirements for publication '{}' are met", getPublication().get().getName());
        }
    }

}
