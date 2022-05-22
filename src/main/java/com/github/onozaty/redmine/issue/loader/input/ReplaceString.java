package com.github.onozaty.redmine.issue.loader.input;

import org.apache.commons.lang3.StringUtils;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class ReplaceString {

    private String pattern;

    @Default
    private String replacement = "";

    public String replace(String target) {

        if (StringUtils.isEmpty(target) || StringUtils.isEmpty(pattern)) {
            return target;
        }

        return target.replaceAll(pattern, replacement);
    }
}
