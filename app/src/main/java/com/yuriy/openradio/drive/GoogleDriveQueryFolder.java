package com.yuriy.openradio.drive;

import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveId;

/**
 * Created by Chernyshov Yurii
 * At Android Studio
 * On 06/07/17
 * E-Mail: chernyshov.yuriy@gmail.com
 */
final class GoogleDriveQueryFolder extends GoogleDriveQueryDrive {

    GoogleDriveQueryFolder() {
        this(false);
    }

    GoogleDriveQueryFolder(final boolean isTerminator) {
        super(isTerminator);
    }

    @Override
    protected DriveFolder getDriveFolder(final GoogleDriveRequest request, final GoogleDriveResult result) {
        return Drive.DriveApi.getRootFolder(request.getGoogleApiClient());
    }

    @Override
    protected String getName(final GoogleDriveRequest request) {
        return request.getFolderName();
    }

    @Override
    protected void setResult(final GoogleDriveResult result, final DriveId driveId) {
        result.setFolder(driveId.asDriveFolder());
    }
}
