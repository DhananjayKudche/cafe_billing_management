package com.kudche.cafebillingmanagement.Utils;

import android.content.Context;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class GoogleDriveHelper {

    private final Drive driveService;

    public GoogleDriveHelper(Context context, String accountName) {
        GoogleAccountCredential credential = GoogleAccountCredential.usingOAuth2(
                context, Collections.singleton(DriveScopes.DRIVE_FILE));
        credential.setSelectedAccountName(accountName);

        driveService = new Drive.Builder(
                new NetHttpTransport(),
                new GsonFactory(),
                credential)
                .setApplicationName("Cafe Billing Management")
                .build();
    }

    public String getOrCreateFolder(String folderName, String parentId) throws IOException {
        String query = "name = '" + folderName + "' and mimeType = 'application/vnd.google-apps.folder' and trashed = false";
        if (parentId != null) {
            query += " and '" + parentId + "' in parents";
        }

        FileList result = driveService.files().list()
                .setQ(query)
                .setSpaces("drive")
                .setFields("files(id, name)")
                .execute();

        if (!result.getFiles().isEmpty()) {
            return result.getFiles().get(0).getId();
        }

        File folderMetadata = new File();
        folderMetadata.setName(folderName);
        folderMetadata.setMimeType("application/vnd.google-apps.folder");
        if (parentId != null) {
            folderMetadata.setParents(Collections.singletonList(parentId));
        }

        File folder = driveService.files().create(folderMetadata)
                .setFields("id")
                .execute();
        return folder.getId();
    }

    public void uploadFile(java.io.File localFile, String mimeType, String folderId) throws IOException {
        // First, check if file with same name exists in this folder to update it
        String query = "name = '" + localFile.getName() + "' and '" + folderId + "' in parents and trashed = false";
        FileList result = driveService.files().list().setQ(query).setFields("files(id)").execute();

        File fileMetadata = new File();
        fileMetadata.setName(localFile.getName());
        FileContent mediaContent = new FileContent(mimeType, localFile);

        if (!result.getFiles().isEmpty()) {
            // Update existing
            String fileId = result.getFiles().get(0).getId();
            driveService.files().update(fileId, null, mediaContent).execute();
        } else {
            // Create new
            fileMetadata.setParents(Collections.singletonList(folderId));
            driveService.files().create(fileMetadata, mediaContent).execute();
        }
    }

    public String downloadFileContent(String fileName, String folderId) throws IOException {
        String query = "name = '" + fileName + "' and '" + folderId + "' in parents and trashed = false";
        FileList result = driveService.files().list().setQ(query).setFields("files(id)").execute();

        if (result.getFiles().isEmpty()) return null;

        String fileId = result.getFiles().get(0).getId();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        driveService.files().get(fileId).executeMediaAndDownloadTo(outputStream);
        return outputStream.toString();
    }
}