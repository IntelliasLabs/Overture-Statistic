# Statistic framework

This module is an aggregator for all Statistic Framework components. 
It does not contain standalone logic but collects the framework modules so they can be used as a single dependency. 
Include it in your project if you want to extend or build upon the Statistic Framework.

With the full aggregator on the classpath, ingress, storage, and analytics share the same range
attribute contract:

- ingress preprocesses and materializes range documents for line features
- storage persists both source features and derivative documents
- analytics consumes the existing `rangeattribute-*` indices for dashboards and index discovery

## how to run

Just start spring boot application class [StatisticApplication.java](src/main/java/com/intellias/mobility/statistic/StatisticApplication.java)
and it will set-up elk stack automatically

Then open http://localhost:5601/ username: elastic password: elastic
then create api key http://localhost:5601/app/management/security/api_keys/
and paste it in [application.properties](src/main/resources/deprecated/application.properties) statistic-app.elastic.token=

## check elk
Test data
```shell
export API_KEY=
curl -X POST "http://localhost:9201/_bulk?pretty&pipeline=ent-search-generic-ingestion" \
  -H "Authorization: ApiKey "${API_KEY}"" \
  -H "Content-Type: application/json" \
  -d'
{ "index" : { "_index" : "test-index" } }
{"name": "Snow Crash", "author": "Neal Stephenson", "release_date": "1992-06-01", "page_count": 470, "_extract_binary_content": true, "_reduce_whitespace": true, "_run_ml_inference": true}
{ "index" : { "_index" : "test-index" } }
{"name": "Revelation Space", "author": "Alastair Reynolds", "release_date": "2000-03-15", "page_count": 585, "_extract_binary_content": true, "_reduce_whitespace": true, "_run_ml_inference": true}
{ "index" : { "_index" : "test-index" } }
{"name": "1984", "author": "George Orwell", "release_date": "1985-06-01", "page_count": 328, "_extract_binary_content": true, "_reduce_whitespace": true, "_run_ml_inference": true}
{ "index" : { "_index" : "test-index" } }
'

```

```shell
export API_KEY=
curl -X POST "http://localhost:9201/test-index/_search?pretty" \
  -H "Authorization: ApiKey "${API_KEY}"" \
  -H "Content-Type: application/json" \
  -d'
{
  "query": {
    "query_string": {
      "query": "snow"
    }
  }
}'
```
