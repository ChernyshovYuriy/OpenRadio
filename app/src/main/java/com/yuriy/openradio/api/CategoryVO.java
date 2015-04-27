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

package com.yuriy.openradio.api;

/**
 * Created by Yuriy Chernyshov
 * At Android Studio
 * On 12/15/14
 * E-Mail: chernyshov.yuriy@gmail.com
 */

import android.util.Log;

/**
 * {@link com.yuriy.openradio.api.CategoryVO} is a value object that holds Radio Category data.
 */
public final class CategoryVO {

    private static final String CLASS_NAME = CategoryVO.class.getSimpleName();

    private int mId;

    private int mAmount;

    private String mName = "";

    private String mDescription = "";

    private String mTitle = "";

    /**
     * Private constructor.
     * Disallow instantiation of this helper class.
     */
    private CategoryVO() { }

    public int getId() {
        return mId;
    }

    public void setId(final int value) {
        mId = value;
    }

    public String getName() {
        return mName;
    }

    public void setName(String value) {
        if (value == null) {
            Log.w(CLASS_NAME, "Attempt to set null Name, reset it to empty String");
            value = "";
        }
        mName = value;
    }

    public String getDescription() {
        return mDescription;
    }

    public void setDescription(final String value) {
        mDescription = value;
    }

    public int getAmount() {
        return mAmount;
    }

    public void setAmount(final int value) {
        mAmount = value;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String value) {
        mTitle = value;
    }

    /**
     * Factory method to create instance of the {@link com.yuriy.openradio.api.CategoryVO}.
     *
     * @return Instance of the {@link com.yuriy.openradio.api.CategoryVO}.
     */
    public static CategoryVO makeDefaultInstance() {
        return new CategoryVO();
    }
}
