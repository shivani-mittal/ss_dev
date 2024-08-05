package com.ibm.maas360.toolchain;

import com.ibm.maas360.toolchain.github.GitHubApiWrapper;
import com.ibm.maas360.toolchain.twistlock.TwistlockAppOwnerFileReader;
import org.kohsuke.github.GHIssue;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class IssueAssigner {
    private static final String ISSUE_REPOSITORY_NAME = "maas360-hosted/twistlock-issues";
    private static final Logger logger = Logger.getLogger(IssueAssigner.class.getName());


    public static void main(String[] args) {
        try {

            GitHubApiWrapper gitHubApiWrapper = new GitHubApiWrapper();

            Map<String, String> appNameOwnerMap = TwistlockAppOwnerFileReader.loadAppOwnersFromLog
                    (TwistlockAppOwnerFileReader.APP_OWNER_MAPPING_FILE);

            List<GHIssue> issues = gitHubApiWrapper.queryOpenIssuesForRepository(ISSUE_REPOSITORY_NAME);

            for (GHIssue issue : issues) {

                logger.info("Processing issue #" + issue.getNumber());
                String issueBody = issue.getBody();

                Set<String> appOwnersToAssign = findAllAppOwnersForIssue(issueBody, appNameOwnerMap);

                if (!appOwnersToAssign.isEmpty()) {
                    for (String owner : appOwnersToAssign) {
                        gitHubApiWrapper.assignIssueToUser(issue, owner);
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

    private static Set<String> findAllAppOwnersForIssue(String issueBody, Map<String, String> appNameOwnerMap)
    {
        Set<String> assignedOwners = new HashSet<>();

        for (Map.Entry<String, String> entry : appNameOwnerMap.entrySet()) {
            String appName = entry.getKey();
            String owner = entry.getValue();

            // Check if the issue body contains the app name
            if (issueBody != null && issueBody.contains(appName)) {
                assignedOwners.add(owner); // Add the owner to the set
            }
        }
        return assignedOwners;
    }
}