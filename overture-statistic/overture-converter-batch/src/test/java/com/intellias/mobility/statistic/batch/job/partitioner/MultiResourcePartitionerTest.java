/**
 Copyright ©2025 Intellias
 */
package com.intellias.mobility.statistic.batch.job.partitioner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.intellias.mobility.statistic.infrastructure.reader.FileReaderFactory;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import org.apache.parquet.hadoop.ParquetFileReader;
import org.apache.parquet.hadoop.metadata.BlockMetaData;
import org.apache.parquet.hadoop.metadata.ParquetMetadata;
import org.apache.parquet.io.InputFile;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.test.util.ReflectionTestUtils;
import software.amazon.awssdk.services.s3.S3Client;

@ExtendWith(MockitoExtension.class)
class MultiResourcePartitionerTest {

  @Mock
  private S3Client s3Client;

  @Mock
  private FileReaderFactory fileReaderFactory;

  @Mock
  private InputFile mockInputFile;

  @Mock
  private ParquetFileReader parquetFileReader;

  @Mock
  private ParquetMetadata parquetMetadata;

  @TempDir
  private Path tempDir;

  private MultiResourcePartitioner partitioner;
  private MockedStatic<ParquetFileReader> readerMockedStatic;

  @BeforeEach
  void setUp() {
    readerMockedStatic = Mockito.mockStatic(ParquetFileReader.class);
    readerMockedStatic
        .when(() -> ParquetFileReader.open(any(InputFile.class)))
        .thenReturn(parquetFileReader);

    lenient().when(parquetFileReader.getFooter()).thenReturn(parquetMetadata);
    lenient().when(fileReaderFactory.readFile(any(String.class))).thenReturn(mockInputFile);
  }

  @AfterEach
  void tearDown() {
    readerMockedStatic.close();
  }

  @Test
  @DisplayName("Should create multiple partitions based on row count for a single large file")
  void shouldCreateMultiplePartitionsForSingleFile() throws IOException {
    // Given
    Path file = Files.createFile(tempDir.resolve("large_file.parquet"));
    String inputPath = file.toString();
    partitioner = new MultiResourcePartitioner(inputPath, fileReaderFactory);
    ReflectionTestUtils.setField(partitioner, "partitionSizeRows", 100_000);

    BlockMetaData block = mock(BlockMetaData.class);
    when(block.getRowCount()).thenReturn(250_000L);
    when(parquetMetadata.getBlocks()).thenReturn(List.of(block));

    // When
    Map<String, ExecutionContext> partitions = partitioner.partition(10);

    // Then
    assertThat(partitions).hasSize(3);
  }

  @Test
  @DisplayName("Should correctly partition multiple files with different sizes")
  void shouldPartitionMultipleFiles() throws IOException {
    // Given
    Files.createFile(tempDir.resolve("small.parquet"));
    Files.createFile(tempDir.resolve("large.parquet"));
    String inputPath = tempDir.toString();
    partitioner = new MultiResourcePartitioner(inputPath, fileReaderFactory);
    ReflectionTestUtils.setField(partitioner, "partitionSizeRows", 100_000);

    BlockMetaData smallBlock = mock(BlockMetaData.class);
    when(smallBlock.getRowCount()).thenReturn(20_000L);
    List<BlockMetaData> smallFileBlocks = List.of(smallBlock);

    BlockMetaData largeBlock1 = mock(BlockMetaData.class);
    when(largeBlock1.getRowCount()).thenReturn(100_000L);
    BlockMetaData largeBlock2 = mock(BlockMetaData.class);
    when(largeBlock2.getRowCount()).thenReturn(150_000L);
    List<BlockMetaData> largeFileBlocks = List.of(largeBlock1, largeBlock2);

    when(parquetMetadata.getBlocks()).thenReturn(smallFileBlocks).thenReturn(largeFileBlocks);

    // When
    Map<String, ExecutionContext> partitions = partitioner.partition(10);

    // Then
    assertThat(partitions).hasSize(4);
  }

  @Test
  @DisplayName("S3: Should throw exception if type is not specified")
  void shouldThrowExceptionForS3PathWithoutType() {
    // Given
    String inputPath = "s3://my-bucket/theme=places/";
    partitioner = new MultiResourcePartitioner(inputPath, fileReaderFactory);
    ReflectionTestUtils.setField(partitioner, "s3Client", s3Client);

    // When & Then
    var exception = assertThrows(IllegalArgumentException.class, () -> partitioner.partition(10));
    assertThat(exception.getMessage()).contains("Input path for S3 must specify a feature type");
  }

  @Test
  @DisplayName("Local: Should return zero partitions for an empty folder")
  void shouldReturnZeroPartitionsForEmptyLocalFolder() {
    // Given
    String inputPath = tempDir.toString();
    partitioner = new MultiResourcePartitioner(inputPath, fileReaderFactory);

    // When
    Map<String, ExecutionContext> partitions = partitioner.partition(10);

    // Then
    assertThat(partitions).isEmpty();
  }
}
