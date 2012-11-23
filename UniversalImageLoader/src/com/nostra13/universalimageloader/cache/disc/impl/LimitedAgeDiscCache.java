package com.nostra13.universalimageloader.cache.disc.impl;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import com.nostra13.universalimageloader.cache.disc.BaseDiscCache;
import com.nostra13.universalimageloader.cache.disc.naming.FileNameGenerator;
import com.nostra13.universalimageloader.core.DefaultConfigurationFactory;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

/**
 * Cache which deletes files which were loaded more than defined time. Cache size is unlimited.
 *
 * @author Sergey Tarasevich (nostra13[at]gmail[dot]com)
 * @see BaseDiscCache
 */
public class LimitedAgeDiscCache extends BaseDiscCache {

    private final long maxFileAge;

    private final Map<File, Long> loadingDates = Collections.synchronizedMap(new HashMap<File, Long>());

    /**
     * @param cacheDir Directory for file caching
     * @param maxAge   Max file age (in seconds). If file age will exceed this value then it'll be removed on next treatment
     *                 (and therefore be reloaded).
     */
    public LimitedAgeDiscCache(File cacheDir, long maxAge) {
        this(cacheDir, DefaultConfigurationFactory.createFileNameGenerator(), maxAge);
    }

    /**
     * @param cacheDir          Directory for file caching
     * @param fileNameGenerator Name generator for cached files
     * @param maxAge            Max file age (in seconds). If file age will exceed this value then it'll be removed on next treatment
     *                          (and therefore be reloaded).
     */
    public LimitedAgeDiscCache(File cacheDir, FileNameGenerator fileNameGenerator, long maxAge) {
        super(cacheDir, fileNameGenerator);
        this.maxFileAge = maxAge * 1000; // to milliseconds
        readLoadingDates();
    }

    private void readLoadingDates() {
        File[] cachedFiles = getCacheDir().listFiles();
        for (File cachedFile : cachedFiles) {
            loadingDates.put(cachedFile, cachedFile.lastModified());
        }
    }

//    @Override
//    public void put(String key, File file) {
//        long currentTime = System.currentTimeMillis();
//        file.setLastModified(currentTime);
//        loadingDates.put(file, currentTime);
//    }

    @Override
    public boolean put(String key, Bitmap bitmap, ImageLoaderConfiguration config) {
        File file = super.getFile(key);
        if (super.put(key, bitmap, config)) {
            long currentTime = System.currentTimeMillis();
            file.setLastModified(currentTime);
            loadingDates.put(file, currentTime);
            return true;
        }
        return false;

    }

    @Override
    public Bitmap get(String key, BitmapFactory.Options options) {
        File file = super.getFile(key);
        Bitmap bitmap = null;
        if (file.exists()) {
            Long loadingDate = loadingDates.get(file);
            if (loadingDate == null) {
                loadingDate = file.lastModified();
            }
            if (System.currentTimeMillis() - loadingDate > maxFileAge) {
                file.delete();
                loadingDates.remove(file);
            }
        }
//        if (file.exists()) {
            bitmap = BitmapFactory.decodeFile(file.getAbsolutePath(), options);
//        }
        return bitmap;
    }
}