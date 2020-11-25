package eu.xenit.gradle.enterprise.violations;

import static org.junit.Assert.assertTrue;

import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.Test;

public class ViolationHandlerTest {

    @Test
    public void testDefaultLevel() {
        Project project = ProjectBuilder.builder().build();
        ViolationHandler handler = ViolationHandler.fromProject(project);

        assertTrue(handler instanceof FatalViolationHandler);
    }

    @Test
    public void testEnforceLevel() {
        Project project = ProjectBuilder.builder().build();
        project.getExtensions().getExtraProperties().set("eu.xenit.enterprise.violations", "enforce");
        ViolationHandler handler = ViolationHandler.fromProject(project);

        assertTrue(handler instanceof FatalViolationHandler);
    }

    @Test
    public void testLogLevel() {
        Project project = ProjectBuilder.builder().build();
        project.getExtensions().getExtraProperties().set("eu.xenit.enterprise.violations", "log");
        ViolationHandler handler = ViolationHandler.fromProject(project);

        assertTrue(handler instanceof LogOnlyViolationHandler);
    }

    @Test
    public void testDisableLevel() {
        Project project = ProjectBuilder.builder().build();
        project.getExtensions().getExtraProperties().set("eu.xenit.enterprise.violations", "disable");
        ViolationHandler handler = ViolationHandler.fromProject(project);

        assertTrue(handler instanceof DisabledViolationHandler);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidLevel() {
        Project project = ProjectBuilder.builder().build();
        project.getExtensions().getExtraProperties().set("eu.xenit.enterprise.violations", "invalid-value");
        ViolationHandler.fromProject(project);
    }
}
