package com.google.drive.api.authorize;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.FileList;
import com.google.drive.controller.UploadController;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;

public class Apiv3 {
    private static final String APPLICATION_NAME = "Google Drive API Java Quickstart";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

    private static final List<String> SCOPES = Collections.singletonList(DriveScopes.DRIVE);

    private static Credential getCredential() {
        try {
            return GoogleCredential.fromStream(new FileInputStream(new File(UploadController.path.toString())))
                    //return GoogleCredential.fromStream(Thread.currentThread().getContextClassLoader().getResourceAsStream(UploadController.path.toString()))
                    .createScoped(SCOPES);
        } catch (IOException e) {
            throw new RuntimeException("Cannot be authorized in Google account", e);
        }
    }
    public static FileList get_driveservice_v3_files(Drive driveservice, String query) {
        try {
            return driveservice.files().list().setQ(query).setFields("nextPageToken, " +
                    "files(id, parents, name, webViewLink, mimeType)").execute();
            //, sharingUser(emailAddress, permissionId)
        } catch (Exception x) {
            System.out.println(x);
            throw new RuntimeException("Cannot get_driveservice_v3_files = {}", x.getCause());
        }
    }
    public static Drive Drive() throws IOException, GeneralSecurityException {
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        Drive service = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredential())
                .setApplicationName(APPLICATION_NAME)
                .build();
        return service;
    }
}