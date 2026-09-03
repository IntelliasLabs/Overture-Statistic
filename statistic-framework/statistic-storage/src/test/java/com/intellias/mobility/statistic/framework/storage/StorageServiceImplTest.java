/**
 Copyright ©2025 Intellias
 */
package com.intellias.mobility.statistic.framework.storage;

import static org.junit.jupiter.api.Assertions.*;

import com.intellias.mobility.statistic.framework.TestcontainersConfiguration;
import com.intellias.mobility.statistic.framework.elastic.ElasticManageClient;
import com.intellias.mobility.statistic.framework.property.model.PointFeatureProperty;
import com.intellias.mobility.statistic.framework.range.RangeDocument;
import com.intellias.statistic.model.feature.*;
import com.intellias.statistic.model.geometry.*;
import com.intellias.statistic.model.geometry.MultiLineGeometry;
import java.util.Date;
import java.util.List;
import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest()
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class StorageServiceImplTest extends TestcontainersConfiguration {
  @Autowired
  private ElasticsearchOperations elasticsearchOperations;

  @Autowired
  private StorageService storageService;

  @Autowired
  private ElasticManageClient elasticManageClient;

  @AfterAll
  void cleanUp() {
    deleteAllIndexes(elasticManageClient);
  }

  @DisplayName("Should store Point Feature correctly")
  @SneakyThrows
  @Test
  void savePointTest() {
    var coordinates = new LonLat(6.134128158472095, 49.593476516900296);

    PointGeometry point = new PointGeometry(coordinates);

    var properties = new PointFeatureProperties(
        "v1", "Point", new Date(), List.of(new FeatureProperty("key", List.of("value"))));

    PointFeature feature = new PointFeature("id1", point, properties);

    storageService.save(feature, "point");
    refreshIndex("statistic-point-point", elasticsearchOperations);

    SearchHits<PointFeature> search = elasticsearchOperations.search(
        Query.findAll(), PointFeature.class, IndexCoordinates.of("statistic-point-point"));

    assertEquals(1, search.getTotalHits());
    assertEquals(feature, search.getSearchHit(0).getContent());
  }

  @DisplayName("Should store Point Feature Properties correctly")
  @SneakyThrows
  @Test
  void savePointPropertiesTest() {
    var coordinates = new LonLat(6.134128158472095, 49.593476516900296);

    PointGeometry point = new PointGeometry(coordinates);

    var properties = new PointFeatureProperties(
        "v1", "Point", new Date(), List.of(new FeatureProperty("key", List.of("value"))));

    PointFeature feature = new PointFeature("id1", point, properties);

    storageService.save(feature, "point2");
    refreshIndex("statistic-point-point2-feature-properties", elasticsearchOperations);

    SearchHits<PointFeatureProperty> search = elasticsearchOperations.search(
        Query.findAll(),
        PointFeatureProperty.class,
        IndexCoordinates.of("statistic-point-point2-feature-properties"));

    assertEquals(1, search.getTotalHits());
    var expectedFeatureProperty = new PointFeatureProperty(
        "key",
        "value",
        "v1",
        new Date(),
        "id1",
        "Point",
        new PointGeometry(6.134128158472095, 49.593476516900296));

    var featureProperty = search.getSearchHit(0).getContent();

    assertEquals(expectedFeatureProperty.getKey(), featureProperty.getKey());
    assertEquals(expectedFeatureProperty.getValue(), featureProperty.getValue());
    assertEquals(expectedFeatureProperty.getFeatureType(), featureProperty.getFeatureType());
    assertEquals(expectedFeatureProperty.getGeometry(), featureProperty.getGeometry());
    assertEquals(expectedFeatureProperty.getVersion(), featureProperty.getVersion());
    assertEquals(expectedFeatureProperty.getFeatureDocId(), featureProperty.getFeatureDocId());
    assertNotNull(featureProperty.getProperties());
    assertEquals(
        expectedFeatureProperty.getKey(), featureProperty.getProperties().getKey());
    assertEquals(
        expectedFeatureProperty.getValue(), featureProperty.getProperties().getValue());
    assertEquals(
        expectedFeatureProperty.getVersion(), featureProperty.getProperties().getVersion());
    assertEquals(
        expectedFeatureProperty.getFeatureType(),
        featureProperty.getProperties().getFeatureType());
  }

  @DisplayName("Should store MultiPoint Feature correctly")
  @SneakyThrows
  @Test
  void saveMultiPointTest() {
    var points = List.of(
        new PointGeometry(new LonLat(6.131935, 49.611673)),
        new PointGeometry(new LonLat(6.133527, 49.613243)));

    MultiPointFeature feature = new MultiPointFeature(
        "id2",
        new MultiPointGeometry(points),
        new PointFeatureProperties(
            "v1", "MultiPoint", new Date(), List.of(new FeatureProperty("key", List.of("value")))));

    storageService.save(feature, "multi-point");
    refreshIndex("statistic-multipoint-multi-point", elasticsearchOperations);

    SearchHits<MultiPointFeature> search = elasticsearchOperations.search(
        Query.findAll(),
        MultiPointFeature.class,
        IndexCoordinates.of("statistic-multipoint-multi-point"));

    assertEquals(1, search.getTotalHits());
    assertEquals(feature, search.getSearchHit(0).getContent());
  }

  @DisplayName("Should store LineString Feature correctly")
  @SneakyThrows
  @Test
  void saveLineStringTest() {
    var lineString = new LineGeometry(List.of(
        new PointGeometry(new LonLat(6.129673, 49.611004)),
        new PointGeometry(new LonLat(6.133527, 49.613243))));

    LineFeature feature = new LineFeature(
        "id3",
        lineString,
        new LineFeatureProperties(
            "v1", "LineString", new Date(), List.of(new FeatureProperty("key", List.of("value")))));

    storageService.save(feature, "line-string");

    refreshIndex("statistic-linestring-line-string", elasticsearchOperations);

    SearchHits<LineFeature> search = elasticsearchOperations.search(
        Query.findAll(),
        LineFeature.class,
        IndexCoordinates.of("statistic-linestring-line-string"));

    assertEquals(1, search.getTotalHits());
    assertEquals(feature, search.getSearchHit(0).getContent());
  }

  @DisplayName("Should store MultiLineString Feature correctly")
  @SneakyThrows
  @Test
  void saveMultiLineStringTest() {
    var lines = List.of(
        new LineGeometry(List.of(
            new PointGeometry(new LonLat(6.129673, 49.611004)),
            new PointGeometry(new LonLat(6.133527, 49.613243)))),
        new LineGeometry(List.of(
            new PointGeometry(new LonLat(6.134128, 49.593476)),
            new PointGeometry(new LonLat(6.134940, 49.593091)))));

    MultiLineFeature feature = new MultiLineFeature(
        "id4",
        new MultiLineGeometry(lines),
        new LineFeatureProperties(
            "v1",
            "MultiLineString",
            new Date(),
            List.of(new FeatureProperty("key", List.of("value")))));

    storageService.save(feature, "multi-line-string");

    refreshIndex("statistic-multilinestring-multi-line-string", elasticsearchOperations);

    SearchHits<MultiLineFeature> search = elasticsearchOperations.search(
        Query.findAll(),
        MultiLineFeature.class,
        IndexCoordinates.of("statistic-multilinestring-multi-line-string"));

    assertEquals(1, search.getTotalHits());
    assertEquals(feature, search.getSearchHit(0).getContent());
  }

  @DisplayName("Should store Polygon Feature correctly")
  @SneakyThrows
  @Test
  void savePolygonTest() {
    var polygon = new PolygonGeometry(
        new LineGeometry(List.of(
            new PointGeometry(new LonLat(6.1305, 49.6100)),
            new PointGeometry(new LonLat(6.1320, 49.6105)),
            new PointGeometry(new LonLat(6.1310, 49.6115)),
            new PointGeometry(new LonLat(6.1305, 49.6100)))),
        List.of());

    PolygonFeature feature = new PolygonFeature(
        "id5",
        polygon,
        new PolygonFeatureProperties(
            "v1", "Polygon", new Date(), List.of(new FeatureProperty("key", List.of("value")))));

    storageService.save(feature, "polygon");

    refreshIndex("statistic-polygon-polygon", elasticsearchOperations);

    SearchHits<PolygonFeature> search = elasticsearchOperations.search(
        Query.findAll(), PolygonFeature.class, IndexCoordinates.of("statistic-polygon-polygon"));

    assertEquals(1, search.getTotalHits());
    assertEquals(feature, search.getSearchHit(0).getContent());
  }

  @DisplayName("Should store MultiPolygon Feature correctly")
  @SneakyThrows
  @Test
  void saveMultiPolygonTest() {
    var polygons = List.of(new PolygonGeometry(
        new LineGeometry(List.of(
            new PointGeometry(new LonLat(6.1305, 49.6100)),
            new PointGeometry(new LonLat(6.1320, 49.6105)),
            new PointGeometry(new LonLat(6.1310, 49.6115)),
            new PointGeometry(new LonLat(6.1305, 49.6100)))),
        List.of()));

    MultiPolygonFeature feature = new MultiPolygonFeature(
        "id6",
        new MultiPolygonGeometry(polygons),
        new PolygonFeatureProperties(
            "v1",
            "MultiPolygon",
            new Date(),
            List.of(new FeatureProperty("key", List.of("value")))));

    storageService.save(feature, "multi-polygon");

    refreshIndex("statistic-multipolygon-multi-polygon", elasticsearchOperations);

    SearchHits<MultiPolygonFeature> search = elasticsearchOperations.search(
        Query.findAll(),
        MultiPolygonFeature.class,
        IndexCoordinates.of("statistic-multipolygon-multi-polygon"));

    assertEquals(1, search.getTotalHits());
    assertEquals(feature, search.getSearchHit(0).getContent());
  }

  @DisplayName("Should store GeometryCollection Feature correctly")
  @SneakyThrows
  @Test
  void saveGeometryCollectionTest() {
    var point = new PointGeometry(new LonLat(6.131935, 49.611673));
    var lineString = new LineGeometry(List.of(
        new PointGeometry(new LonLat(6.129673, 49.611004)),
        new PointGeometry(new LonLat(6.133527, 49.613243))));
    var polygon = new PolygonGeometry(
        new LineGeometry(List.of(
            new PointGeometry(new LonLat(6.1305, 49.6100)),
            new PointGeometry(new LonLat(6.1320, 49.6105)),
            new PointGeometry(new LonLat(6.1310, 49.6115)),
            new PointGeometry(new LonLat(6.1305, 49.6100)))),
        List.of());

    var geometry = new StatisticGeometryCollection(List.of(point, lineString, polygon));
    var properties = new GeometryCollectionFeatureProperties(
        "v1",
        "GeometryCollection",
        new Date(),
        List.of(new FeatureProperty("key", List.of("value"))));

    GeometryCollectionFeature feature = new GeometryCollectionFeature("id7", geometry, properties);

    storageService.save(feature, "geometry-collection");

    refreshIndex("statistic-geometrycollection-geometry-collection", elasticsearchOperations);

    SearchHits<GeometryCollectionFeature> search = elasticsearchOperations.search(
        Query.findAll(),
        GeometryCollectionFeature.class,
        IndexCoordinates.of("statistic-geometrycollection-geometry-collection"));

    assertEquals(1, search.getTotalHits());
    assertEquals(feature, search.getSearchHit(0).getContent());
  }

  @DisplayName("Should store auxiliary documents alongside a feature")
  @SneakyThrows
  @Test
  void saveWithAuxiliaryDocumentsTest() {
    var coordinates = new LonLat(6.134128158472095, 49.593476516900296);

    PointFeature feature = new PointFeature(
        "id8",
        new PointGeometry(coordinates),
        new PointFeatureProperties("v1", "Point", new Date()));

    var rangeDocument = new RangeDocument(
        "speed",
        "50",
        "v1",
        "2025-01-01T00:00:00Z",
        10.0,
        "id8",
        "Point",
        new MultiLineGeometry(List.of(new LineGeometry(List.of(
            new PointGeometry(new LonLat(6.134128158472095, 49.593476516900296)),
            new PointGeometry(new LonLat(6.134940, 49.593091)))))));

    storageService.save(
        feature,
        "point-aux",
        List.of(
            new AuxiliaryDocumentWrite("statistic-rangeattribute-point-point-aux", rangeDocument)));

    refreshIndex("statistic-rangeattribute-point-point-aux", elasticsearchOperations);

    SearchHits<RangeDocument> search = elasticsearchOperations.search(
        Query.findAll(),
        RangeDocument.class,
        IndexCoordinates.of("statistic-rangeattribute-point-point-aux"));

    assertEquals(1, search.getTotalHits());
    var actualRangeDocument = search.getSearchHit(0).getContent();
    assertEquals(rangeDocument, actualRangeDocument);
    assertNotNull(actualRangeDocument.getProperties());
    assertEquals(rangeDocument.getKey(), actualRangeDocument.getProperties().getKey());
    assertEquals(rangeDocument.getValue(), actualRangeDocument.getProperties().getValue());
    assertEquals(rangeDocument.getVersion(), actualRangeDocument.getProperties().getVersion());
    assertEquals(
        rangeDocument.getTimestamp(), actualRangeDocument.getProperties().getTimestamp());
    assertEquals(
        rangeDocument.getFeatureType(), actualRangeDocument.getProperties().getFeatureType());
  }
}
