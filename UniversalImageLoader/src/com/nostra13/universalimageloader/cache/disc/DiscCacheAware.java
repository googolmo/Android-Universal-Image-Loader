package com.nostra13.universalimageloader.cache.disc;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import java.io.File;
import java.io.InputStream;

/**
 * Interface for disc cache
 *
 * @author Sergey Tarasevich (nostra13[at]gmail[dot]com)
 */
public interface DiscCacheAware {

    /**
     * This method must not to save file on file system in fact. It is called after image was cached in cache directory
     * and it was decoded to bitmap in memory. Such order is required to prevent possible deletion of file after it was
     * cached on disc and before it was tried to decode to bitmap.
     */
//    void put(String key, File file);

    boolean put(String key, Bitmap bitmap, ImageLoaderConfiguration config);

    /**
     * Returns {@linkplain File file object} appropriate incoming key.<br />
     * <b>NOTE:</b> Must <b>not to return</b> a null. Method must return specific {@linkplain File file object} for
     * incoming key whether file exists or not.
     */
//    File get(String key);

    /**
     * Returns {@linkplain Bitmap object} appropriate incoming key.<br />
     * <b>NOTE:</b> Must <b>not to return</b> a null. Method must return specific {@linkplain Bitmap object} for
     * incoming key whether file exists or not.
     */
    Bitmap get(String key, BitmapFactory.Options options);

    /**
     * Clears cache directory
     */
    void clear();

    /**
     * 删除某个文件
     * @param key
     */
    boolean delete(String key);

    boolean isExits(String key);
}
