package com.enjoyxstudy.redmine.issue.updater.client;

import java.util.List;

import com.enjoyxstudy.redmine.issue.updater.Issue;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class IssuesBody {

    private List<Issue> issues;
}
