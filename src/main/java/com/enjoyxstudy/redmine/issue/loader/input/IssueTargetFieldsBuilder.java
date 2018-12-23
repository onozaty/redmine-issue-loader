package com.enjoyxstudy.redmine.issue.loader.input;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IssueTargetFieldsBuilder {

    private Map<String, Object> updateTargetFields = new HashMap<>();

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
