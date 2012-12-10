package com.nostra13.universalimageloader.cache.memory;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import android.app.ActivityManager;
import android.content.Context;
import com.nostra13.universalimageloader.utils.L;

/**
 * Limited cache. Provides object storing. Size of all stored bitmaps will not to exceed size limit (
 * {@link #getSizeLimit()}).
 * 
 * @author Sergey Tarasevich (nostra13[at]gmail[dot]com)
 * @see com.nostra13.universalimageloader.cache.memory.BaseMemoryCache
 */
public abstract class LimitedMemoryCache<K, V> extends BaseMemoryCache<K, V> {

	private static final int MAX_NORMAL_CACHE_SIZE_IN_MB = 16;
	private static final int MAX_NORMAL_CACHE_SIZE = 16 * 1024 * 1024;

	private static final int DEFAULT_MEMORY_CAPACITY_FOR_DEVICES_OLDER_THAN_API_LEVEL_4 = 12;

	private final int sizeLimit;

	private int cacheSize = 0;

	/**
	 * Contains strong references to stored objects. Each next object is added last. If hard cache size will exceed
	 * limit then first object is deleted (but it continue exist at {@link #softMap} and can be collected by GC at any
	 * time)
	 */
	private final List<V> hardCache = Collections.synchronizedList(new LinkedList<V>());

	/**
	 * @param sizeLimit
	 *            Maximum size for cache (in bytes)
	 */
	public LimitedMemoryCache(int sizeLimit) {
		this.sizeLimit = sizeLimit;
		if (sizeLimit > MAX_NORMAL_CACHE_SIZE) {
			L.w("You set too large memory cache size (more than %1$d Mb)", MAX_NORMAL_CACHE_SIZE_IN_MB);
		}
	}

	/**
	 * @param percentageOfMemoryForCache
	 *            Maximum percent of memory size for cache (in bytes)
	 */
	public LimitedMemoryCache(Context context, int percentageOfMemoryForCache) {
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
		this.sizeLimit = (1024 *1024*(memClass * percentageOfMemoryForCache))/100;
		if (sizeLimit > MAX_NORMAL_CACHE_SIZE) {
			L.w("You set too large memory cache size (more than %1$d Mb)", MAX_NORMAL_CACHE_SIZE_IN_MB);
		}
	}

	@Override
	public boolean put(K key, V value) {
		boolean putSuccessfully = false;
		// Try to add value to hard cache
		int valueSize = getSize(value);
		int sizeLimit = getSizeLimit();
		if (valueSize < sizeLimit) {
			while (cacheSize + valueSize > sizeLimit) {
				V removedValue = removeNext();
				if (hardCache.remove(removedValue)) {
					cacheSize -= getSize(removedValue);
				}
			}
			hardCache.add(value);
			cacheSize += valueSize;

			putSuccessfully = true;
		}
		// Add value to soft cache
		super.put(key, value);
		return putSuccessfully;
	}

	@Override
	public void remove(K key) {
		V value = super.get(key);
		if (value != null) {
			if (hardCache.remove(value)) {
				cacheSize -= getSize(value);
			}
		}
		super.remove(key);
	}

	@Override
	public void clear() {
		hardCache.clear();
		cacheSize = 0;
		super.clear();
	}

	protected int getSizeLimit() {
		return sizeLimit;
	}

	protected abstract int getSize(V value);

	protected abstract V removeNext();
}
