package eu.xenit.gradle.enterprise.conventions.extensions.signing;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.gradle.api.Project;
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin;
import org.gradle.plugins.signing.Sign;
import org.gradle.plugins.signing.signatory.pgp.PgpSignatory;
import org.gradle.security.internal.gnupg.GnupgSignatory;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.Test;

public class AutomaticSigningPluginTest extends AbstractSigningPluginSetup {

    @Test
    public void doesNotApplySigningPlugin() {
        Project project = ProjectBuilder.builder().build();

        project.getPluginManager().apply(AutomaticSigningPlugin.class);
        project.getPluginManager().apply(MavenPublishPlugin.class);

        assertFalse(project.getPlugins().hasPlugin("signing"));
    }


    @Test
    public void configuresSigningWithoutConfiguration() {
        Project project = createProject((p) -> {
        });

        Sign signTask = project.getTasks().withType(Sign.class).getByName("signMavenJavaPublication");

        // Without further configuration, assert that it no signatory can be configured
        assertNull(signTask.getSignatory());
    }


    @Test
    public void configuresSigningWithSigningConfiguration() {
        Project project = createProject(this::configureSigningWithPgp);

        Sign signTask = project.getTasks().withType(Sign.class).getByName("signMavenJavaPublication");

        assertThat(signTask.getSignatory(), instanceOf(PgpSignatory.class));
        assertEquals("32C2FC7D", signTask.getSignatory().getKeyId());
    }

    @Test
    public void configuresSigningWithEnvironmentVariable() throws Exception {
        withInMemorySigningEnv(() -> {
            Project project = createProject(p -> {});

            Sign signTask = project.getTasks().withType(Sign.class).getByName("signMavenJavaPublication");

            assertThat(signTask.getSignatory(), instanceOf(PgpSignatory.class));
            assertEquals("32C2FC7D", signTask.getSignatory().getKeyId());
        });
    }

    @Test
    public void configuresSigningSubkeyWithEnvironmentVariable() throws Exception {
        withInMemorySigningEnvAndSubkey("E99CFF0D", () -> {
            Project project = createProject(p -> {});

            Sign signTask = project.getTasks().withType(Sign.class).getByName("signMavenJavaPublication");

            assertThat(signTask.getSignatory(), instanceOf(PgpSignatory.class));
            assertEquals("E99CFF0D", signTask.getSignatory().getKeyId());
        });
    }

    @Test
    public void configuresSigningWithAgent() {
        Project project = createProject(this::configureSigningWithAgent);

        Sign signTask = project.getTasks().withType(Sign.class).getByName("signMavenJavaPublication");

        assertThat(signTask.getSignatory(), instanceOf(GnupgSignatory.class));
        assertEquals("32C2FC7D", signTask.getSignatory().getKeyId());
    }

}
