package com.ibm.maas360.toolchain;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;

public class EvidenceFolderReader
{
    public static List<Path> findAndCopyCSVFiles(String repoPath) throws IOException {

        Path sourceDir = Paths.get(repoPath);
        List<Path> csvFiles = new ArrayList<>();

        System.out.println("Searching for CSV files in: " + sourceDir);
        Files.walkFileTree(sourceDir, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)  {
                if (file.toString().endsWith(".csv")) {
                    csvFiles.add(file);
                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                return FileVisitResult.CONTINUE;
            }
        });

        return csvFiles;
    }

    public static Map<String, Map<String, Object>> prepareContentForUnifiedCSVFile(List<Path> csvFiles) {
        Map<String, Map<String, Object>> result = new HashMap<>();
        for (Path csvFile : csvFiles) {
            try (BufferedReader br = new BufferedReader(new FileReader(csvFile.toFile()));
                 CSVParser parser = CSVFormat.DEFAULT.withDelimiter(',').withHeader().parse(br)) {

                for (CSVRecord record : parser) {
                    String cve = record.get("cve");
                    String image = record.get("image").split("-\\d")[0];

                    if (result.containsKey(cve)) {
                        Map<String, Object> details = result.get(cve);
                        List<String> images = (List<String>) details.get("images");
                        if (!images.contains(image)) {
                            images.add(image);
                        }
                    } else {
                        Map<String, Object> details = new HashMap<>();
                        details.put("images", new ArrayList<>(Collections.singletonList(image)));
                        details.put("hasFix", record.get("hasFix") != null ? record.get("hasFix") : "");
                        details.put("imageTag", record.get("imageTag") != null ? record.get("imageTag") : "");
                        details.put("cveLastModified", record.get("cveLastModified") != null ? record.get("cveLastModified") : "");
                        details.put("cveReferences", record.get("cveReferences") != null ? record.get("cveReferences") : "");
                        details.put("highestSeverity", record.get("highestSeverity") != null ? record.get("highestSeverity") : "");
                        details.put("artifactNames", record.get("artifactNames") != null ? record.get("artifactNames") : "");
                        details.put("artifactTypes", record.get("artifactTypes") != null ? record.get("artifactTypes") : "");
                        details.put("vulnerableVersions", record.get("vulnerableVersions") != null ? record.get("vulnerableVersions") : "");
                        details.put("fixedVersions", record.get("fixedVersions") != null ? record.get("fixedVersions") : "");

                        result.put(cve, details);
                    }
                }
            } catch (Exception e) {
                System.err.println(e);
            }
        }
        return result;
    }
}
