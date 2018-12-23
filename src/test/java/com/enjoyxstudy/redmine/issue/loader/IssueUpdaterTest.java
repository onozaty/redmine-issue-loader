package com.enjoyxstudy.redmine.issue.loader;

import java.io.IOException;

import org.junit.Test;

import com.enjoyxstudy.redmine.issue.loader.IssueUpdater;
import com.enjoyxstudy.redmine.issue.loader.client.Client;
import com.enjoyxstudy.redmine.issue.loader.input.CustomField;
import com.enjoyxstudy.redmine.issue.loader.input.IssueTargetFieldsBuilder;

public class IssueUpdaterTest {

    @Test
    public void test() throws IOException {

        // 動作確認だけ
        Client client = Client.builder()
                .redmineBaseUrl("http://192.168.33.10/")
                .apiKey("20d0779f947c3c9a7248332a078ff458644ed73d")
                .build();

        IssueUpdater updater = new IssueUpdater(client);

        updater.update(
                new CustomField(1, "C"),
                new IssueTargetFieldsBuilder()
                        .customField(new CustomField(2, "xxx"))
                        .customField(new CustomField(3, "yyy"))
                        .build());
    }

}
