package com.nostra13.universalimageloader.cache.memory.impl;

import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Bitmap;
import com.nostra13.universalimageloader.cache.memory.MemoryCacheAware;
import com.nostra13.universalimageloader.utils.LruCache;

import java.util.Collection;

/**
 * Limited {@link Bitmap bitmap} cache. Provides {@link Bitmap bitmaps} storing. Size of all stored bitmaps will not to
 * exceed size limit. When cache reaches limit size then the least recently used bitmap is deleted from cache.
 *
 * @author Sergey Tarasevich (nostra13[at]gmail[dot]com)
 */
public class LruMemoryCache implements MemoryCacheAware<String, Bitmap> {

    public static final int DEFAULT_MEMORY_CACHE_PERCENTAGE = 25;
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
        this.mCache = new LruCache<String, Bitmap>(this.capacity) {
            @Override
            protected int sizeOf(String key, Bitmap value) {
//                return value.getRowBytes()*value.getHeight();
                return value.getByteCount();
            }
        };
    }



    @Override
    public boolean put(String key, Bitmap value) {
        synchronized (mCache) {
            if (get(key) == null) {
                mCache.put(key, value);
            }
        }

        return false;
    }

    @Override
    public Bitmap get(String key) {
        return mCache.get(key);
    }

    @Override
    public void remove(String key) {
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
