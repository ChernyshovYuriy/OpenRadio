/*
 * Copyright 2016 The "Open Radio" Project. Author: Chernyshov Yuriy
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

package com.yuriy.openradio.business.mediaitem;

import android.media.MediaDescription;
import android.media.MediaMetadata;
import android.media.browse.MediaBrowser;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;

import com.yuriy.openradio.R;
import com.yuriy.openradio.api.RadioStationVO;
import com.yuriy.openradio.business.SafeRunnable;
import com.yuriy.openradio.net.UrlBuilder;
import com.yuriy.openradio.service.FavoritesStorage;
import com.yuriy.openradio.utils.AppUtils;
import com.yuriy.openradio.utils.MediaIDHelper;
import com.yuriy.openradio.utils.MediaItemHelper;
import com.yuriy.openradio.utils.QueueHelper;

import java.util.List;

/**
 * Created by Yuriy Chernyshov
 * At Android Studio
 * On 8/31/15
 * E-Mail: chernyshov.yuriy@gmail.com
 */

/**
 * {@link MediaItemRecentlyAddedStations} is concrete implementation of the {@link MediaItemCommand}
 * that designed to prepare the recently added radio stations.
 */
public class MediaItemRecentlyAddedStations implements MediaItemCommand {

    private static final String CLASS_NAME = MediaItemRecentlyAddedStations.class.getSimpleName();

    /**
     * Default constructor.
     */
    public MediaItemRecentlyAddedStations() {
        super();
    }

    @Override
    public void create(final IUpdatePlaybackState playbackStateListener,
                       @NonNull final MediaItemShareObject shareObject) {

        // Use result.detach to allow calling result.sendResult from another thread:
        shareObject.getResult().detach();

        AppUtils.API_CALL_EXECUTOR.submit(
                new SafeRunnable<MediaItemRecentlyAddedStations>(this) {

                    @Override
                    public void safeRun(final MediaItemRecentlyAddedStations reference) {
                        if (reference == null) {
                            Log.e(CLASS_NAME, "Reference is null");
                            return;
                        }
                        // Load all categories into menu
                        reference.loadStations(playbackStateListener, shareObject);
                    }
                }
        );
    }

    /**
     * Load Radio Stations into Menu.
     *
     * @param playbackStateListener Listener of the Playback State changes.
     * @param shareObject           Instance of the {@link MediaItemShareObject} which holds various
     *                              references needed to execute command.
     */
    private void loadStations(final IUpdatePlaybackState playbackStateListener,
                              @NonNull final MediaItemShareObject shareObject) {
        final Uri uri;
        if (shareObject.isAndroidAuto()) {
            final int numberOfItems = 10;
            uri = UrlBuilder.getRecentlyAddedStations(shareObject.getContext(), numberOfItems);
        } else {
            uri = UrlBuilder.getRecentlyAddedStations(shareObject.getContext());
        }
        final List<RadioStationVO> list = shareObject.getServiceProvider().getStations(
                shareObject.getDownloader(),
                uri
        );

        if (list.isEmpty()) {

            final MediaMetadata track = MediaItemHelper.buildMediaMetadataForEmptyCategory(
                    shareObject.getContext(),
                    MediaIDHelper.MEDIA_ID_PARENT_CATEGORIES + shareObject.getCurrentCategory()
            );
            final MediaDescription mediaDescription = track.getDescription();
            final MediaBrowser.MediaItem mediaItem = new MediaBrowser.MediaItem(
                    mediaDescription, MediaBrowser.MediaItem.FLAG_BROWSABLE);
            shareObject.getMediaItems().add(mediaItem);
            shareObject.getResult().sendResult(shareObject.getMediaItems());

            if (playbackStateListener != null) {
                playbackStateListener.updatePlaybackState(
                        shareObject.getContext().getString(R.string.no_data_message)
                );
            }

            return;
        }

        synchronized (QueueHelper.RADIO_STATIONS_MANAGING_LOCK) {
            QueueHelper.copyCollection(shareObject.getRadioStations(), list);
        }

        for (final RadioStationVO radioStation : shareObject.getRadioStations()) {

            final MediaDescription mediaDescription = MediaItemHelper.buildMediaDescriptionFromRadioStation(
                    shareObject.getContext(),
                    radioStation
            );

            final MediaBrowser.MediaItem mediaItem = new MediaBrowser.MediaItem(
                    mediaDescription, MediaBrowser.MediaItem.FLAG_PLAYABLE);

            if (FavoritesStorage.isFavorite(radioStation, shareObject.getContext())) {
                MediaItemHelper.updateFavoriteField(mediaItem, true);
            }

            shareObject.getMediaItems().add(mediaItem);
        }

        shareObject.getResult().sendResult(shareObject.getMediaItems());
    }
}