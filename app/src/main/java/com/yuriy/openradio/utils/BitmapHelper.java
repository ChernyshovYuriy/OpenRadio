/*
 * Copyright (C) 2015 The Android Open Source Project
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

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * {@link BitmapHelper} is a helper class that provides different methods to operate over Bitmap.
 */
public class BitmapHelper {

    // Bitmap size for album art in media notifications when there are more than 3 playback actions
    public static final int MEDIA_ART_SMALL_WIDTH  = 64;
    public static final int MEDIA_ART_SMALL_HEIGHT = 64;

    // Bitmap size for album art in media notifications when there are no more than 3 playback actions
    public static final int MEDIA_ART_BIG_WIDTH  = 128;
    public static final int MEDIA_ART_BIG_HEIGHT = 128;

    /**
     * Scale Bitmap.
     *
     * @param scaleFactor Scale factor.
     * @param inputStream Input Stream that represents a Bitmap.
     * @return Scaled Bitmap.
     */
    public static Bitmap scaleBitmap(final int scaleFactor, final InputStream inputStream) {
        // Get the dimensions of the bitmap
        final BitmapFactory.Options options = new BitmapFactory.Options();

        // Decode the image file into a Bitmap sized to fill the View
        options.inJustDecodeBounds = false;
        options.inSampleSize = scaleFactor;

        return BitmapFactory.decodeStream(inputStream, null, options);
    }

    /**
     * Method to help find scale factor for the Bitmap vased on the desired Width and Height.
     *
     * @param targetW     desired width.
     * @param targetH     Desired height.
     * @param inputStream Input Stream that represents a Bitmap.
     * @return Scale factor.
     */
    public static int findScaleFactor(final int targetW, final int targetH,
                                      final InputStream inputStream) {
        // Get the dimensions of the bitmap
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(inputStream, null, options);
        int actualW = options.outWidth;
        int actualH = options.outHeight;

        // Determine how much to scale down the image
        return Math.min(actualW / targetW, actualH / targetH);
    }

    /**
     * Download and scale Bitmap.
     *
     * @param uri    Uri to fetch from.
     * @param width  Width of the Bitmap.
     * @param height Height of the Bitmap.
     * @return Downloaded and scaled Bitmap.
     * @throws IOException
     */
    public static Bitmap fetchAndRescaleBitmap(final String uri, final int width, final int height)
            throws IOException {

        final URL url = new URL(uri);
        HttpURLConnection httpConnection = (HttpURLConnection) url.openConnection();
        httpConnection.setDoInput(true);
        httpConnection.connect();
        InputStream inputStream = httpConnection.getInputStream();
        int scaleFactor = findScaleFactor(width, height, inputStream);

        httpConnection = (HttpURLConnection) url.openConnection();
        httpConnection.setDoInput(true);
        httpConnection.connect();
        inputStream = httpConnection.getInputStream();

        return scaleBitmap(scaleFactor, inputStream);
    }

    /**
     * Overlay two Bitmaps.
     *
     * @param bitmap_A Bitmap to overlay on.
     * @param bitmap_B Bitmap overlay.
     * @return Bitmap.
     */
    public static Bitmap overlay(final Bitmap bitmap_A, final Bitmap bitmap_B) {
        final Bitmap bitmap = Bitmap.createBitmap(
                bitmap_A.getWidth(), bitmap_A.getHeight(), bitmap_A.getConfig()
        );
        final Canvas canvas = new Canvas(bitmap);
        canvas.drawBitmap(bitmap_A, new Matrix(), null);
        canvas.drawBitmap(bitmap_B, 0, 0, null);
        return bitmap;
    }

    /**
     *
     * @param options
     * @param reqWidth
     * @param reqHeight
     * @return
     */
    public static int calculateInSampleSize(final BitmapFactory.Options options,
                                            final int reqWidth, final int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    /**
     * Overlay Flag over Bitmap.
     *
     * @param baseBitmap Bitmap to overlay with Flag.
     * @param flagData   Flag Bitmap.
     * @return Overlay Bitmap.
     */
    public static Bitmap overlayWithFlag(final Bitmap baseBitmap, final byte[] flagData) {
        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(
                options, baseBitmap.getWidth() / 3, baseBitmap.getHeight() / 3
        );

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;

        final Bitmap flagBitmap = BitmapFactory.decodeByteArray(
                flagData, 0, flagData.length, options
        );

        return overlay(baseBitmap, flagBitmap);
    }
}
