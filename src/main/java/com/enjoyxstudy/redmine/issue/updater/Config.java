package com.enjoyxstudy.redmine.issue.updater;

import java.util.List;

import lombok.Data;

@Data
public class Config {

    private String readmineUrl;

    private String apyKey;

    private String csvEncoding;
    
    private List<FieldSetting> fields;
}
