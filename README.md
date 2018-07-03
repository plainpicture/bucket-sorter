
# BucketSorter

BucketSorter is a Proxy for ElasticSearch written in Java to support
window-based search result diversification. I.e. BucketSorter intercepts
the requests for ElasticSearch, parses it, enlarges the `from` and `size`
parameters to match the desired window size, fetches the results from
ElasticSearch and resorts the documents to maximize the distance between the
documents within the result window. The distance is calculated via 3 fields
extracted from the docvalue fields `bucket`, `slot` and `bucket_score` of the
documents.

