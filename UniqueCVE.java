import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;

public class CSVProcessor {

    public static void main(String[] args) {
        System.out.println("Getting unique cve from all the vulnerable images:");
        Map<String, Map<String, Object>> result = new HashMap<>();

        String repoPath = "../../../../../evidences";

        try {
            List<Path> csvFiles = findAndCopyCSVFiles(Paths.get(repoPath));

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
        } catch (IOException e) {
            System.err.println(e);
        }

        String outputPath = "../../../../../result/unique.csv";
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

    private static List<Path> findAndCopyCSVFiles(Path sourceDir) throws IOException {
        List<Path> csvFiles = new ArrayList<>();

        System.out.println("Searching for CSV files in: " + sourceDir);
        Files.walkFileTree(sourceDir, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                if (file.toString().endsWith(".csv")) {
                    try {
                        csvFiles.add(file);
                    } catch (IOException e) {
                        System.err.println("Failed to copy file: " + file);
                        e.printStackTrace();
                    }
                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                return FileVisitResult.CONTINUE;
            }
        });

        return csvFiles;
    }
}
