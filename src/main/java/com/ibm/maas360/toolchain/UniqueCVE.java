package com.ibm.maas360.toolchain;

import com.ibm.maas360.toolchain.EvidenceFolderReader;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UniqueCVE
{

    public static void main(String[] args) {
        Map<String, Map<String, Object>> result = new HashMap<>();
        System.out.println("Getting unique cve from all the vulnerable images:");

        String repoPath = "evidences";

        try {
            List<Path> csvFiles = EvidenceFolderReader.findAndCopyCSVFiles(repoPath);
            result = EvidenceFolderReader.prepareContentForUnifiedCSVFile(csvFiles);
        } catch (IOException e) {
            System.err.println(e);
        }

        writeResultToFinalCSVFile(result);
    }

    private static void writeResultToFinalCSVFile(Map<String, Map<String, Object>> result) {
        String outputPath = "result/unique.csv";
        List<String> headers = Arrays.asList("cve", "images", "imageTag", "hasFix", "highestSeverity", "cveLastModified", "cveReferences", "artifactNames", "artifactTypes", "vulnerableVersions", "fixedVersions");
        Path outputPath1 = Paths.get(outputPath);

        try (BufferedWriter writer = Files.newBufferedWriter(outputPath1, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
            writer.write(String.join(",", headers));
            writer.newLine();

            for (Map.Entry<String, Map<String, Object>> entry : result.entrySet()) {
                String cve = entry.getKey();
                Map<String, Object> details = entry.getValue();
                writer.write(String.format("%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s",
                        cve,
                        String.join("|", (List<String>) details.get("images")),
                        details.get("imageTag"),
                        details.get("hasFix"),
                        details.get("highestSeverity"),
                        details.get("cveLastModified"),
                        details.get("cveReferences"),
                        details.get("artifactNames"),
                        details.get("artifactTypes"),
                        details.get("vulnerableVersions"),
                        details.get("fixedVersions")));
                writer.newLine();
            }
            System.out.println("Unique cve file for vulnerable images generated.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
