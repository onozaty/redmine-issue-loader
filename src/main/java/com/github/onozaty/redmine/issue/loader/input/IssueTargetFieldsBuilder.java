package com.github.onozaty.redmine.issue.loader.input;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class IssueTargetFieldsBuilder {

    private Map<String, Object> updateTargetFields = new LinkedHashMap<>(); // テスト時に順序を保証したいので

    public IssueTargetFieldsBuilder field(FieldType type, String value) {

        updateTargetFields.put(type.getFieldName(), value);
        return this;
    }

    public IssueTargetFieldsBuilder customField(CustomField customField) {

        @SuppressWarnings("unchecked")
        List<CustomField> customFields = (List<CustomField>) updateTargetFields.get("custom_fields");

        if (customFields == null) {
            customFields = new ArrayList<>();
            updateTargetFields.put("custom_fields", customFields);
        }

        customFields.add(customField);

        return this;
    }

    public Map<String, Object> build() {
        return updateTargetFields;
    }
}
