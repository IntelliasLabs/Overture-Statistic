/**
 Copyright ©2025 Intellias
 */
package com.intellias.mobility.statistic.util;

import java.net.URI;
import lombok.experimental.UtilityClass;

@UtilityClass
public class S3PathUtils {

  public record S3Path(String bucket, String key) {}

  /**
   * Parses an S3 URI (s3:// or https://) into a bucket and key.
   */
  public S3Path parseS3Uri(URI uri) {
    String bucket;
    String key;
    String scheme = uri.getScheme();
    String host = uri.getHost();

    if ("s3".equalsIgnoreCase(scheme)) {
      bucket = host;
      key = uri.getPath().startsWith("/") ? uri.getPath().substring(1) : uri.getPath();
    } else if ("https".equalsIgnoreCase(scheme) && host != null && host.contains(".s3.")) {
      bucket = host.substring(0, host.indexOf(".s3."));
      key = uri.getPath().replaceFirst("^/", "");
    } else {
      throw new IllegalArgumentException("Unsupported URI scheme for S3 access: " + scheme);
    }

    if (bucket == null || bucket.isBlank() || key == null) {
      throw new IllegalArgumentException("Could not determine bucket or key from S3 URI: " + uri);
    }
    return new S3Path(bucket, key);
  }
}
