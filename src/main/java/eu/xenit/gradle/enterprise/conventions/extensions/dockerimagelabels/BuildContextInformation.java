package eu.xenit.gradle.enterprise.conventions.extensions.dockerimagelabels;

import java.io.Serializable;
import lombok.Value;

@Value
public class BuildContextInformation implements Serializable {

    String repository;
    String commit;
}
