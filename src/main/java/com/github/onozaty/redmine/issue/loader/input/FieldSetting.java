package com.github.onozaty.redmine.issue.loader.input;

import java.util.Map;

import lombok.Data;

@Data
public class FieldSetting {

    private String headerName;

    private FieldType type;

    private Integer customFieldId;

    private boolean isPrimaryKey;

    private String multipleItemSeparator;

    private Map<String, String> mappings;
}
