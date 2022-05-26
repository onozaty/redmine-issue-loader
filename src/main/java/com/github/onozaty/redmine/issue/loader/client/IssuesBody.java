package com.github.onozaty.redmine.issue.loader.client;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class IssuesBody {

    private List<Issue> issues;
}
