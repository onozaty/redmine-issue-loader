package com.github.onozaty.redmine.issue.loader.input;

import com.github.onozaty.redmine.issue.loader.client.QueryParameter;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class IssueId implements PrimaryKey {

    private int id;

    @Override
    public QueryParameter getQueryParameter() {
        return new QueryParameter("issue_id", String.valueOf(id));
    }
}
