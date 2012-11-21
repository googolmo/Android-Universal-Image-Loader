package com.nostra13.universalimageloader.cache.disc.impl;

import java.io.File;

import android.graphics.Bitmap;
import com.nostra13.universalimageloader.cache.disc.BaseDiscCache;
import com.nostra13.universalimageloader.cache.disc.DiscCacheAware;
import com.nostra13.universalimageloader.cache.disc.naming.FileNameGenerator;
import com.nostra13.universalimageloader.core.DefaultConfigurationFactory;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

/**
 * Default implementation of {@linkplain DiscCacheAware disc cache}. Cache size is unlimited.
 *
 * @author Sergey Tarasevich (nostra13[at]gmail[dot]com)
 * @see BaseDiscCache
 */
public class UnlimitedDiscCache extends BaseDiscCache {
    /**
     * @param cacheDir Directory for file caching
     */
    public UnlimitedDiscCache(File cacheDir) {
        this(cacheDir, DefaultConfigurationFactory.createFileNameGenerator());
    }

    /**
     * @param cacheDir          Directory for file caching
     * @param fileNameGenerator Name generator for cached files
     */
    public UnlimitedDiscCache(File cacheDir, FileNameGenerator fileNameGenerator) {
        super(cacheDir, fileNameGenerator);
    }

//    @Override
//    public void put(String key, File file) {
//        // Do nothing
//    }

//    @Override
//    public void put(String key, Bitmap bitmap, ImageLoaderConfiguration config) {
//        //Do nothing
////        super.put(key, bitmap);
//    }
}
