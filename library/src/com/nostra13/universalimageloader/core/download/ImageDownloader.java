package com.nostra13.universalimageloader.core.download;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

/**
 * Provides retrieving of {@link InputStream} of image by URI.
 * 
 * @author Sergey Tarasevich (nostra13[at]gmail[dot]com)
 */
public interface ImageDownloader {

	/** Retrieves {@link InputStream} of image by URI. Image can be located as in the network and on local file system. */
	InputStream getStream(String imageUri, Object extra) throws IOException;
//    {
////		String scheme = imageUri.getScheme();
////		if (PROTOCOL_HTTP.equals(scheme) || PROTOCOL_HTTPS.equals(scheme) || PROTOCOL_FTP.equals(scheme)) {
////			return getStreamFromNetwork(imageUri);
////		} else if (PROTOCOL_FILE.equals(scheme)) {
////			return getStreamFromFile(imageUri);
////		} else {
////			return getStreamFromOtherSource(imageUri);
////		}
//	}

    /** Represents supported schemes(protocols) of URI. Provides convenient methods for work with schemes and URIs. */
    public enum Scheme {
        HTTP("http"), HTTPS("https"), FILE("file"), CONTENT("content"), ASSETS("assets"), DRAWABLE("drawable"), UNKNOWN("");

        private String scheme;
        private String uriPrefix;

        Scheme(String scheme) {
            this.scheme = scheme;
            uriPrefix = scheme + "://";
        }

        /**
         * Defines scheme of incoming URI
         *
         * @param uri URI for scheme detection
         * @return Scheme of incoming URI
         */
        public static Scheme ofUri(String uri) {
            if (uri != null) {
                for (Scheme s : values()) {
                    if (s.belongsTo(uri)) {
                        return s;
                    }
                }
            }
            return UNKNOWN;
        }

        private boolean belongsTo(String uri) {
            return uri.startsWith(uriPrefix);
        }

        /** Appends scheme to incoming path */
        public String wrap(String path) {
            return uriPrefix + path;
        }

        /** Removed scheme part ("scheme://") from incoming URI */
        public String crop(String uri) {
            if (!belongsTo(uri)) {
                throw new IllegalArgumentException(String.format("URI [%1$s] doesn't have expected scheme [%2$s]", uri, scheme));
            }
            return uri.substring(uriPrefix.length());
        }
    }
}