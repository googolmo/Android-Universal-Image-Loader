package com.nostra13.universalimageloader.cache.memory.impl;

import java.util.Collection;

import android.graphics.Bitmap;

import android.support.v4.util.LruCache;
import com.nostra13.universalimageloader.cache.memory.MemoryCacheAware;

/**
 * Limited {@link Bitmap bitmap} cache. Provides {@link Bitmap bitmaps} storing. Size of all stored bitmaps will not to
 * exceed size limit. When cache reaches limit size then the least recently used bitmap is deleted from cache.
 *
 * @author Sergey Tarasevich (nostra13[at]gmail[dot]com)
 */
public class LruMemoryCache implements MemoryCacheAware {

    private static final int INITIAL_CAPACITY = 10;
    private static final float LOAD_FACTOR = 1.1f;
    private int capacity;

    /**
     * Cache providing Least-Recently-Used logic
     *
     */
//    private final Map<String, Bitmap> lruCache = Collections.synchronizedMap(new LinkedHashMap<String, Bitmap>(INITIAL_CAPACITY, LOAD_FACTOR, true));

    private LruCache<String, Bitmap> lruCache;


    public LruMemoryCache(int sizeLimit) {
        capacity = sizeLimit * 1204 * 1024;
//        lruCache = new LruCache<String, Bitmap>(sizeLimit * 1024 * 1024);
    }

    private void reset() {
        if (lruCache != null) {
            lruCache.evictAll();
        }
        lruCache = new LruCache<String, Bitmap>(capacity) {
            @Override
            protected int sizeOf(String key, Bitmap value) {
                return value.getRowBytes()*value.getHeight();
            }
        };
    }



    @Override
    public boolean put(Object key, Object value) {
        return false;
    }

    @Override
    public Object get(Object key) {
        return null;
    }

    @Override
    public void remove(Object key) {
    }

    @Override
    public Collection keys() {
        return null;
    }

    @Override
    public void clear() {
        reset();
    }

}
