package com.nostra13.universalimageloader.core.download;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

/**
 * Provides retrieving of {@link InputStream} of image by URI.
 *
 * @author Sergey Tarasevich (nostra13[at]gmail[dot]com)
 */
public abstract class ImageDownloader {

    public static final String PROTOCOL_FILE = "file";

    public static final String PROTOCOL_HTTP = "http";
    public static final String PROTOCOL_HTTPS = "https";
    public static final String PROTOCOL_FTP = "ftp";
    public static final String PROTOCOL_CACHE = "discCache";

    public static final int BUFFER_SIZE = 8 * 1024; // 8 Kb

    /** Retrieves {@link InputStream} of image by URI. Image can be located as in the network and on local file system. */
    public InputStream getStream(URI imageUri, ImageLoaderConfiguration config) throws IOException {
        String scheme = imageUri.getScheme();
        if (PROTOCOL_HTTP.equals(scheme) || PROTOCOL_HTTPS.equals(scheme) || PROTOCOL_FTP.equals(scheme)) {
            return getStreamFromNetwork(imageUri);
        } else if (PROTOCOL_FILE.equals(scheme)) {
            return getStreamFromFile(imageUri);
        } else if (PROTOCOL_CACHE.equals(scheme)) {
            Log.d(ImageLoader.TAG, "key=" + imageUri.getHost() + ":"+ imageUri.getPath());
            return null;
        } else {
            return getStreamFromOtherSource(imageUri);
        }
    }

    public Bitmap getBitmap(URI imageUri, ImageLoaderConfiguration config, BitmapFactory.Options options) {
        return config.discCache.get(imageUri.getHost() + ":"+ imageUri.getPath(), options);
    }

    /**
     * Retrieves {@link InputStream} of image by URI from other source. Should be overriden by successors to implement
     * image downloading from special sources (not local file and not web URL).
     */
    protected InputStream getStreamFromOtherSource(URI imageUri) throws IOException {
        return null;
    }

    /** Retrieves {@link InputStream} of image by URI (image is located in the network) */
    protected abstract InputStream getStreamFromNetwork(URI imageUri) throws IOException;

    /** Retrieves {@link InputStream} of image by URI (image is located on the local file system or SD card) */
    protected InputStream getStreamFromFile(URI imageUri) throws IOException {
        return new BufferedInputStream(imageUri.toURL().openStream(), BUFFER_SIZE);
    }
}