
# BucketSorter

BucketSorter is a Proxy for ElasticSearch written in Java to support
window-based search result diversification. I.e. BucketSorter intercepts
the requests for ElasticSearch, parses them, adapts the `from` and `size`
parameters to match the desired window size, fetches the results from
ElasticSearch and resorts the hits to maximize the distance between the them
within the result window. The distance is calculated via 3 fields extracted
from the docvalue fields `bucket`, `slot` and `bucket_score`.
