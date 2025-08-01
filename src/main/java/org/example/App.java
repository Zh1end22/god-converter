package org.example;

import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.errors.MinioException;

import org.jodconverter.local.LocalConverter;
import org.jodconverter.local.office.LocalOfficeManager;
import org.jodconverter.core.DocumentConverter;
import org.jodconverter.core.office.OfficeException;
import org.jodconverter.core.office.OfficeManager;

import java.io.File;
import java.io.FileInputStream;

public class App {

    public static void main(String[] args) throws Exception {
        // 1) ARG CHECK
        if (args.length < 8) {
            System.err.println("Usage: java -jar godconverter.jar "
                    + "--input <inbox-folder> "
                    + "--endpoint <minio-url> "
                    + "--accessKey <your-key> "
                    + "--secretKey <your-secret>");
            System.exit(1);
        }

        // 2) READ ARGS
        String inputDir  = args[1];
        String endpoint  = args[3];
        String accessKey = args[5];
        String secretKey = args[7];
        String bucket    = "godconverter";

        // 3) PREPARE FOLDERS
        File inFolder  = new File(inputDir);
        File outFolder = new File("tmp_converted");
        outFolder.mkdirs();

        // 3a) VERIFY the inbox exists
        if (!inFolder.exists() || !inFolder.isDirectory()) {
            System.err.printf("Error: inbox folder '%s' does not exist or is not a directory.%n", inputDir);
            System.exit(2);
        }

        // 4) START LIBREOFFICE
        OfficeManager officeManager = LocalOfficeManager.install();
        officeManager.start();

        // 5) CREATE CONVERTER
        DocumentConverter converter = LocalConverter.make(officeManager);

        // 6) INIT MINIO CLIENT
        MinioClient minio = MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .build();

        // 7) PROCESS FILES
        File[] files = inFolder.listFiles();
        if (files == null || files.length == 0) {
            System.out.println("No files found in inbox; nothing to do.");
        } else {
            for (File f : files) {
                String name = f.getName();
                String ext  = name.substring(name.lastIndexOf('.') + 1).toLowerCase();

                if ("pdf".equals(ext)) {
                    System.out.println("Skipping already-PDF: " + name);
                    continue;
                }

                File pdf = new File(outFolder, name + ".pdf");
                try {
                    converter.convert(f).to(pdf).execute();
                    System.out.println("Converted: " + name);
                } catch (OfficeException e) {
                    System.err.println("Conversion failure: " + name);
                    continue;
                }

                try (FileInputStream fis = new FileInputStream(pdf)) {
                    minio.putObject(
                            PutObjectArgs.builder()
                                    .bucket(bucket)
                                    .object(pdf.getName())
                                    .stream(fis, pdf.length(), -1)
                                    .contentType("application/pdf")
                                    .build()
                    );
                    System.out.println("Uploaded: " + pdf.getName());
                } catch (MinioException me) {
                    System.err.println("Upload failure: " + pdf.getName() + " → " + me.getMessage());
                }
            }
        }

        // 8) SHUTDOWN
        officeManager.stop();
    }
}
