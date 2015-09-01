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

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaDescription;
import android.media.browse.MediaBrowser;
import android.net.Uri;
import android.service.media.MediaBrowserService;
import android.support.annotation.NonNull;

import com.yuriy.openradio.R;
import com.yuriy.openradio.api.APIServiceProvider;
import com.yuriy.openradio.net.Downloader;
import com.yuriy.openradio.service.FavoritesStorage;
import com.yuriy.openradio.utils.MediaIDHelper;

import java.util.List;

/**
 * Created by Yuriy Chernyshov
 * At Android Studio
 * On 8/31/15
 * E-Mail: chernyshov.yuriy@gmail.com
 */
public class MediaItemRoot implements MediaItemCommand {

    @Override
    public void create(final Context context, final String countryCode,
                       final Downloader downloader, final APIServiceProvider serviceProvider,
                       @NonNull final MediaBrowserService.Result<List<MediaBrowser.MediaItem>> result,
                       final List<MediaBrowser.MediaItem> mediaItems) {
        final String iconUrl = "android.resource://" +
                context.getPackageName() + "/drawable/ic_all_categories";

        // Worldwide Stations
        mediaItems.add(new MediaBrowser.MediaItem(
                new MediaDescription.Builder()
                        .setMediaId(MediaIDHelper.MEDIA_ID_ALL_CATEGORIES)
                        .setTitle(context.getString(R.string.all_categories_title))
                        .setIconUri(Uri.parse(iconUrl))
                        .setSubtitle(context.getString(R.string.all_categories_sub_title))
                        .build(), MediaBrowser.MediaItem.FLAG_BROWSABLE
        ));

        // All countries list
        mediaItems.add(new MediaBrowser.MediaItem(
                new MediaDescription.Builder()
                        .setMediaId(MediaIDHelper.MEDIA_ID_COUNTRIES_LIST)
                        .setTitle(context.getString(R.string.countries_list_title))
                        .setIconUri(Uri.parse(iconUrl))
                        .setSubtitle(context.getString(R.string.country_stations_sub_title))
                        .build(), MediaBrowser.MediaItem.FLAG_BROWSABLE
        ));

        //If the Country code is known
        if (!countryCode.isEmpty()) {

            final int identifier = context.getResources().getIdentifier(
                    "flag_" + countryCode.toLowerCase(),
                    "drawable", context.getPackageName()
            );
            // Overlay base image with the appropriate flag
            final BitmapsOverlay overlay = BitmapsOverlay.getInstance();
            final Bitmap bitmap = overlay.execute(context, identifier,
                    BitmapFactory.decodeResource(
                            context.getResources(),
                            R.drawable.ic_all_categories
                    ));
            mediaItems.add(new MediaBrowser.MediaItem(
                    new MediaDescription.Builder()
                            .setMediaId(MediaIDHelper.MEDIA_ID_COUNTRY_STATIONS)
                            .setTitle(context.getString(R.string.country_stations_title))
                            .setIconBitmap(bitmap)
                            .setSubtitle(context.getString(
                                    R.string.country_stations_sub_title
                            ))
                            .build(), MediaBrowser.MediaItem.FLAG_BROWSABLE
            ));
        }

        if (!FavoritesStorage.isFavoritesEmpty(context)) {
            // Favorites list

            final int identifier = context.getResources().getIdentifier(
                    "ic_favorites_on",
                    "drawable", context.getPackageName()
            );
            // Overlay base image with the appropriate flag
            final BitmapsOverlay overlay = BitmapsOverlay.getInstance();
            final Bitmap bitmap = overlay.execute(context, identifier,
                    BitmapFactory.decodeResource(
                            context.getResources(),
                            R.drawable.ic_all_categories
                    ));

            mediaItems.add(new MediaBrowser.MediaItem(
                    new MediaDescription.Builder()
                            .setMediaId(MediaIDHelper.MEDIA_ID_FAVORITES_LIST)
                            .setTitle(context.getString(R.string.favorites_list_title))
                            .setIconBitmap(bitmap)
                            .setSubtitle(context.getString(R.string.favorites_list_sub_title))
                            .build(), MediaBrowser.MediaItem.FLAG_BROWSABLE
            ));
        }

        result.sendResult(mediaItems);
    }
}
