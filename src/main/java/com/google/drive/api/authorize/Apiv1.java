package com.google.drive.api.authorize;


import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.appsactivity.Appsactivity;
import com.google.api.services.appsactivity.AppsactivityScopes;
import com.google.api.services.appsactivity.model.ListActivitiesResponse;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import static com.google.drive.Reports.DynamicReport.service;



public class Apiv1 {
    private static final String APPLICATION_NAME = "G Suite Activity API Java Quickstart";
    private static final JsonFactory JSON_FACTORY =
            JacksonFactory.getDefaultInstance();
    private static HttpTransport HTTP_TRANSPORT;
    private static final List<String> SCOPES = Arrays.asList(AppsactivityScopes.ACTIVITY);

    static {
        try {
            HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
            //DATA_STORE_FACTORY = new FileDataStoreFactory(new java.io.File(DATA_STORE_DIR));
        } catch (Throwable t) {
            System.exit(1);
        }
    }

    private static Credential getCredential() {
        try {
            return GoogleCredential.fromStream(Thread.currentThread()
                    .getContextClassLoader().getResourceAsStream("serviceAccount.json"))
                    .createScoped(SCOPES);
        } catch (IOException e) {
            throw new RuntimeException("Cannot be authorized in Google account", e);
        }
    }
    public static ListActivitiesResponse get_driveservice_v1_activities(String query) {
        try {
            return service.activities().list().setSource("drive.google.com").setDriveAncestorId(query).execute();
        } catch (Exception x) {
            throw new RuntimeException("get_driveservice_v1_activities =", x);
        }
    }
    public static Appsactivity getAppsactivityService() throws IOException {
        Credential active_credential = getCredential();
        return new Appsactivity.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, active_credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }
}
