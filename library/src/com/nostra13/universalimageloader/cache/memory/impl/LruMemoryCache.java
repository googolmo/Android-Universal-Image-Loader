package com.nostra13.universalimageloader.cache.memory.impl;

import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import com.nostra13.universalimageloader.cache.memory.MemoryCacheAware;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.utils.LruCache;

import java.util.Collection;

/**
 * Limited {@link Bitmap bitmap} cache. Provides {@link Bitmap bitmaps} storing. Size of all stored bitmaps will not to
 * exceed size limit. When cache reaches limit size then the least recently used bitmap is deleted from cache.
 *
 * @author Sergey Tarasevich (nostra13[at]gmail[dot]com)
 */
public class LruMemoryCache implements MemoryCacheAware<String, Bitmap> {

    private static final int DEFAULT_MEMORY_CAPACITY_FOR_DEVICES_OLDER_THAN_API_LEVEL_4 = 12;
    private LruCache<String, Bitmap> mCache;
    private int capacity;


    public LruMemoryCache(Context context, int percentageOfMemoryForCache) {
        int memClass = ((ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE)).getMemoryClass();

        if(memClass == 0) {
            memClass = DEFAULT_MEMORY_CAPACITY_FOR_DEVICES_OLDER_THAN_API_LEVEL_4;
        }
        if(percentageOfMemoryForCache < 0) {
            percentageOfMemoryForCache = 0;
        }
        if(percentageOfMemoryForCache > 81) {
            percentageOfMemoryForCache = 80;
        }
        this.capacity = (1024 *1024*(memClass * percentageOfMemoryForCache))/100;
        if(this.capacity <= 0) {
            this.capacity = 1024*1024*4;
        }
        reset();
    }

    private void reset() {
        if (this.mCache != null) {
            this.mCache.evictAll();
        }
        Log.d(ImageLoader.TAG, "memorry cache size = " + this.capacity);
        this.mCache = new LruCache<String, Bitmap>(this.capacity) {
            @Override
            protected int sizeOf(String key, Bitmap value) {
                return value.getRowBytes()*value.getHeight();
//                return value.getByteCount();
            }
        };
    }



    @Override
    public boolean put(String key, Bitmap value) {
//        synchronized (mCache) {
            if (get(key) == null) {
                Bitmap b = mCache.put(key, value);
                if (b != null) {
//                    b.recycle();
                    return true;
                }
            }
//        }


        return false;
    }

    @Override
    public Bitmap get(String key) {
        Bitmap bitmap = mCache.get(key);
        if (bitmap != null && bitmap.isRecycled()) {
            mCache.remove(key);
            bitmap = null;
        }
        return bitmap;
    }

    @Override
    public void remove(String key) {
        Bitmap bitmap = mCache.get(key);
        if (bitmap != null && !bitmap.isRecycled()) {
            bitmap.recycle();
        }
        mCache.remove(key);

    }

    @Override
    public Collection<String> keys() {
        return mCache.snapshot().keySet();
    }

    @Override
    public void clear() {
        reset();
    }

}
