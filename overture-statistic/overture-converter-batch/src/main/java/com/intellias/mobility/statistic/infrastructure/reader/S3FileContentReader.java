/**
 Copyright ©2025 Intellias
 */
package com.intellias.mobility.statistic.infrastructure.reader;

import com.intellias.mobility.statistic.util.S3PathUtils;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.hadoop.conf.Configuration;
import org.apache.parquet.hadoop.util.HadoopInputFile;
import org.apache.parquet.io.InputFile;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.sync.ResponseTransformer;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;

@Slf4j
@Component
@RequiredArgsConstructor
public class S3FileContentReader implements FileContentReader {

  private final S3Client s3Client;
  private static final Path TEMP_DIRECTORY = Path.of("target/s3-temp");
  private static final ConcurrentHashMap<String, CachedTempFile> TEMP_FILE_CACHE =
      new ConcurrentHashMap<>();

  @Override
  public boolean supports(URI uri) {
    String scheme = uri.getScheme();
    String host = uri.getHost();
    return ("s3".equalsIgnoreCase(scheme))
        || ("https".equalsIgnoreCase(scheme)
            && host != null
            && host.matches(".*\\.s3\\..*\\.amazonaws\\.com"));
  }

  @Override
  public InputFile readFile(URI uri) {
    S3PathUtils.S3Path s3Path = S3PathUtils.parseS3Uri(uri);
    return downloadSingleFile(s3Path.bucket(), s3Path.key());
  }

  private InputFile downloadSingleFile(String bucket, String key) {
    try {
      log.info("Streaming S3 file from bucket={} key={}", bucket, key);
      Files.createDirectories(TEMP_DIRECTORY);
      CachedTempFile cachedFile =
          TEMP_FILE_CACHE.compute(cacheKey(bucket, key), (cacheKey, existing) -> {
            if (existing != null) {
              existing.retain();
              return existing;
            }

            Path tempFile = null;
            try {
              tempFile = TEMP_DIRECTORY.resolve("s3-download-" + UUID.randomUUID() + ".parquet");
              tempFile.toFile().deleteOnExit();

              s3Client.getObject(
                  GetObjectRequest.builder().bucket(bucket).key(key).build(),
                  ResponseTransformer.toFile(tempFile));

              return new CachedTempFile(cacheKey, tempFile);
            } catch (Exception e) {
              if (tempFile != null) {
                try {
                  Files.deleteIfExists(tempFile);
                } catch (IOException cleanupException) {
                  log.warn("Could not delete temporary file: {}", tempFile, cleanupException);
                }
              }
              throw new RuntimeException("Failed to read S3 file", e);
            }
          });

      return new CachedS3InputFile(cachedFile);

    } catch (Exception e) {
      log.error("Failed to read S3 file at s3://{}/{}", bucket, key, e);
      throw new RuntimeException("Failed to read S3 file", e);
    }
  }

  private static String cacheKey(String bucket, String key) {
    return bucket + "/" + key;
  }

  private static final class CachedS3InputFile implements InputFile, AutoCloseable {
    private final CachedTempFile cachedTempFile;

    private CachedS3InputFile(CachedTempFile cachedTempFile) {
      this.cachedTempFile = cachedTempFile;
    }

    @Override
    public long getLength() throws IOException {
      return delegate().getLength();
    }

    @Override
    public org.apache.parquet.io.SeekableInputStream newStream() throws IOException {
      return delegate().newStream();
    }

    @Override
    public void close() {
      cachedTempFile.release();
    }

    private HadoopInputFile delegate() throws IOException {
      return HadoopInputFile.fromPath(
          new org.apache.hadoop.fs.Path(cachedTempFile.tempFile.toUri()), new Configuration());
    }
  }

  private static final class CachedTempFile {
    private final String cacheKey;
    private final Path tempFile;
    private final AtomicInteger refCount = new AtomicInteger(1);

    private CachedTempFile(String cacheKey, Path tempFile) {
      this.cacheKey = cacheKey;
      this.tempFile = tempFile;
    }

    private void retain() {
      refCount.incrementAndGet();
    }

    private void release() {
      int remaining = refCount.decrementAndGet();
      if (remaining > 0) {
        return;
      }

      TEMP_FILE_CACHE.remove(cacheKey, this);
      try {
        Files.deleteIfExists(tempFile);
      } catch (IOException cleanupException) {
        log.warn("Could not delete temporary file: {}", tempFile, cleanupException);
      }
    }
  }
}
