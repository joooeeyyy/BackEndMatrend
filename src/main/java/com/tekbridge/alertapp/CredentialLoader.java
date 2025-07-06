package com.tekbridge.alertapp;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
public class CredentialLoader {

    public static void loadCredentialsFromResources() throws IOException {
        InputStream inputStream = CredentialLoader.class.getResourceAsStream("/service-account.json");
        if (inputStream == null) {
            throw new FileNotFoundException("service-account.json not found in resources.");
        }else{
            System.out.println("FOUNDDDDDDDDDDDDDDDDDDDDDD");
        }

        // Copy to a temp file
        File tempFile = File.createTempFile("gcp-credentials", ".json");
        tempFile.deleteOnExit();
        try (FileOutputStream out = new FileOutputStream(tempFile)) {
            inputStream.transferTo(out);
        }

        // Set system property to the extracted file path
        System.setProperty("GOOGLE_APPLICATION_CREDENTIALS", tempFile.getAbsolutePath());
    }

}
