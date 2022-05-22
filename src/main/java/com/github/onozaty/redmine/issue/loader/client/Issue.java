package com.github.onozaty.redmine.issue.loader.client;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.github.onozaty.redmine.issue.loader.input.CustomField;

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
