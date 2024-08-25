package com.ibm.maas360.toolchain;


import com.ibm.maas360.toolchain.github.GitHubApiWrapper;
import org.kohsuke.github.GHIssue;

import java.io.IOException;
import java.util.List;

public class GitHubIssueLabeler {

    private static final String LABEL_REPOSITORY_NAME = "maas360-hosted/maas360-issues";

    public static void main(String[] args) {
        try {
            GitHubApiWrapper apiWrapper = new GitHubApiWrapper();
            List<GHIssue> issues = apiWrapper.queryOpenIssuesForRepository(LABEL_REPOSITORY_NAME);

            for (GHIssue issue : issues) {
                apiWrapper.processMarkdownAndLabelIssue(issue);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
