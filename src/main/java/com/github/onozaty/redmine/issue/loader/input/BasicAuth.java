package com.github.onozaty.redmine.issue.loader.input;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class BasicAuth {

    private String username;

    private String password;

    public String toAuthorizationValue() {

        return "Basic "
                + Base64.getEncoder().encodeToString(
                        (username + ":" + password).getBytes(StandardCharsets.UTF_8));
    }
}
