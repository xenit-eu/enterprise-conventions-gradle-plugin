package eu.xenit.gradle.enterprise.conventions.integration;

import lombok.RequiredArgsConstructor;
import org.gradle.api.JavaVersion;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

@RequiredArgsConstructor
public class JavaVersionCompatibilityMatcher extends TypeSafeMatcher<JavaVersion> {
    private final JavaVersion targetVersion;


    @Override
    protected boolean matchesSafely(JavaVersion item) {
        return item.isCompatibleWith(targetVersion);
    }


    @Override
    public void describeTo(Description description) {
        description.appendText("Java version ").appendValue(targetVersion);

    }
}
