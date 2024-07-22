package com.ibm

import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser
import org.apache.commons.csv.CSVRecord

import java.nio.file.*
import java.nio.file.attribute.BasicFileAttributes

static void main(String[] args) {
    println "Getting unique cve from all the vulnerable images:"
    Map<String, Map<String, Object>> result = [:]

    String repoPath = "../../../../../evidences"

    try {
        List<Path> csvFiles = findAndCopyCSVFiles(Paths.get(repoPath))

        csvFiles.each { Path csvFile ->
            println "Reading file: $csvFile"
            try {
                BufferedReader br = new BufferedReader(new FileReader(csvFile.toFile()))
                CSVParser parser = CSVFormat.DEFAULT.withDelimiter(',' as char).withHeader().parse(br)
                parser.records.each { CSVRecord record ->
                    String cve = record.get("cve")
                    String image = record.get("image").split("-\\d")[0]

                    if (result.containsKey(cve)) {
                        def details = result[cve]
                        if(!((ArrayList<String>)details["images"]).contains(image)){
                        details["images"].add(image)
                            }
                    } else {
                        result[cve] = [
                                "images": [image],
                                "hasFix": record.get("hasFix")!=null?record.get("hasFix"):"",
                                "cveLastModified": record.get("cveLastModified")!=null?record.get("cveLastModified"):"",
                                "cveReferences": record.get("cveReferences")!=null?record.get("cveReferences"):"",
                                "highestSeverity": record.get("highestSeverity")!=null?record.get("highestSeverity"):"",
                                "artifactNames": record.get("artifactNames")!=null?record.get("artifactNames"):"",
                                "artifactTypes": record.get("artifactTypes")!=null?record.get("artifactTypes"):"",
                                "vulnerableVersions": record.get("vulnerableVersions")!=null?record.get("vulnerableVersions"):"",
                                "fixedVersions": record.get("fixedVersions")!=null?record.get("fixedVersions"):""
                        ]
                    }
                }
            } catch (Exception e) {
                println(e)
            }
        }
    } catch (IOException e) {
        println(e)
    }

    String outputPath = "../../../../../result/unique.csv"
    List<String> headers = ["cve", "images", "hasFix", "highestSeverity", "cveLastModified","cveReferences","artifactNames","artifactTypes","vulnerableVersions","fixedVersions"]
    Path outputPath1 = Paths.get(outputPath)

    try (BufferedWriter writer = Files.newBufferedWriter(outputPath1, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
        writer.write(headers.join(','))
        writer.newLine()

        result.each { cve, details ->
            writer.write("${cve},${details["images"].join('|')},${details["hasFix"]},${details["highestSeverity"]},${details["cveLastModified"]},${details["cveReferences"]},${details["artifactNames"]},${details["artifactTypes"]},${details["vulnerableVersions"]},${details["fixedVersions"]}")
            writer.newLine()
        }
        println "Unique cve file for vulnerable images generated."
    } catch (Exception e) {
        e.printStackTrace()
    }
}

static List<Path> findAndCopyCSVFiles(Path sourceDir) throws IOException {
    List<Path> csvFiles = []

    println "Searching for CSV files in: $sourceDir"
    Files.walkFileTree(sourceDir, new SimpleFileVisitor<Path>() {
        @Override
        FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            println "Found file: $file"
            if (file.toString().endsWith(".csv")) {
                try {
                    csvFiles.add(file)
                } catch (IOException e) {
                    println("Failed to copy file: $file")
                    e.printStackTrace()
                }
            }
            return FileVisitResult.CONTINUE
        }

        @Override
        FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
            return FileVisitResult.CONTINUE
        }
    })

    return csvFiles
}