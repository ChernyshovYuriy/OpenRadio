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

package com.yuriy.openradio.vo;

import android.support.annotation.NonNull;

import com.yuriy.openradio.service.LocationService;

/**
 * Created by Chernyshov Yurii
 * At Android Studio
 * On 19/11/17
 * E-Mail: chernyshov.yuriy@gmail.com
 */

public final class GoogleGeoLocation {

    private CountryVO mCountry;

    public GoogleGeoLocation() {
        super();
        mCountry = CountryVO.getDefaultCountry();
    }

    public void setCountry(@NonNull final CountryVO value) {
        mCountry = new CountryVO(value.getName(), value.getCode());
    }

    public CountryVO getCountry() {
        return mCountry;
    }

    @Override
    public String toString() {
        return "GoogleGeoLocation{" +
                "country=" + mCountry +
                "}";
    }
}
