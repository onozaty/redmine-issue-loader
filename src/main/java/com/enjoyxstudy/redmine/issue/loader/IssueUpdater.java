package com.enjoyxstudy.redmine.issue.loader;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.enjoyxstudy.redmine.issue.loader.client.Client;
import com.enjoyxstudy.redmine.issue.loader.client.Issue;
import com.enjoyxstudy.redmine.issue.loader.input.IssueId;
import com.enjoyxstudy.redmine.issue.loader.input.PrimaryKey;

import lombok.Value;

@Value
public class IssueUpdater {

    private final Client client;

    public IssueId update(PrimaryKey key, Map<String, Object> updateTargetFields) throws IOException {

        // キーとなる情報を使ってIssueを検索
        List<Issue> targetIssues = client.getIssues(key.getQueryParameter());

        // 1件ではない場合はエラー
        if (targetIssues.size() == 0) {
            throw new IllegalStateException("The target issue was not found. " + key);
        } else if (targetIssues.size() > 1) {
            throw new IllegalStateException("There are multiple target issue. " + key);
        }

        int targetIssueId = targetIssues.get(0).getId();

        // 内容更新
        client.updateIssue(targetIssueId, updateTargetFields);

        // 更新対象となったIssueIdを返却
        return new IssueId(targetIssueId);
    }
}
