package com.nostra13.universalimageloader.core;

import static com.nostra13.universalimageloader.core.ImageLoader.LOG_CACHE_IMAGE_IN_MEMORY;
import static com.nostra13.universalimageloader.core.ImageLoader.LOG_CACHE_IMAGE_ON_DISC;
import static com.nostra13.universalimageloader.core.ImageLoader.LOG_DELAY_BEFORE_LOADING;
import static com.nostra13.universalimageloader.core.ImageLoader.LOG_GET_IMAGE_FROM_MEMORY_CACHE_AFTER_WAITING;
import static com.nostra13.universalimageloader.core.ImageLoader.LOG_LOAD_IMAGE_FROM_DISC_CACHE;
import static com.nostra13.universalimageloader.core.ImageLoader.LOG_LOAD_IMAGE_FROM_INTERNET;
import static com.nostra13.universalimageloader.core.ImageLoader.LOG_POSTPROCESS_IMAGE;
import static com.nostra13.universalimageloader.core.ImageLoader.LOG_PREPROCESS_IMAGE;
import static com.nostra13.universalimageloader.core.ImageLoader.LOG_RESUME_AFTER_PAUSE;
import static com.nostra13.universalimageloader.core.ImageLoader.LOG_START_DISPLAY_IMAGE_TASK;
import static com.nostra13.universalimageloader.core.ImageLoader.LOG_TASK_CANCELLED;
import static com.nostra13.universalimageloader.core.ImageLoader.LOG_TASK_INTERRUPTED;
import static com.nostra13.universalimageloader.core.ImageLoader.LOG_WAITING_FOR_IMAGE_LOADED;
import static com.nostra13.universalimageloader.core.ImageLoader.LOG_WAITING_FOR_RESUME;

import java.io.IOException;
import java.io.InputStream;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.SystemClock;
import android.widget.ImageView;


import com.nostra13.universalimageloader.cache.disc.LruDiskCache;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageLoadingListener;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.assist.ImageSize;
import com.nostra13.universalimageloader.core.assist.ViewScaleType;
import com.nostra13.universalimageloader.core.download.ImageDownloader;
import com.nostra13.universalimageloader.utils.DiskLruCache.DiskLruCache;

import com.nostra13.universalimageloader.utils.L;

/**
 * Presents load'n'display image task. Used to load image from Internet or file system, decode it to {@link Bitmap}, and
 * display it in {@link ImageView} through {@link DisplayBitmapTask}.
 * 
 * @author Sergey Tarasevich (nostra13[at]gmail[dot]com)
 * @see ImageLoaderConfiguration
 * @see ImageLoadingInfo
 */
final class LoadAndDisplayImageTask implements Runnable {

	private static final int ATTEMPT_COUNT_TO_DECODE_BITMAP = 3;
	private static final int BUFFER_SIZE = 8 * 1024; // 8 Kb

    private final ImageLoaderEngine engine;
    private final ImageLoadingInfo imageLoadingInfo;
    private final Handler handler;

	// Helper references
    private final ImageLoaderConfiguration configuration;
    private final ImageDownloader networkDeniedDownloader;
    private final ImageDownloader downloader;
    private final ImageDownloader slowNetworkDownloader;
	private final boolean loggingEnabled;
	private final String uri;
	private final String memoryCacheKey;
	private final ImageView imageView;
	private final ImageSize targetSize;
	private final DisplayImageOptions options;
	private final ImageLoadingListener listener;

	public LoadAndDisplayImageTask(ImageLoaderEngine engine, ImageLoadingInfo imageLoadingInfo, Handler handler) {
		this.engine = engine;
		this.imageLoadingInfo = imageLoadingInfo;
		this.handler = handler;

        configuration = engine.configuration;
        downloader = configuration.downloader;
        networkDeniedDownloader = configuration.networkDeniedDownloader;
        slowNetworkDownloader = configuration.slowNetworkDownloader;
        loggingEnabled = configuration.loggingEnabled;
        uri = imageLoadingInfo.uri;
        memoryCacheKey = imageLoadingInfo.memoryCacheKey;
        imageView = imageLoadingInfo.imageView;
        targetSize = imageLoadingInfo.targetSize;
        options = imageLoadingInfo.options;
        listener = imageLoadingInfo.listener;
	}

	@Override
	public void run() {
        AtomicBoolean pause = engine.getPause();
		if (pause.get()) {
			synchronized (pause) {
				if (loggingEnabled) L.i(LOG_WAITING_FOR_RESUME, memoryCacheKey);
				try {
					pause.wait();
				} catch (InterruptedException e) {
					L.e(LOG_TASK_INTERRUPTED, memoryCacheKey);
					return;
				}
				if (loggingEnabled) L.i(LOG_RESUME_AFTER_PAUSE, memoryCacheKey);
			}
		}
		if (checkTaskIsNotActual()) return;

		if (options.shouldDelayBeforeLoading()) {
            log(LOG_DELAY_BEFORE_LOADING, options.getDelayBeforeLoading(), memoryCacheKey);
			try {
				Thread.sleep(options.getDelayBeforeLoading());
			} catch (InterruptedException e) {
				L.e(LOG_TASK_INTERRUPTED, memoryCacheKey);
				return;
			}

			if (checkTaskIsNotActual()) return;
		}

		ReentrantLock loadFromUriLock = imageLoadingInfo.loadFromUriLock;
        log(LOG_START_DISPLAY_IMAGE_TASK, memoryCacheKey);
        if (loadFromUriLock.isLocked()) {
            log(LOG_WAITING_FOR_IMAGE_LOADED, memoryCacheKey);
        }

		loadFromUriLock.lock();
		Bitmap bmp;
		try {
			if (checkTaskIsNotActual()) return;

			bmp = configuration.memoryCache.get(memoryCacheKey);
			if (bmp == null) {
				bmp = tryLoadBitmap();
				if (bmp == null) return;

				if (checkTaskIsNotActual() || checkTaskIsInterrupted()) return;

                if (options.shouldPreProcess()) {
                    log(LOG_PREPROCESS_IMAGE, memoryCacheKey);
                    bmp = options.getPreProcessor().process(bmp);
                }

				if (options.isCacheInMemory()) {
					log(LOG_CACHE_IMAGE_IN_MEMORY, memoryCacheKey);

					configuration.memoryCache.put(memoryCacheKey, bmp);
				}
			} else {
				log(LOG_GET_IMAGE_FROM_MEMORY_CACHE_AFTER_WAITING, memoryCacheKey);
			}
            if (options.shouldPostProcess()) {
                log(LOG_POSTPROCESS_IMAGE, memoryCacheKey);
                bmp = options.getPostProcessor().process(bmp);
            }
		} finally {
			loadFromUriLock.unlock();
		}

		if (checkTaskIsNotActual() || checkTaskIsInterrupted()) return;

		DisplayBitmapTask displayBitmapTask = new DisplayBitmapTask(bmp, imageLoadingInfo, engine);
		displayBitmapTask.setLoggingEnabled(loggingEnabled);
		handler.post(displayBitmapTask);
	}

	/**
	 * Check whether the image URI of this task matches to image URI which is actual for current ImageView at this
	 * moment and fire {@link ImageLoadingListener#onLoadingCancelled()} event if it doesn't.
	 */
	private boolean checkTaskIsNotActual() {
		String currentCacheKey = engine.getLoadingUriForView(imageView);
		// Check whether memory cache key (image URI) for current ImageView is actual. 
		// If ImageView is reused for another task then current task should be cancelled.
		boolean imageViewWasReused = !memoryCacheKey.equals(currentCacheKey);
		if (imageViewWasReused) {
			handler.post(new Runnable() {
				@Override
				public void run() {
					listener.onLoadingCancelled(uri, imageView);
				}
			});
		}

		if (loggingEnabled && imageViewWasReused) L.i(LOG_TASK_CANCELLED, memoryCacheKey);
		return imageViewWasReused;
	}

	/** Check whether the current task was interrupted */
	private boolean checkTaskIsInterrupted() {
		boolean interrupted = Thread.interrupted();
		if (loggingEnabled && interrupted) L.e(LOG_TASK_INTERRUPTED, memoryCacheKey);
		return interrupted;
	}

	private Bitmap tryLoadBitmap() {
		LruDiskCache discCache = configuration.discCache;
		DiskLruCache.Snapshot snapshot = discCache.get(uri);
//		File imageFile = discCache.get(uri);

		Bitmap bitmap = null;
		try {
			// Try to load image from disc cache
			if (snapshot != null) {
				if (loggingEnabled) L.i(LOG_LOAD_IMAGE_FROM_DISC_CACHE, memoryCacheKey);

				Bitmap b = decodeImage(snapshot);
				if (b != null) {
					return b;
				}
			}

			// Load image from Web
			if (loggingEnabled) L.i(LOG_LOAD_IMAGE_FROM_INTERNET, memoryCacheKey);

			if (options.isCacheOnDisc()) {
				if (loggingEnabled) L.i(LOG_CACHE_IMAGE_ON_DISC, memoryCacheKey);

				Bitmap b = getBitmap(uri);
//				saveImageOnDisc(imageFile);
				if (b != null) {
					discCache.put(uri, b, configuration);
                    if (snapshot != null) {
                        snapshot.close();
                    }
                    snapshot = discCache.get(uri);
                    bitmap = decodeImage(snapshot.getInputStream(0));
				}

//				imageUriForDecoding = imageFile.toURI();
			} else {
				bitmap = decodeImage(downloader.getStream(uri, null));
			}


			if (bitmap == null) {
                fireImageLoadingFailedEvent(FailReason.FailType.DECODING_ERROR, null);
			}
		} catch (IllegalStateException e) {
            fireImageLoadingFailedEvent(FailReason.FailType.NETWORK_DENIED, null);
        } catch (IOException e) {
			L.e(e);
            fireImageLoadingFailedEvent(FailReason.FailType.IO_ERROR, e);
//			if (sna.exists()) {
//				imageFile.delete();
//			}
		} catch (OutOfMemoryError e) {
            L.e(e);
            fireImageLoadingFailedEvent(FailReason.FailType.OUT_OF_MEMORY, e);
        } catch (Throwable e) {
            L.e(e);
            fireImageLoadingFailedEvent(FailReason.FailType.UNKNOWN, e);
        } finally {
			if (snapshot != null) {
				snapshot.close();
			}
		}
		return bitmap;
	}

//	private Bitmap decodeImage(URI imageUri) throws IOException {
//		Bitmap bmp = null;
//
//		if (configuration.handleOutOfMemory) {
//			bmp = decodeWithOOMHandling(imageUri);
//		} else {
//			ImageDecoder decoder = new ImageDecoder(imageUri, downloader, options);
//			decoder.setLoggingEnabled(loggingEnabled);
//			ViewScaleType viewScaleType = ViewScaleType.fromImageView(imageView);
//			bmp = decoder.decode(targetSize, options.getImageScaleType(), viewScaleType);
//		}
//		return bmp;
//	}

	private Bitmap decodeImage(DiskLruCache.Snapshot snapshot) throws IOException {
		Bitmap bmp = null;

		if (configuration.handleOutOfMemory) {
			bmp = decodeWithOOMHandling(snapshot);
		} else {
			ImageDecoder decoder = new ImageDecoder(snapshot, downloader, options);
			decoder.setLoggingEnabled(loggingEnabled);
			ViewScaleType viewScaleType = ViewScaleType.fromImageView(imageView);
			bmp = decoder.decode(targetSize, options.getImageScaleType(), viewScaleType);
		}
		return bmp;
	}

	private Bitmap decodeWithOOMHandling(DiskLruCache.Snapshot imageUri) throws IOException {
		Bitmap result = null;
		ImageDecoder decoder = new ImageDecoder(imageUri, downloader, options);
		decoder.setLoggingEnabled(loggingEnabled);
		for (int attempt = 1; attempt <= ATTEMPT_COUNT_TO_DECODE_BITMAP; attempt++) {
			try {
				ViewScaleType viewScaleType = ViewScaleType.fromImageView(imageView);
				result = decoder.decode(targetSize, options.getImageScaleType(), viewScaleType);
			} catch (OutOfMemoryError e) {
				L.e(e);

				switch (attempt) {
					case 1:
						System.gc();
						break;
					case 2:
						configuration.memoryCache.clear();
						System.gc();
						break;
					case 3:
						throw e;
				}
				// Wait some time while GC is working
				SystemClock.sleep(attempt * 1000);
				continue;
			}
			break;
		}
		return result;
	}


	private Bitmap decodeImage(InputStream inputStream) throws IOException {
		Bitmap bmp = null;

		if (configuration.handleOutOfMemory) {
			bmp = decodeWithOOMHandling(inputStream);
		} else {
			ImageDecoder decoder = new ImageDecoder(inputStream, getDownloader(), options);
			decoder.setLoggingEnabled(loggingEnabled);
			ViewScaleType viewScaleType = ViewScaleType.fromImageView(imageView);
			bmp = decoder.decode(targetSize, options.getImageScaleType(), viewScaleType);
		}
		return bmp;
	}

	private Bitmap decodeWithOOMHandling(InputStream inputStream) throws IOException {
		Bitmap result = null;
		ImageDecoder decoder = new ImageDecoder(inputStream, getDownloader(), options);
		decoder.setLoggingEnabled(loggingEnabled);
		for (int attempt = 1; attempt <= ATTEMPT_COUNT_TO_DECODE_BITMAP; attempt++) {
			try {
				ViewScaleType viewScaleType = ViewScaleType.fromImageView(imageView);
				result = decoder.decode(targetSize, options.getImageScaleType(), viewScaleType);
			} catch (OutOfMemoryError e) {
				L.e(e);

				switch (attempt) {
					case 1:
						System.gc();
						break;
					case 2:
						configuration.memoryCache.clear();
						System.gc();
						break;
					case 3:
						throw e;
				}
				// Wait some time while GC is working
				SystemClock.sleep(attempt * 1000);
				continue;
			}
			break;
		}
		return result;
	}

	private Bitmap getBitmap(String imageUri) throws IOException{
		int width = configuration.maxImageWidthForDiscCache;
		int height = configuration.maxImageHeightForDiscCache;
		InputStream is = getDownloader().getStream(imageUri, options.getExtraForDownloader());
		if (is != null) {
			try {
				if (width > 0 || height > 0) {
					ImageSize targetImageSize = new ImageSize(width, height);
					if (loggingEnabled) {
						L.d("load from internet and targetsize");
					}

					ImageDecoder decoder = new ImageDecoder(is, downloader, options);
					decoder.setLoggingEnabled(loggingEnabled);
					return decoder.decode(targetImageSize, ImageScaleType.IN_SAMPLE_INT, ViewScaleType.FIT_INSIDE);

				}
				if (loggingEnabled) {
					L.d("load from internet and no targetsize");
				}
				return BitmapFactory.decodeStream(is);
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				try {
					is.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		if (loggingEnabled) {
			L.d("bitmap is null");
		}
		return null;
}

//	private void saveImageOnDisc(File targetFile) throws IOException, URISyntaxException {
//		int width = configuration.maxImageWidthForDiscCache;
//		int height = configuration.maxImageHeightForDiscCache;
//		if (width > 0 || height > 0) {
//			// Download, decode, compress and save image
//			ImageSize targetImageSize = new ImageSize(width, height);
//			ImageDecoder decoder = new ImageDecoder(new URI(uri), downloader, options);
//			decoder.setLoggingEnabled(loggingEnabled);
//			Bitmap bmp = decoder.decode(targetImageSize, ImageScaleType.IN_SAMPLE_INT, ViewScaleType.FIT_INSIDE);
//
//			OutputStream os = new BufferedOutputStream(new FileOutputStream(targetFile), BUFFER_SIZE);
//			boolean compressedSuccessfully = bmp.compress(configuration.imageCompressFormatForDiscCache, configuration.imageQualityForDiscCache, os);
//			if (compressedSuccessfully) {
//				bmp.recycle();
//				return;
//			}
//		}
//
//		// If previous compression wasn't needed or failed
//		// Download and save original image
//		InputStream is = downloader.getStream(new URI(uri));
//		try {
//			OutputStream os = new BufferedOutputStream(new FileOutputStream(targetFile), BUFFER_SIZE);
//			try {
//				FileUtils.copyStream(is, os);
//			} finally {
//				os.close();
//			}
//		} finally {
//			is.close();
//		}
//	}

	private void fireImageLoadingFailedEvent(final FailReason.FailType failType, final Throwable failCause) {
		if (!Thread.interrupted()) {
			handler.post(new Runnable() {
				@Override
				public void run() {
                    if (options.shouldShowImageOnFail()) {
                        imageView.setImageResource(options.getImageOnFail());
                    }
                    listener.onLoadingFailed(uri, imageView, new FailReason(failType, failCause));
				}
			});
		}
	}

    private ImageDownloader getDownloader() {
        ImageDownloader d;
        if (engine.isNetworkDenied()) {
            d = networkDeniedDownloader;
        } else if (engine.isSlowNetwork()) {
            d = slowNetworkDownloader;
        } else {
            d = downloader;
        }
        return d;
    }

    String getLoadingUri() {
        return uri;
    }

    private void log(String message, Object... args) {
        if (loggingEnabled) L.i(message, args);
    }
}
