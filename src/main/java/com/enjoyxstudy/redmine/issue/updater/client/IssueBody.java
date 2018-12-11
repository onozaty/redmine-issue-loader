package com.enjoyxstudy.redmine.issue.updater.client;

import com.enjoyxstudy.redmine.issue.updater.Issue;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class IssueBody {

    private Issue issue;
}
