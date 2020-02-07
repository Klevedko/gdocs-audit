package com.google.drive.api.Google;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.drive.controller.UploadController;

import java.io.File;
import java.io.FileInputStream;
import java.util.Collections;
import java.util.List;

import java.io.IOException;

public class GoogleDriveUtils {
    private static final String DATA_STORE_DIR = System.getProperty("user.dir") + "/tokens/token_Google";

    private static final String APPLICATION_NAME = "Google Drive API Java Quickstart";

    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

    // Directory to store user credentials for this application.
    //private static final java.io.File CREDENTIALS_FOLDER //
    //            = new java.io.File(System.getProperty("user.dir"), "/token_Google");

    private static final List<String> SCOPES = Collections.singletonList(DriveScopes.DRIVE);

    // Global instance of the {@link FileDataStoreFactory}.
    private static FileDataStoreFactory DATA_STORE_FACTORY;

    // Global instance of the HTTP transport.
    private static HttpTransport HTTP_TRANSPORT;

    private static Drive _driveService;

    static {
        try {
            HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
            //DATA_STORE_FACTORY = new FileDataStoreFactory(new java.io.File(DATA_STORE_DIR));
        } catch (Throwable t) {
            t.printStackTrace();
            System.exit(1);
        }
    }

    private static Credential getCredential() {
        try {
            //return GoogleCredential.fromStream(Thread.currentThread().getContextClassLoader().getResourceAsStream(UploadController.path.toString()))
            return GoogleCredential.fromStream(new FileInputStream(new File(UploadController.path.toString())))
                    .createScoped(SCOPES);
        } catch (IOException e) {
            throw new RuntimeException("Cannot be authorized in Google account", e);
        }
    }

    public static Drive getDriveService() throws IOException {
        if (_driveService != null) {
            return _driveService;
        }
        Credential credential = getCredential();
        //
        _driveService = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential) //
                .setApplicationName(APPLICATION_NAME).build();
        return _driveService;
    }

}