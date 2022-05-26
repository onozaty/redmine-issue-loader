package com.github.onozaty.redmine.issue.loader.input;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.github.onozaty.redmine.issue.loader.client.QueryParameter;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CustomField implements PrimaryKey {

    private int id;

    /**
     * カスタムフィールドの値です。
     * 複数選択の場合、リストが入ります。
     */
    private Object value;

    @Override
    public QueryParameter getQueryParameter() {
        // カスタムフィールドの「フィルタとして使用」が有効となっている必要あり
        return new QueryParameter("cf_" + id, String.valueOf(value));
    }
}
