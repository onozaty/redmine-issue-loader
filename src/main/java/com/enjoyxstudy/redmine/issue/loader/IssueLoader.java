package com.enjoyxstudy.redmine.issue.loader;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.enjoyxstudy.redmine.issue.loader.client.Client;
import com.enjoyxstudy.redmine.issue.loader.client.Issue;
import com.enjoyxstudy.redmine.issue.loader.client.QueryParameter;
import com.enjoyxstudy.redmine.issue.loader.input.IssueId;
import com.enjoyxstudy.redmine.issue.loader.input.PrimaryKey;

import lombok.Value;

@Value
public class IssueLoader {

    private static final QueryParameter ALL_STATUS_QUERY = new QueryParameter("status_id", "*");

    private final Client client;

    public IssueId create(Map<String, Object> targetFields) throws IOException {

        // 新規作成
        int issueId = client.createIssue(targetFields);

        return new IssueId(issueId);
    }

    public IssueId update(PrimaryKey key, Map<String, Object> targetFields) throws IOException {

        // キーとなる情報を使ってIssueを検索
        List<Issue> targetIssues = client.getIssues(
                Arrays.asList(
                        ALL_STATUS_QUERY, // 終了しているチケットも対象にするため指定
                        key.getQueryParameter()));

        // 1件ではない場合はエラー
        if (targetIssues.size() == 0) {
            throw new IllegalStateException("The target issue was not found. " + key);
        } else if (targetIssues.size() > 1) {
            throw new IllegalStateException("There are multiple target issue. " + key);
        }

        int targetIssueId = targetIssues.get(0).getId();

        // 内容更新
        client.updateIssue(targetIssueId, targetFields);

        // 更新対象となったIssueIdを返却
        return new IssueId(targetIssueId);
    }
}
