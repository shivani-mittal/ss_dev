import org.kohsuke.github.GHIssue;
import org.kohsuke.github.GHIssueState;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class IssueAssigner {
    private static final String GITHUB_ENTERPRISE_URL = "https://github.ibm.com/api/v3";
    private static final String GITHUB_TOKEN = "6fc8d0904a51b022e1bf45d0d5b9e0fe78c2a300";
    private static final String REPOSITORY_NAME = "maas360-hosted/twistlock-issues";
    private static final String LOG_FILE_PATH = "AppOwners.log";
    private static final Logger logger = Logger.getLogger(IssueAssigner.class.getName());


    // Replace this with your list of app names and owners
    private static final Map<String, String> appOwners = new HashMap<>();

    public static void main(String[] args) {
        try {
            loadAppOwnersFromLog(LOG_FILE_PATH);
            GitHub github = new GitHubBuilder()
                    .withEndpoint(GITHUB_ENTERPRISE_URL)
                    .withOAuthToken(GITHUB_TOKEN)
                    .build();
            List<GHIssue> issues = github.getRepository(REPOSITORY_NAME).getIssues(GHIssueState.OPEN);

            for (GHIssue issue : issues) {
                logger.info("Processing issue #" + issue.getNumber());
                String issueBody = issue.getBody();

                Set<String> assignedOwners = new HashSet<>();

                for (Map.Entry<String, String> entry : appOwners.entrySet()) {
                    String appName = entry.getKey();
                    String owner = entry.getValue();

                    // Check if the issue body contains the app name
                    if (issueBody != null && issueBody.contains(appName)) {
                        assignedOwners.add(owner); // Add the owner to the set
                    }
                }

                if (!assignedOwners.isEmpty()) {
                    // Assign the issue to all owners for each app found
                    for (String owner : assignedOwners) {
                        logger.info("Assigning issue #" + issue.getNumber() + " to " + owner + "...");
                        issue.assignTo(github.getUser(owner));
                    }
                    logger.info("Assigned issue #" + issue.getNumber() + " to all relevant owners.");
                } else {
                    logger.info("No app names found in issue title #" + issue.getNumber() + ". No assignment made.");
                }
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "An error occurred while processing issues.", e);
        }
    }

    private static void loadAppOwnersFromLog(String filePath) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                // Assuming the log file format is "AppName,Owner"
                String[] parts = line.split(","); // Change delimiter if necessary
                if (parts.length == 2) {
                    String appName = parts[0].trim();
                    String owner = parts[1].trim();
                    appOwners.put(appName, owner);
                } else {
                    logger.warning("Skipping invalid line in log file: " + line);
                }
            }
        }
    }
}