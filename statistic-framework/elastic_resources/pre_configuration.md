
## Index templates

### Polygon/LineString features index template
```shell
PUT /_index_template/feature_line_template
{
  "index_patterns": [
    "feature_line_*"
  ],
  "template": {
    "mappings": {
      "properties": {
        "geometry": {
          "type": "geo_shape"
        }
      }
    }
  }
}
```

### Point features index template
```shell
PUT /_index_template/feature_point_template
{
  "index_patterns": [
    "feature_point_*"
  ],
  "template": {
    "mappings": {
      "properties": {
        "geometry": {
          "type": "geo_point"
        }
      }
    }
  }
}
```

