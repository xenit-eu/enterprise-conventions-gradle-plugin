package eu.xenit.gradle.enterprise.conventions.integration;

import lombok.RequiredArgsConstructor;
import org.gradle.api.JavaVersion;
import org.gradle.util.GradleVersion;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

@RequiredArgsConstructor
public class GradleVersionCompatibilityMatcher extends TypeSafeMatcher<GradleVersion> {
    private final GradleVersion targetVersion;


    @Override
    protected boolean matchesSafely(GradleVersion item) {
        return item.compareTo(targetVersion) >= 0;
    }


    @Override
    public void describeTo(Description description) {
        description.appendText("Gradle version ").appendValue(targetVersion);

    }
}
