package com.github.onozaty.redmine.issue.loader.input;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Data
@NoArgsConstructor
public class Config {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @NonNull
    private LoadMode mode;

    @NonNull
    private String readmineUrl;

    private String apiKey;

    private BasicAuth basicAuth;

    private int timeout = 10;

    private ReplaceString replaceString;

    @NonNull
    private String csvEncoding;

    @NonNull
    private List<FieldSetting> fields;

    public static Config of(Path configPath) throws JsonParseException, JsonMappingException, IOException {
        return objectMapper.readValue(configPath.toFile(), Config.class);
    }
}
