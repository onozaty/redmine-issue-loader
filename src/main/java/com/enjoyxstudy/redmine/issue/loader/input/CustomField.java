package com.enjoyxstudy.redmine.issue.loader.input;

import com.enjoyxstudy.redmine.issue.loader.client.QueryParameter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CustomField implements PrimaryKey {

    private int id;

    private String value;

    @Override
    public QueryParameter getQueryParameter() {
        // カスタムフィールドの「フィルタとして使用」が有効となっている必要あり
        return new QueryParameter("cf_" + id, value);
    }
}
