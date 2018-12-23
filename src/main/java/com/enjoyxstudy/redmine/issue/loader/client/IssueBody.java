package com.enjoyxstudy.redmine.issue.loader.client;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class IssueBody {

    @JsonProperty("issue")
    private Map<String, Object> fields;
}
