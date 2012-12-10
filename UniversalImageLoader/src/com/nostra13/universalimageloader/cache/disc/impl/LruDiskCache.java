package com.nostra13.universalimageloader.cache.disc.impl;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import com.jakewharton.DiskLruCache;
import com.nostra13.universalimageloader.cache.disc.DiscCacheAware;
import com.nostra13.universalimageloader.cache.disc.naming.FileNameGenerator;
import com.nostra13.universalimageloader.core.DefaultConfigurationFactory;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.utils.FileUtils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * User: googolmo
 * Date: 12-11-21
 * Time: 下午3:04
 */
public class LruDiskCache implements DiscCacheAware {

    private DiskLruCache mDiskCache;
    private static final long DISK_CACHE_SIZE = 1024 * 1024 * 25; //25MB
    private static final int APP_VERSION = 1;
    private static final int VALUE_COUNT = 1;
    public static final int IO_BUFFER_SIZE = 8 * 1024;
    private File mCacheDir;
//    private ImageLoaderConfiguration mConfig;

    private FileNameGenerator fileNameGenerator;


    public LruDiskCache(File cacheDir, FileNameGenerator fileNameGenerator) {
        init(cacheDir, VALUE_COUNT, DISK_CACHE_SIZE);
//        mConfig = config;
        this.fileNameGenerator = fileNameGenerator;
    }

    public LruDiskCache(File cacheDir) {
        init(cacheDir, VALUE_COUNT, DISK_CACHE_SIZE);
//        mConfig = config;
        this.fileNameGenerator = DefaultConfigurationFactory.createFileNameGenerator();
    }

    public LruDiskCache(File cacheDir, long limitSize) {
        init(cacheDir, VALUE_COUNT, limitSize);
//        mConfig = config;
        this.fileNameGenerator = DefaultConfigurationFactory.createFileNameGenerator();
    }

    public LruDiskCache(File cacheDir, long limitSize, FileNameGenerator fileNameGenerator) {
        init(cacheDir, VALUE_COUNT, limitSize);
        this.fileNameGenerator = fileNameGenerator;
//        mConfig = config;
    }

    private void init(File cacheDir, int valueCount, long limitSize) {
        try {
            mDiskCache = DiskLruCache.open(cacheDir, APP_VERSION, valueCount, limitSize);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean put(String key, Bitmap bitmap, ImageLoaderConfiguration config) {
        DiskLruCache.Editor editor = null;
        key = this.fileNameGenerator.generate(key);
        try {
            editor = mDiskCache.edit(key);
            if (editor == null) {
                return false;
            }
            if (writeBitmapToFile(bitmap, editor, config)) {
                mDiskCache.flush();
                editor.commit();
                return true;
            } else {
                editor.abort();
            }
        } catch (IOException e) {
            e.printStackTrace();
            try {
                if (editor != null) {
                    editor.abort();
                }
            } catch (IOException ignored) {
                ignored.printStackTrace();
            }
        }
        return false;
    }

    private boolean writeBitmapToFile(Bitmap bitmap, DiskLruCache.Editor editor, ImageLoaderConfiguration config) throws IOException {
        OutputStream out = null;
        try {
            out = new BufferedOutputStream(editor.newOutputStream(0), IO_BUFFER_SIZE);
            return bitmap.compress(config.imageCompressFormatForDiscCache, config.imageQualityForDiscCache, out);
        } finally {
            if (out != null) {
                out.close();
            }
        }
    }

    @Override
    public Bitmap get(String key, BitmapFactory.Options options) {
        Bitmap bitmap = null;
        DiskLruCache.Snapshot snapshot = null;
        key = this.fileNameGenerator.generate(key);
        try {
            snapshot = mDiskCache.get(key);
            if (snapshot == null) {
                return null;
            }
            final InputStream in = snapshot.getInputStream(0);
            if (in != null) {
                final BufferedInputStream buffIn = new BufferedInputStream(in, IO_BUFFER_SIZE);
                if (options == null) {
//					options = new BitmapFactory.Options();
                    bitmap = BitmapFactory.decodeStream(buffIn);
                } else {
                    bitmap = BitmapFactory.decodeStream(buffIn, null, options);
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (snapshot != null) {
                snapshot.close();
            }
        }
        return bitmap;
    }

//    @Override
//    public InputStream getInputStream(String key) {
//        DiskLruCache.Snapshot snapshot = null;
//        key = this.fileNameGenerator.generate(key);
//        try {
//            snapshot = mDiskCache.get(key);
//            if (snapshot == null) {
//                return null;
//            }
//            return snapshot.getInputStream(0);
//        } catch (IOException e) {
//            e.printStackTrace();
//        } finally {
//            if (snapshot != null) {
//                snapshot.close();
//            }
//        }
//        return null;
//    }

    @Override
    public void clear() {
        try {
            mDiskCache.delete();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean delete(String key) {
        return false;
    }

    @Override
    public boolean isExits(String key) {
        DiskLruCache.Snapshot snapshot = null;
        key = this.fileNameGenerator.generate(key);
        try {
            snapshot = mDiskCache.get(key);
            if (snapshot != null) {
                return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }
}
