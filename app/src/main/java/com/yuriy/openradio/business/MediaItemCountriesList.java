/*
 * Copyright 2015 The "Open Radio" Project. Author: Chernyshov Yuriy
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

package com.yuriy.openradio.business;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaDescription;
import android.media.browse.MediaBrowser;
import android.support.annotation.NonNull;

import com.yuriy.openradio.R;
import com.yuriy.openradio.net.UrlBuilder;
import com.yuriy.openradio.utils.AppUtils;
import com.yuriy.openradio.utils.MediaIDHelper;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by Yuriy Chernyshov
 * At Android Studio
 * On 8/31/15
 * E-Mail: chernyshov.yuriy@gmail.com
 */

/**
 * {@link MediaItemCountriesList} is concrete implementation of the {@link MediaItemCommand} that
 * designed to prepare data to display list of all Countries.
 */
public class MediaItemCountriesList implements MediaItemCommand {

    @Override
    public void create(final IUpdatePlaybackState playbackStateListener,
                       @NonNull final MediaItemShareObject shareObject) {

        // Use result.detach to allow calling result.sendResult from another thread:
        shareObject.getResult().detach();

        AppUtils.API_CALL_EXECUTOR.submit(
                new Runnable() {

                    @Override
                    public void run() {

                        // Load all countries into menu
                        loadAllCountries(playbackStateListener, shareObject);
                    }
                }
        );
    }

    /**
     * Load All Countries into Menu.
     *
     * @param playbackStateListener Listener of the Playback State changes.
     * @param shareObject           Instance of the {@link MediaItemShareObject} which holds various
     *                              references needed to execute command.
     */
    private void loadAllCountries(final IUpdatePlaybackState playbackStateListener,
                                  @NonNull final MediaItemShareObject shareObject) {

        final List<String> list = shareObject.getServiceProvider().getCounties(
                shareObject.getDownloader(),
                UrlBuilder.getAllCountriesUrl(shareObject.getContext()));

        if (list.isEmpty() && playbackStateListener != null) {
            playbackStateListener.updatePlaybackState(
                    shareObject.getContext().getString(R.string.no_data_message)
            );
            return;
        }

        Collections.sort(list, new Comparator<String>() {

            @Override
            public int compare(String lhs, String rhs) {
                return lhs.compareTo(rhs);
            }
        });

        String countryName;
        // Overlay base image with the appropriate flag
        final BitmapsOverlay flagLoader = BitmapsOverlay.getInstance();
        Bitmap bitmap;

        for (final String countryCode : list) {

            if (AppUtils.COUNTRY_CODE_TO_NAME.containsKey(countryCode)) {
                countryName = AppUtils.COUNTRY_CODE_TO_NAME.get(countryCode);
            } else {
                countryName = "";
            }

            final int identifier = shareObject.getContext().getResources().getIdentifier(
                    "flag_" + countryCode.toLowerCase(),
                    "drawable", shareObject.getContext().getPackageName()
            );

            bitmap = flagLoader.execute(shareObject.getContext(), identifier,
                    BitmapFactory.decodeResource(
                            shareObject.getContext().getResources(),
                            R.drawable.ic_child_categories
                    )
            );

            shareObject.getMediaItems().add(new MediaBrowser.MediaItem(
                    new MediaDescription.Builder()
                            .setMediaId(
                                    MediaIDHelper.MEDIA_ID_COUNTRIES_LIST + countryCode
                            )
                            .setTitle(countryName)
                            .setIconBitmap(bitmap)
                            .setSubtitle(countryCode)
                            .build(), MediaBrowser.MediaItem.FLAG_BROWSABLE
            ));
        }

        shareObject.getResult().sendResult(shareObject.getMediaItems());
    }
}
