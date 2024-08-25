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


 public void processMarkdownAndLabelIssue(GHIssue issue) throws IOException {
        String markdown = extractMarkdownFromIssue(issue); // Implement this method to extract the markdown
        List<String> labels = extractLabelsFromMarkdown(markdown);
        applyLabelsToIssue(issue, labels);
    }

    private String extractMarkdownFromIssue(GHIssue issue) throws IOException {
        // Assuming GHIssue has a method to get comments
        List<GHIssueComment> comments = issue.getComments();
        StringBuilder markdown = new StringBuilder();

        for (GHIssueComment comment : comments) {
            markdown.append(comment.getBody()).append("\n\n"); // Append each comment body with spacing
        }
        String markdownContent = markdown.toString().trim(); // Convert to string and trim extra whitespace
        if (markdownContent.isEmpty()) {
            throw new IllegalArgumentException("No markdown content found in issue comments.");
        }
        return markdownContent; // Return the combined markdown content
    }


    private List<String> extractLabelsFromMarkdown(String markdown) {
        List<String> labels = new ArrayList<>();

        // Split the markdown into lines
        String[] lines = markdown.split("\n");

        // Find the header line and the first data line
        boolean headerFound = false;
        String firstDataRow = null;

        for (String line : lines) {
            // Identify the header line
            if (line.startsWith("|") && !headerFound) {
                headerFound = true; // Mark header as found
                continue; // Skip to the next line
            }

            // If header is found, look for the first data line
            if (headerFound) {
                if (line.startsWith("|")) {
                    firstDataRow = line; // Capture the first data row
                    break; // Exit the loop after finding the first data row
                }
            }
        }

        // If a first data row was found, process it
        if (firstDataRow != null) {
            String[] columns = firstDataRow.split("\\|"); // Split by pipe character

            // Extract values for highestSeverity and artifactTypes
            String highestSeverity = null;
            String artifactType = null;

            // Assuming the columns follow the specified order in your markdown table
            for (int i = 0; i < columns.length; i++) {
                String value = columns[i].trim(); // Trim whitespace
                if (value.equalsIgnoreCase("highestSeverity")) {
                    highestSeverity = columns[i + 1].trim(); // Next column for value
                } else if (value.equalsIgnoreCase("artifactTypes")) {
                    artifactType = columns[i + 1].trim(); // Next column for value
                }
            }

            labels.add(highestSeverity); // Add the highestSeverity label
            labels.add(artifactType); // Add the artifactType label

            // Log extracted values for verification
            logger.info("Extracted values - Highest Severity: " + highestSeverity + ", Artifact Type: " + artifactType);
        } else {
            logger.severe("Markdown table does not contain expected data.");
        }

        return labels;
    }



    private void applyLabelsToIssue(GHIssue issue, List<String> labels) throws IOException {
        logger.info("Applying labels to issue #" + issue.getNumber() + ": " + labels);
        issue.addLabels(labels.toArray(new String[0])); // Convert List to array
    }
        }
