package com.ibm.maas360.toolchain.github;

import org.kohsuke.github.GHIssue;
import org.kohsuke.github.GHIssueState;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

public class GitHubApiWrapper
{
    private static final Logger logger = Logger.getLogger(GitHubApiWrapper.class.getSimpleName());

    private static final String GITHUB_ENTERPRISE_URL = "https://github.ibm.com/api/v3";
    private static final String GITHUB_TOKEN = "<Github token>";

    private final GitHub github;

    public GitHubApiWrapper() throws IOException
    {
        this.github = new GitHubBuilder()
                .withEndpoint(GITHUB_ENTERPRISE_URL)
                .withOAuthToken(GITHUB_TOKEN)
                .build();
    }

    public List<GHIssue> queryOpenIssuesForRepository(String repositoryName) throws IOException
    {
        logger.info("Querying Open Issues for repository " + repositoryName);

        List<GHIssue> issueList = github.getRepository(repositoryName).getIssues(GHIssueState.OPEN);

        logger.info("Count of Open Issues found " + issueList.size());
        return issueList;
    }

    public void assignIssueToUser(GHIssue ghIssue, String gitUserName) throws IOException
    {
        logger.info("Assigning issue #" + ghIssue.getNumber() + " to " + ghIssue + "...");
        ghIssue.assignTo(github.getUser(gitUserName));
    }
}
