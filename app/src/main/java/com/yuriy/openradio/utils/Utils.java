/*
 * Copyright (C) 2012 The Android Open Source Project
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

package com.yuriy.openradio.utils;

import android.os.Build;
import android.os.Build.VERSION_CODES;

/**
 * Class containing static utility methods.
 */
public final class Utils {

    private Utils() {}

    /**
     * Holder for the Search query. Up to now I found it as quick solution to pass query
     * from {@link com.yuriy.openradio.view.MainActivity} to the
     * {@link com.yuriy.openradio.business.service.OpenRadioService}
     */
    private static StringBuilder sSearchQuery = new StringBuilder();

    public static boolean hasKitKat() {
        return Build.VERSION.SDK_INT >= VERSION_CODES.KITKAT;
    }

    public static boolean hasVersionM() {
        return Build.VERSION.SDK_INT >= VERSION_CODES.M;
    }

    /**
     * Save Search query string.
     *
     * @param searchQuery Search query string.
     */
    public static void setSearchQuery(final String searchQuery) {
        clearSearchQuery();
        sSearchQuery.append(searchQuery);
    }

    /**
     * @return Gets the Search query string.
     */
    public static String getSearchQuery() {
        return sSearchQuery.toString();
    }

    /**
     * Clear Search query string.
     */
    public static void clearSearchQuery() {
        sSearchQuery.setLength(0);
    }
}
