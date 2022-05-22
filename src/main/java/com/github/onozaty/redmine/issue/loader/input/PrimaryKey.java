package com.github.onozaty.redmine.issue.loader.input;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.onozaty.redmine.issue.loader.client.QueryParameter;

public interface PrimaryKey {

    @JsonIgnore
    QueryParameter getQueryParameter();
}
