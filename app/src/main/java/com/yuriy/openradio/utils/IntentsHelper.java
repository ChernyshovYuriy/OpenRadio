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

package com.yuriy.openradio.utils;

import android.content.Intent;
import android.net.Uri;

/**
 * Created by Yuriy Chernyshov
 * At Android Studio
 * On 12/20/14
 * E-Mail: chernyshov.yuriy@gmail.com
 */
public final class IntentsHelper {

    /**
     * Private constructor
     */
    private IntentsHelper() { }

    /**
     * Make intent to navigate to provided url.
     *
     * @param url Url to navigate to.
     * @return {@link android.content.Intent}
     */
    public static Intent makeUrlBrowsableIntent(final String url) {
        return new Intent(Intent.ACTION_VIEW, Uri.parse(url));
    }
}
