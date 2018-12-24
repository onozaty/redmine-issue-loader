package com.enjoyxstudy.redmine.issue.loader.input;

import com.enjoyxstudy.redmine.issue.loader.client.QueryParameter;
import com.fasterxml.jackson.annotation.JsonIgnore;

public interface PrimaryKey {

    @JsonIgnore
    QueryParameter getQueryParameter();
}
