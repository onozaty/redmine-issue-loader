package com.enjoyxstudy.redmine.issue.updater;

import java.util.Map;

import lombok.Data;

@Data
public class FieldSetting {

    private String headerName;

    private FieldType type;

    private Integer customFieldId;

    private boolean isPrimaryKey;
    
    private Map<String, String> mappings;
}
