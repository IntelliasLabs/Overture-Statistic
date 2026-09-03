/**
 Copyright ©2025 Intellias
 */
package com.intellias.mobility.statistic.batch.job.partitioner;

import com.intellias.mobility.statistic.infrastructure.reader.FileReaderFactory;
import com.intellias.mobility.statistic.util.S3PathUtils;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.extern.slf4j.Slf4j;
import org.apache.parquet.format.converter.ParquetMetadataConverter;
import org.apache.parquet.hadoop.ParquetFileReader;
import org.apache.parquet.hadoop.metadata.BlockMetaData;
import org.apache.parquet.hadoop.metadata.ParquetMetadata;
import org.apache.parquet.io.InputFile;
import org.jetbrains.annotations.NotNull;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;

/**
 * A Partitioner that creates tasks for parallel processing by splitting files into smaller chunks.
 * <p>
 * It finds all .parquet files in a given S3 or local path. For each file, it reads
 * the metadata to get the total number of rows and creates a separate task for each chunk of rows.
 * <p>
 * This helps to process large and small files together more efficiently and uses all
 * available threads.
 */
@Component
@StepScope
@Slf4j
public class MultiResourcePartitioner implements Partitioner {

  @Autowired(required = false)
  private S3Client s3Client;

  private final String inputPath;
  private final FileReaderFactory fileReaderFactory;

  @Value("${batch.jobs.partition-size.rows:100000}")
  private int partitionSizeRows;

  public MultiResourcePartitioner(
      @Value("#{jobParameters['inputPath']}") String inputPath,
      FileReaderFactory fileReaderFactory) {
    this.inputPath = inputPath;
    this.fileReaderFactory = fileReaderFactory;
  }

  /**
   * Creates work partitions for parallel processing based on row counts.
   * <p>
   * This method finds all Parquet files in the input path, reads the
   * total number of rows from each file's metadata, and then splits each
   * file into smaller chunks. Each chunk becomes a separate partition
   * for a worker thread to process.
   **/
  @Override
  public @NotNull Map<String, ExecutionContext> partition(int gridSize) {
    log.info("Partitioning path: {} based on row counts.", inputPath);

    List<String> filePaths = findFilePaths();
    Map<String, ExecutionContext> partitions = new HashMap<>();
    AtomicInteger partitionCounter = new AtomicInteger(0);

    for (String filePath : filePaths) {
      try {
        long totalRows = getRowCountFromMetadata(filePath);
        log.info(
            "File [{}] contains {} rows. Slicing into partitions of {} rows.",
            filePath,
            totalRows,
            partitionSizeRows);

        if (totalRows == 0) {
          log.warn("File {} is empty, skipping.", filePath);
          continue;
        }

        for (long start = 0; start < totalRows; start += partitionSizeRows) {
          long end = Math.min(start + partitionSizeRows - 1, totalRows - 1);

          ExecutionContext context = new ExecutionContext();
          context.putString("filePath", filePath);
          context.putLong("startRow", start);
          context.putLong("endRow", end);

          String partitionName = "partition:" + partitionCounter.getAndIncrement();
          partitions.put(partitionName, context);
          log.debug(
              "Created partition {} for file [{}], rows {} to {}",
              partitionName,
              filePath,
              start,
              end);
        }
      } catch (Exception e) {
        log.warn("Could not process file: {}. Skipping. Reason: {}", filePath, e.getMessage());
      }
    }

    logResult(partitions.size());
    return partitions;
  }

  private List<String> findFilePaths() {
    if (inputPath.toLowerCase().startsWith("s3:")
        || (inputPath.startsWith("https:") && inputPath.contains(".s3."))) {
      return findS3FilePaths();
    } else {
      return findLocalFilePaths();
    }
  }

  /**
   * Finds all .parquet files at a given S3 path.
   */
  private List<String> findS3FilePaths() {
    if (s3Client == null) {
      throw new IllegalStateException(
          "S3Client bean is required for reading from S3, but it was not found. Please check your configuration.");
    }
    if (!inputPath.contains("/type=")) {
      throw new IllegalArgumentException(
          "Input path for S3 must specify a feature type (e.g., '/theme=places/type=place/'). "
              + "Processing an entire theme is not supported. Path provided: " + inputPath);
    }

    List<String> filePaths = new ArrayList<>();
    S3PathUtils.S3Path s3Path = S3PathUtils.parseS3Uri(URI.create(inputPath));
    String bucket = s3Path.bucket();
    String key = s3Path.key();

    if (key.toLowerCase().endsWith(".parquet")) {
      log.info("Path is a single S3 file.");
      filePaths.add(inputPath);
    } else {
      log.info("Path is an S3 directory, scanning for files...");
      ListObjectsV2Request request =
          ListObjectsV2Request.builder().bucket(bucket).prefix(key).build();
      s3Client.listObjectsV2Paginator(request).contents().forEach(s3Object -> {
        String fullKey = s3Object.key();
        if (fullKey.toLowerCase().endsWith(".parquet")) {
          filePaths.add("s3://" + bucket + "/" + fullKey);
        }
      });
    }
    return filePaths;
  }

  /**
   * Finds all .parquet files at a given local path.
   */
  private List<String> findLocalFilePaths() {
    List<String> filePaths = new ArrayList<>();

    try {
      Path path;

      if (inputPath.startsWith("classpath:")) {
        // Handle classpath resource
        String resourcePath = inputPath.replace("classpath:", "");
        var resourceUrl = getClass().getClassLoader().getResource(resourcePath);
        if (resourceUrl == null) {
          throw new IllegalArgumentException("Classpath resource not found: " + resourcePath);
        }
        path = Paths.get(resourceUrl.toURI());
      } else {
        // Resolve relative paths to absolute paths
        Path rawPath = Paths.get(inputPath);
        if (!rawPath.isAbsolute()) {
          rawPath = Paths.get(System.getProperty("user.dir")).resolve(rawPath);
        }
        path = rawPath;
      }

      if (Files.isRegularFile(path)) {
        log.info("Path is a single local file: {}", path);
        filePaths.add(path.toUri().toString());
      } else if (Files.isDirectory(path)) {
        log.info("Path is a local directory, scanning for files...");
        try (var stream = Files.walk(path)) {
          List<String> foundPaths = stream
              .filter(
                  p -> p.toString().toLowerCase().endsWith(".parquet") && Files.isRegularFile(p))
              .map(p -> p.toUri().toString())
              .toList();
          filePaths.addAll(foundPaths);
        }
      }
    } catch (Exception e) {
      throw new RuntimeException("Failed to resolve input path: " + inputPath, e);
    }

    return filePaths;
  }

  private long getRowCountFromMetadata(String filePath) throws IOException {
    if (isS3Path(filePath)) {
      return getS3RowCountFromFooter(filePath);
    }

    InputFile inputFile = fileReaderFactory.readFile(filePath);
    try (ParquetFileReader reader = ParquetFileReader.open(inputFile)) {
      ParquetMetadata metadata = reader.getFooter();
      return metadata.getBlocks().stream().mapToLong(BlockMetaData::getRowCount).sum();
    } finally {
      if (inputFile instanceof AutoCloseable closeable) {
        try {
          closeable.close();
        } catch (Exception e) {
          log.warn("Failed to release cached input file for {}", filePath, e);
        }
      }
    }
  }

  private long getS3RowCountFromFooter(String filePath) throws IOException {
    if (s3Client == null) {
      throw new IllegalStateException("S3Client bean is required for S3 metadata reads.");
    }

    S3PathUtils.S3Path s3Path = S3PathUtils.parseS3Uri(URI.create(filePath));
    var headObject = s3Client.headObject(
        HeadObjectRequest.builder().bucket(s3Path.bucket()).key(s3Path.key()).build());

    long objectSize = headObject.contentLength();
    if (objectSize < 8) {
      throw new IOException("Invalid Parquet file: footer is too small for " + filePath);
    }

    byte[] trailer = readS3Range(s3Path.bucket(), s3Path.key(), objectSize - 8, objectSize - 1);
    int footerLength =
        ByteBuffer.wrap(trailer, 0, 4).order(ByteOrder.LITTLE_ENDIAN).getInt();
    String magic = new String(trailer, 4, 4, StandardCharsets.US_ASCII);
    if (!"PAR1".equals(magic)) {
      throw new IOException("Invalid Parquet magic in footer for " + filePath);
    }

    long footerStart = objectSize - footerLength - 8;
    if (footerStart < 0) {
      throw new IOException("Invalid Parquet footer length for " + filePath);
    }

    byte[] footerBytes = readS3Range(s3Path.bucket(), s3Path.key(), footerStart, objectSize - 9);
    try (ByteArrayInputStream footerInput = new ByteArrayInputStream(footerBytes)) {
      ParquetMetadata metadata = new ParquetMetadataConverter().readParquetMetadata(footerInput);
      return metadata.getBlocks().stream().mapToLong(BlockMetaData::getRowCount).sum();
    }
  }

  private byte[] readS3Range(String bucket, String key, long startInclusive, long endInclusive)
      throws IOException {
    try (var response = s3Client.getObject(GetObjectRequest.builder()
        .bucket(bucket)
        .key(key)
        .range("bytes=" + startInclusive + "-" + endInclusive)
        .build())) {
      return response.readAllBytes();
    } catch (Exception e) {
      throw new IOException(
          "Failed to read Parquet footer range from s3://" + bucket + "/" + key, e);
    }
  }

  private boolean isS3Path(String filePath) {
    return filePath.toLowerCase().startsWith("s3:")
        || (filePath.startsWith("https:") && filePath.contains(".s3."));
  }

  private void logResult(int partitionCount) {
    if (partitionCount == 0) {
      log.warn("No .parquet files found for path: {}", inputPath);
    } else {
      log.info("Created {} partitions to process.", partitionCount);
    }
  }
}
