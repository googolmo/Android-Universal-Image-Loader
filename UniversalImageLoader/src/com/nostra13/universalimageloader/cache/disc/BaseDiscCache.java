package com.nostra13.universalimageloader.cache.disc;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import com.nostra13.universalimageloader.cache.disc.naming.FileNameGenerator;
import com.nostra13.universalimageloader.core.DefaultConfigurationFactory;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

/**
 * Base disc cache. Implements common functionality for disc cache.
 *
 * @author Sergey Tarasevich (nostra13[at]gmail[dot]com)
 * @see DiscCacheAware
 * @see FileNameGenerator
 */
public abstract class BaseDiscCache implements DiscCacheAware {

    private File cacheDir;

    private FileNameGenerator fileNameGenerator;

    public BaseDiscCache(File cacheDir) {
        this(cacheDir, DefaultConfigurationFactory.createFileNameGenerator());
    }

    public BaseDiscCache(File cacheDir, FileNameGenerator fileNameGenerator) {
        this.cacheDir = cacheDir;
        this.fileNameGenerator = fileNameGenerator;
    }

    @Override
    public Bitmap get(String key) {
        String fileName = fileNameGenerator.generate(key);
        File f = new File(cacheDir, fileName);
        Bitmap bitmap = BitmapFactory.decodeFile(f.getAbsolutePath());
        if (bitmap == null && f.exists()) {
            f.delete();
        }
        return bitmap;
    }

    public File getFile(String key) {
        String fileName = fileNameGenerator.generate(key);
        File f = new File(cacheDir, fileName);
        return f;
    }

    @Override
    public void clear() {
        File[] files = cacheDir.listFiles();
        if (files != null) {
            for (File f : files) {
                f.delete();
            }
        }
    }

    @Override
    public boolean delete(String key) {
        String fileName = fileNameGenerator.generate(key);
        File f = new File(cacheDir, fileName);
        if (f.exists()) {
            return f.delete();
        }
        return false;
    }

    @Override
    public void put(String key, Bitmap bitmap, ImageLoaderConfiguration config) {
        File file = getFile(key);
        if (file.exists()) {
            file.delete();
        }
        FileOutputStream out = null;
        try {
            file.createNewFile();
            out = new FileOutputStream(file);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        boolean result = bitmap.compress(config.imageCompressFormatForDiscCache, config.imageQualityForDiscCache, out);
        if (result) {
            bitmap.recycle();
        }
        try {
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try{
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    protected File getCacheDir() {
        return cacheDir;
    }
}