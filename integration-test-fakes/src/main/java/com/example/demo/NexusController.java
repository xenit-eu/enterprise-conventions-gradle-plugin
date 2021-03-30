package com.example.demo;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.commons.io.IOUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class NexusController {

    private static Map<String, String> STORAGE = new ConcurrentHashMap<>();

    @GetMapping("/service/local/staging/profiles")
    public String getProfiles() throws IOException {
        return IOUtils.resourceToString("/templates/staging-profiles.json", Charset.defaultCharset());
    }

    @PostMapping("/service/local/staging/profiles/1234/start")
    public StagingCreateResponseBody postProfiles(@RequestBody StagingCreateRequestBody requestBody) {
        StagingCreateResponseBody response = new StagingCreateResponseBody();
        response.data.description = requestBody.data.description;
        response.data.stagedRepositoryId = "xeniteu-1";
        return response;
    }

    @PutMapping("/service/local/staging/deployByRepositoryId/xeniteu-1/{*name}")
    @ResponseBody
    public ResponseEntity putData(@PathVariable String name, @RequestBody String request) {
        STORAGE.put(name, request);
        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }

    @GetMapping("/service/local/staging/deployByRepositoryId/xeniteu-1/{*name}")
    public String getData(@PathVariable String name) {
        if (!STORAGE.containsKey(name)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        return STORAGE.get(name);
    }

    public static class StagingCreateRequestBody {

        public StagingCreateRequestData data;
    }

    public static class StagingCreateRequestData {

        public String description;
    }

    public static class StagingCreateResponseBody {

        public StagingCreateResponseData data = new StagingCreateResponseData();
    }

    public static class StagingCreateResponseData {

        public String stagedRepositoryId;
        public String description;
    }
}
