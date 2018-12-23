package com.enjoyxstudy.redmine.issue.loader.client;

import java.util.List;

import com.enjoyxstudy.redmine.issue.loader.input.CustomField;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Singular;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Issue {

    private Integer id;

    @Singular
    private List<CustomField> customFields;
}
