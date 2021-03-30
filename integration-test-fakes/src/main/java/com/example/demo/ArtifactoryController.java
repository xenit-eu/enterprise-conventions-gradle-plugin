package com.example.demo;

import java.io.IOException;
import java.nio.charset.Charset;
import org.apache.commons.io.IOUtils;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ArtifactoryController {

    @GetMapping(value = "/artifactory/api/repositories", produces = {MediaType.APPLICATION_JSON_VALUE})
    @ResponseBody
    public String repositories() throws IOException {
        return IOUtils.resourceToString("/templates/repositories.json", Charset.defaultCharset());
    }


}
