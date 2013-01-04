package com.nostra13.universalimageloader.cache.disc;

import android.graphics.Bitmap;
import com.nostra13.universalimageloader.cache.disc.DiscCacheAware;
import com.nostra13.universalimageloader.cache.disc.naming.FileNameGenerator;
import com.nostra13.universalimageloader.core.DefaultConfigurationFactory;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.utils.DiskLruCache.DiskLruCache;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

/**
 * User: googolmo
 * Date: 12-12-11
 * Time: 下午3:49
 */
public class LruDiskCache{

	private DiskLruCache mDiskCache;
	private static final long DISK_CACHE_SIZE = 1024 * 1024 * 25; //25MB
	private static final int APP_VERSION = 1;
	private static final int VALUE_COUNT = 1;
	public static final int IO_BUFFER_SIZE = 8 * 1024;
	private File mCacheDir;
	private FileNameGenerator fileNameGenerator;

	public LruDiskCache(File cacheDir) {
		init(cacheDir, DISK_CACHE_SIZE);
		this.fileNameGenerator = DefaultConfigurationFactory.createFileNameGenerator();

	}

	public LruDiskCache(File cacheDir, FileNameGenerator fileNameGenerator) {
		init(cacheDir, DISK_CACHE_SIZE);
		if (fileNameGenerator != null) {
			this.fileNameGenerator = fileNameGenerator;
		} else {
			this.fileNameGenerator = DefaultConfigurationFactory.createFileNameGenerator();
		}

	}

	public LruDiskCache(File cacheDir, long limitSize) {
		init(cacheDir, limitSize);
		this.fileNameGenerator = DefaultConfigurationFactory.createFileNameGenerator();
	}

	public LruDiskCache(File cacheDir, long limitSize, FileNameGenerator fileNameGenerator) {
		init(cacheDir, limitSize);
		if (fileNameGenerator != null) {
			this.fileNameGenerator = fileNameGenerator;
		} else {
			this.fileNameGenerator = DefaultConfigurationFactory.createFileNameGenerator();
		}

	}

	private void init(File cacheDir, long limitSize) {
		try {
			mDiskCache = DiskLruCache.open(cacheDir, APP_VERSION, limitSize);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public boolean put(String key, Bitmap bitmap, ImageLoaderConfiguration config) {
		DiskLruCache.Editor editor = null;
		key = this.fileNameGenerator.generate(key);
		try {
			editor = mDiskCache.edit(key);
			if (editor == null) {
				return false;
			}
			if (writeBitmapToFile(bitmap, editor, config)) {
//				mDiskCache.flush();
				editor.commit();
                mDiskCache.flush();
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
			return bitmap.compress(config.getImageCompressFormatForDiscCache()
					, config.getImageQualityForDiscCache(), out);
		} finally {
			if (out != null) {
				out.close();
			}
		}
	}

	public DiskLruCache.Snapshot get(String key) {
		key = this.fileNameGenerator.generate(key);
		try {
			return mDiskCache.get(key);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public void clear() {
		try {
			mDiskCache.delete();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

    public File getFile(String key) {
        key = this.fileNameGenerator.generate(key);
        return mDiskCache.getFile(key);
    }

    /**
     * 获得DiskLruCache
     * @return DiskLruCache实例
     */
    public DiskLruCache getDiskCache() {
        return mDiskCache;
    }

}
