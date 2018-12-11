package com.enjoyxstudy.redmine.issue.updater;

import lombok.Data;

@Data
public class FieldSetting {

    private String headerName;

    private FieldType type;

    private Integer customFieldId;

    private boolean isPrimaryKey;
}
