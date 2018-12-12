package com.enjoyxstudy.redmine.issue.updater;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Test;

public class IssueUpdateRunnerTest {

    @Test
    public void test1() throws URISyntaxException, IOException {

        // 動作確認用(キーがチケットID)
        Path configPath = Paths.get(IssueUpdateRunnerTest.class.getResource("config1.json").toURI());
        Path csvPath = Paths.get(IssueUpdateRunnerTest.class.getResource("issues.csv").toURI());

        IssueUpdateRunner runner = new IssueUpdateRunner(System.out);
        runner.execute(configPath, csvPath);
    }

    @Test
    public void test2() throws URISyntaxException, IOException {

        // 動作確認用(キーがカスタムフィールド)
        Path configPath = Paths.get(IssueUpdateRunnerTest.class.getResource("config2.json").toURI());
        Path csvPath = Paths.get(IssueUpdateRunnerTest.class.getResource("issues.csv").toURI());

        IssueUpdateRunner runner = new IssueUpdateRunner(System.out);
        runner.execute(configPath, csvPath);
    }

}
