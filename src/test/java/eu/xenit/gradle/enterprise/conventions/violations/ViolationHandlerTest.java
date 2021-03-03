package eu.xenit.gradle.enterprise.conventions.violations;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;

import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.Test;

public class ViolationHandlerTest {

    @Test
    public void testDefaultLevel() {
        Project project = ProjectBuilder.builder().build();
        ViolationHandler handler = ViolationHandler.fromProject(project, "category");

        assertThat(handler, instanceOf(FatalViolationHandler.class));
    }

    @Test
    public void testEnforceLevel() {
        Project project = ProjectBuilder.builder().build();
        project.getExtensions().getExtraProperties().set("eu.xenit.enterprise-conventions.violations", "enforce");
        ViolationHandler handler = ViolationHandler.fromProject(project, "category");

        assertThat(handler, instanceOf(FatalViolationHandler.class));
    }

    @Test
    public void testLogLevel() {
        Project project = ProjectBuilder.builder().build();
        project.getExtensions().getExtraProperties().set("eu.xenit.enterprise-conventions.violations", "log");
        ViolationHandler handler = ViolationHandler.fromProject(project, "category");

        assertThat(handler, instanceOf(LogOnlyViolationHandler.class));
    }

    @Test
    public void testDisableLevel() {
        Project project = ProjectBuilder.builder().build();
        project.getExtensions().getExtraProperties().set("eu.xenit.enterprise-conventions.violations", "disable");
        ViolationHandler handler = ViolationHandler.fromProject(project, "category");

        assertThat(handler, instanceOf(DisabledViolationHandler.class));
    }

    @Test
    public void testConfigureCategory() {
        Project project = ProjectBuilder.builder().build();
        project.getExtensions().getExtraProperties()
                .set("eu.xenit.enterprise-conventions.violations.category", "disable");
        ViolationHandler handler = ViolationHandler.fromProject(project, "category");

        assertThat(handler, instanceOf(DisabledViolationHandler.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidLevel() {
        Project project = ProjectBuilder.builder().build();
        project.getExtensions().getExtraProperties().set("eu.xenit.enterprise-conventions.violations", "invalid-value");
        ViolationHandler.fromProject(project, "category");
    }
}
