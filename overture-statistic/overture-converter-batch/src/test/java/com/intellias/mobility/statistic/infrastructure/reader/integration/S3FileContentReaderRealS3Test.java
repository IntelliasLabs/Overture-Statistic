/**
 Copyright ©2025 Intellias
 */
package com.intellias.mobility.statistic.infrastructure.reader.integration;

import static org.junit.jupiter.api.Assertions.*;

import com.intellias.mobility.statistic.infrastructure.reader.S3FileContentReader;
import java.net.URI;
import org.apache.parquet.io.InputFile;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.auth.credentials.AnonymousCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

public class S3FileContentReaderRealS3Test {

  private static final Logger log = LoggerFactory.getLogger(S3FileContentReaderRealS3Test.class);

  private final S3Client s3Client = S3Client.builder()
      .endpointOverride(URI.create("https://s3.amazonaws.com"))
      .credentialsProvider(AnonymousCredentialsProvider.create())
      .region(Region.US_WEST_2)
      .build();

  @Test
  @Disabled(
      "Relies on a mutable public S3 object; run manually when validating against live AWS data.")
  void testReadSingleFile_httpsS3() throws Exception {
    URI uri = URI.create(
        "https://overturemaps-us-west-2.s3.us-west-2.amazonaws.com/release/2025-07-23.0/theme=base/type=bathymetry/part-00000-848ba1eb-b6dc-4096-822b-0376c6deba7c-c000.zstd.parquet");

    S3FileContentReader reader = new S3FileContentReader(s3Client);
    InputFile file = reader.readFile(uri);

    byte[] preview = file.newStream().readNBytes(1024);
    assertNotNull(preview);
    assertTrue(preview.length > 0);

    log.info("Read {} bytes from single file.", preview.length);
    log.info("Preview:\n{}", new String(preview));
  }
}
