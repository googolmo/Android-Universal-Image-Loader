package com.nostra13.universalimageloader.core;

import java.io.File;

import android.content.Context;
import android.graphics.Bitmap;

import com.nostra13.universalimageloader.cache.disc.DiscCacheAware;
import com.nostra13.universalimageloader.cache.disc.LruDiskCache;
import com.nostra13.universalimageloader.cache.disc.impl.FileCountLimitedDiscCache;
import com.nostra13.universalimageloader.cache.disc.impl.TotalSizeLimitedDiscCache;
import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiscCache;
import com.nostra13.universalimageloader.cache.disc.naming.FileNameGenerator;
import com.nostra13.universalimageloader.cache.disc.naming.HashCodeFileNameGenerator;
import com.nostra13.universalimageloader.cache.memory.MemoryCacheAware;
import com.nostra13.universalimageloader.cache.memory.impl.FuzzyKeyMemoryCache;
import com.nostra13.universalimageloader.cache.memory.impl.LruMemoryCache;
import com.nostra13.universalimageloader.core.assist.MemoryCacheUtil;
import com.nostra13.universalimageloader.core.display.BitmapDisplayer;
import com.nostra13.universalimageloader.core.display.SimpleBitmapDisplayer;
import com.nostra13.universalimageloader.core.download.ImageDownloader;
import com.nostra13.universalimageloader.core.download.URLConnectionImageDownloader;
import com.nostra13.universalimageloader.utils.StorageUtils;

/**
 * Factory for providing of default options for {@linkplain com.nostra13.universalimageloader.core.ImageLoaderConfiguration configuration}
 * 
 * @author Sergey Tarasevich (nostra13[at]gmail[dot]com)
 */
public class DefaultConfigurationFactory {

	/** Create {@linkplain com.nostra13.universalimageloader.cache.disc.naming.HashCodeFileNameGenerator default implementation} of FileNameGenerator */
	public static FileNameGenerator createFileNameGenerator() {
		return new HashCodeFileNameGenerator();
	}

	/** Create default implementation of {@link LruMemoryCache} depends on incoming parameters */
	public static LruDiskCache createDiscCache(Context context, FileNameGenerator discCacheFileNameGenerator, int discCacheSize, int discCacheFileCount) {
		if (discCacheSize > 0) {
			File individualCacheDir = StorageUtils.getIndividualCacheDirectory(context);
			return new LruDiskCache(individualCacheDir, discCacheSize, discCacheFileNameGenerator);
		} else if (discCacheFileCount > 0) {
			File individualCacheDir = StorageUtils.getIndividualCacheDirectory(context);
			return new LruDiskCache(individualCacheDir, discCacheFileNameGenerator);
		} else {
			File cacheDir = StorageUtils.getIndividualCacheDirectory(context);
			return new LruDiskCache(cacheDir, discCacheFileNameGenerator);
		}
	}

	/** Create default implementation of {@link com.nostra13.universalimageloader.cache.memory.MemoryCacheAware} depends on incoming parameters */
	public static MemoryCacheAware<String, Bitmap> createMemoryCache(Context context, int memoryCachePercent, boolean denyCacheImageMultipleSizesInMemory) {
		MemoryCacheAware<String, Bitmap> memoryCache = new LruMemoryCache(context, memoryCachePercent);
		if (denyCacheImageMultipleSizesInMemory) {
			memoryCache = new FuzzyKeyMemoryCache<String, Bitmap>(memoryCache, MemoryCacheUtil.createFuzzyKeyComparator());
		}
		return memoryCache;
	}

	/** Create default implementation of {@link com.nostra13.universalimageloader.core.download.ImageDownloader} */
	public static ImageDownloader createImageDownloader() {
		return new URLConnectionImageDownloader();
	}

	/** Create default implementation of {@link com.nostra13.universalimageloader.core.display.BitmapDisplayer} */
	public static BitmapDisplayer createBitmapDisplayer() {
		return new SimpleBitmapDisplayer();
	}
}
