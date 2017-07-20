/*
 * Copyright 2017 The "Open Radio" Project. Author: Chernyshov Yuriy
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.yuriy.openradio.drive;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.Drive;
import com.yuriy.openradio.api.RadioStationVO;
import com.yuriy.openradio.service.FavoritesStorage;
import com.yuriy.openradio.service.LocalRadioStationsStorage;
import com.yuriy.openradio.utils.AppLogger;
import com.yuriy.openradio.utils.QueueHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Chernyshov Yurii
 * At Android Studio
 * On 03/07/17
 * E-Mail: chernyshov.yuriy@gmail.com
 */
public final class GoogleDriveManager {

    /**
     * Listener for the Google Drive client events.
     */
    public interface Listener {

        /**
         * Google Drive client start to connect.
         */
        void onConnect();

        /**
         * Google Drive client connected.
         */
        void onConnected();

        /**
         * Google Drive client failed with {@link ConnectionResult}. Typically this is means to perform another
         * actions based on the result, such as show Auth window or select user to associate with Google Drive client.
         *
         * @param connectionResult
         */
        void onConnectionFailed(final ConnectionResult connectionResult);

        /**
         * Google Drive client start to perform command, such as {@link Command#UPLOAD} or {@link Command#DOWNLOAD}.
         *
         * @param command Command which is started.
         */
        void onStart(final GoogleDriveManager.Command command);

        /**
         * Google Drive successfully completed to perform command,
         * such as {@link Command#UPLOAD} or {@link Command#DOWNLOAD}.
         *
         * @param command Command which is completed.
         */
        void onSuccess(final GoogleDriveManager.Command command);

        /**
         * Google Drive experiencing an error while perform command,
         * such as {@link Command#UPLOAD} or {@link Command#DOWNLOAD}.
         *
         * @param command Command which experiencing an error.
         * @param error   Error message describes a reason.
         */
        void onError(final GoogleDriveManager.Command command, final GoogleDriveError error);
    }

    private static final String RADIO_STATION_CATEGORY_FAVORITES = "favorites";

    private static final String RADIO_STATION_CATEGORY_LOCALS = "locals";

    private static final String FOLDER_NAME = "OPEN_RADIO";

    private static final String FILE_NAME_RADIO_STATIONS = "RadioStations.txt";

    /**
     * Google API client.
     */
    private GoogleApiClient mGoogleApiClient;

    /**
     *
     */
    private final Queue<Command> mCommands;

    private final Listener mListener;

    private final Context mContext;

    private final ExecutorService mExecutorService = Executors.newSingleThreadExecutor();

    /**
     * Command to perform.
     */
    public enum Command {
        UPLOAD,
        DOWNLOAD
    }

    /**
     * Main constructor.
     *
     * @param context  Context of the application.
     * @param listener Listener for the Google Drive client events.
     */
    public GoogleDriveManager(@NonNull final Context context, @NonNull final Listener listener) {
        super();

        mContext = context;
        mCommands = new ConcurrentLinkedQueue<>();
        mListener = listener;
    }

    public void release() {
        mExecutorService.shutdown();
    }

    public void disconnect() {
        if (mGoogleApiClient != null) {
            mGoogleApiClient.disconnect();
        }
    }

    public void connect() {
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();

            mListener.onConnect();
        }
    }

    /**
     * Upload Radio Stations to Google Drive.
     */
    public void uploadRadioStations() {
        queueCommand(mContext, Command.UPLOAD);
    }

    /**
     *
     */
    public void downloadRadioStations() {
        queueCommand(mContext, Command.DOWNLOAD);
    }

    /**
     * Put a command to query.
     *
     * @param context Context of the callee.
     * @param command Command to put in queue.
     */
    private void queueCommand(final Context context, final Command command) {
        final GoogleApiClient client = getGoogleApiClient(context);
        addCommand(command);
        if (!client.isConnected()) {
            client.connect();
        } else {
            if (!client.isConnecting()) {
                handleNextCommand();
            }
        }
    }

    /**
     * Get data of all Radio Stations which are intended to upload and upload it.
     */
    private void getRadioStationsAndUpload() {
        final String favorites = FavoritesStorage.getAllFavoritesAsString(mContext);
        final String locals = LocalRadioStationsStorage.getAllLocalAsString(mContext);
        final String data = mergeRadioStationCategories(favorites, locals);
        final GoogleDriveRequest.Listener listener = new GoogleDriveRequestListenerImpl(this, Command.UPLOAD);

        mExecutorService.submit(
                () -> uploadInternal(FOLDER_NAME, FILE_NAME_RADIO_STATIONS, data, listener)
        );
    }

    /**
     *
     */
    private void downloadRadioStationsAndApply() {
        final GoogleDriveRequest.Listener listener = new GoogleDriveRequestListenerImpl(this, Command.DOWNLOAD);

        mExecutorService.submit(
                () -> downloadInternal(FOLDER_NAME, FILE_NAME_RADIO_STATIONS, listener)
        );
    }

    /**
     * Do actual upload of a single Radio Stations category.
     *
     * @param folderName Folder to upload to.
     * @param fileName   File name to associated with Radio Stations data.
     * @param data       Marshalled Radio Stations.
     * @param listener   Listener.
     */
    private void uploadInternal(final String folderName, final String fileName, final String data,
                                final GoogleDriveRequest.Listener listener) {
        final GoogleDriveRequest request = new GoogleDriveRequest(
                mGoogleApiClient, folderName, fileName, data, listener
        );
        final GoogleDriveResult result = new GoogleDriveResult();

        request.setExecutorService(mExecutorService);

        final GoogleDriveAPIChain queryFolder = new GoogleDriveQueryFolder();
        final GoogleDriveAPIChain createFolder = new GoogleDriveCreateFolder();
        final GoogleDriveAPIChain queryFile = new GoogleDriveQueryFile();
        final GoogleDriveAPIChain deleteFile = new GoogleDriveDeleteFile();
        final GoogleDriveAPIChain saveFile = new GoogleDriveSaveFile(true);

        queryFolder.setNext(createFolder);
        createFolder.setNext(queryFile);
        queryFile.setNext(deleteFile);
        deleteFile.setNext(saveFile);

        queryFolder.handleRequest(request, result);
    }

    /**
     *
     * @param folderName
     * @param fileName
     * @param listener
     */
    private void downloadInternal(final String folderName, final String fileName,
                                  final GoogleDriveRequest.Listener listener) {
        final GoogleDriveRequest request = new GoogleDriveRequest(
                mGoogleApiClient, folderName, fileName, null, listener
        );

        request.setExecutorService(mExecutorService);

        final GoogleDriveResult result = new GoogleDriveResult();

        final GoogleDriveAPIChain queryFolder = new GoogleDriveQueryFolder();
        final GoogleDriveAPIChain queryFile = new GoogleDriveQueryFile();
        final GoogleDriveAPIChain readFile = new GoogleDriveReadFile(true);

        queryFolder.setNext(queryFile);
        queryFile.setNext(readFile);

        queryFolder.handleRequest(request, result);
    }

    private void addCommand(final Command command) {
        if (mCommands.contains(command)) {
            return;
        }

        AppLogger.d("Add Command: " + command);
        mCommands.add(command);
    }

    private Command removeCommand() {
        return mCommands.remove();
    }

    /**
     *
     * @param context
     * @return
     */
    private synchronized GoogleApiClient getGoogleApiClient(final Context context) {
        if (mGoogleApiClient != null) {
            return mGoogleApiClient;
        }

        mGoogleApiClient = new GoogleApiClient.Builder(context)
                .addApi(Drive.API)
                .addScope(Drive.SCOPE_FILE)
                .addConnectionCallbacks(new ConnectionCallbackImpl(this))
                .addOnConnectionFailedListener(new ConnectionFailedListenerImpl(this))
                .build();
        return mGoogleApiClient;
    }

    /**
     * Handles next available command.
     */
    private void handleNextCommand() {
        if (mCommands.isEmpty()) {
            return;
        }

        final Command command = removeCommand();
        switch (command) {
            case UPLOAD:
                getRadioStationsAndUpload();
                break;
            case DOWNLOAD:
                downloadRadioStationsAndApply();
                break;
        }
    }

    /**
     * Demarshall String into List of Radio Stations and update storage of the application.
     *
     * @param data     String representing list of Radio Stations.
     * @param fileName Name of the file
     */
    private void handleDownloadCompleted(@NonNull final String data, @NonNull final String fileName) {
        AppLogger.d("OnDownloadCompleted file:" + fileName + " data:" + data);

        if (FILE_NAME_RADIO_STATIONS.equals(fileName)) {
            final String favoritesRx = splitRadioStationCategories(data)[0];
            final String localsRx = splitRadioStationCategories(data)[1];

            final List<RadioStationVO> favoritesList = FavoritesStorage.getAllFavorites(mContext);
            final List<RadioStationVO> favoritesRxList = FavoritesStorage.getAllFavoritesFromString(favoritesRx);
            QueueHelper.merge(favoritesList, favoritesRxList);
            for (final RadioStationVO radioStation : favoritesList) {
                FavoritesStorage.addToFavorites(radioStation, mContext);
            }
            final List<RadioStationVO> localsList = LocalRadioStationsStorage.getAllLocals(mContext);
            final List<RadioStationVO> localsRxList = LocalRadioStationsStorage.getAllLocalsFromString(localsRx);
            QueueHelper.merge(localsList, localsRxList);
            for (final RadioStationVO radioStation : localsList) {
                LocalRadioStationsStorage.addToLocal(radioStation, mContext);
            }
        }
    }

    /**
     *
     * @param favorites
     * @param locals
     * @return
     */
    private String mergeRadioStationCategories(@NonNull final String favorites, @NonNull final String locals) {
        final JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(RADIO_STATION_CATEGORY_FAVORITES, favorites);
            jsonObject.put(RADIO_STATION_CATEGORY_LOCALS, locals);
        } catch (final JSONException e) {
            e.printStackTrace();
        }
        return jsonObject.toString();
    }

    /**
     *
     * @param data
     * @return
     */
    private String[] splitRadioStationCategories(@NonNull final String data) {
        final String[] categories = new String[]{"", ""};
        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject(data);
        } catch (final JSONException e) {
            e.printStackTrace();
        }
        if (jsonObject != null) {
            categories[0] = jsonObject.optString(RADIO_STATION_CATEGORY_FAVORITES, "");
            categories[1] = jsonObject.optString(RADIO_STATION_CATEGORY_LOCALS, "");
        }
        return categories;
    }

    private static final class ConnectionCallbackImpl implements GoogleApiClient.ConnectionCallbacks {

        private final WeakReference<GoogleDriveManager> mReference;

        private ConnectionCallbackImpl(final GoogleDriveManager reference) {
            super();
            mReference = new WeakReference<>(reference);
        }

        @Override
        public void onConnected(@Nullable final Bundle bundle) {
            AppLogger.d("On Connected:" + bundle);
            final GoogleDriveManager manager = mReference.get();
            if (manager == null) {
                return;
            }
            manager.handleNextCommand();

            manager.mListener.onConnected();
        }

        @Override
        public void onConnectionSuspended(final int i) {
            AppLogger.d("On Connection suspended:" + i);

            final GoogleDriveManager manager = mReference.get();
            if (manager == null) {
                return;
            }
            manager.mListener.onConnectionFailed(null);
        }
    }

    private static final class ConnectionFailedListenerImpl implements GoogleApiClient.OnConnectionFailedListener {

        private final WeakReference<GoogleDriveManager> mReference;

        private ConnectionFailedListenerImpl(final GoogleDriveManager reference) {
            super();
            mReference = new WeakReference<>(reference);
        }

        @Override
        public void onConnectionFailed(@NonNull final ConnectionResult connectionResult) {
            AppLogger.e("On Connection failed:" + connectionResult);
            final GoogleDriveManager manager = mReference.get();
            if (manager == null) {
                return;
            }
            manager.mListener.onConnectionFailed(connectionResult);
        }
    }

    private static final class GoogleDriveRequestListenerImpl implements GoogleDriveRequest.Listener {

        private final WeakReference<GoogleDriveManager> mReference;
        private final Command mCommand;

        private GoogleDriveRequestListenerImpl(final GoogleDriveManager reference,
                                               final Command command) {
            super();
            mReference = new WeakReference<>(reference);
            mCommand = command;
        }

        @Override
        public void onStart() {
            AppLogger.d("On Google Drive started");
            final GoogleDriveManager manager = mReference.get();
            if (manager == null) {
                return;
            }

            manager.mListener.onStart(mCommand);
        }

        @Override
        public void onUploadComplete() {
            AppLogger.d("On Google Drive upload completed");
            final GoogleDriveManager manager = mReference.get();
            if (manager == null) {
                return;
            }

            manager.handleNextCommand();
            manager.mListener.onSuccess(mCommand);
        }

        @Override
        public void onDownloadComplete(final String data, final String fileName) {
            AppLogger.d("On Google Drive download completed");
            final GoogleDriveManager manager = mReference.get();
            if (manager == null) {
                return;
            }

            manager.handleNextCommand();

            if (data != null) {
                manager.handleDownloadCompleted(data, fileName);
            }

            manager.mListener.onSuccess(mCommand);
        }

        @Override
        public void onError(final GoogleDriveError error) {
            AppLogger.e("On Google Drive error : " + error.toString());
            final GoogleDriveManager manager = mReference.get();
            if (manager == null) {
                return;
            }

            manager.handleNextCommand();
            manager.mListener.onError(mCommand, error);
        }
    }
}

