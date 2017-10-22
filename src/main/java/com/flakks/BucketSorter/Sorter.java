package com.flakks.BucketSorter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Comparator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class Sorter {
  private Map<Integer, Counter> bucketFrequencies;

  private List<Hit> sort(List<Hit> hits, long slotShift) {
    bucketFrequencies = new HashMap<Integer, Counter>();

    Map<Integer, Counter> distinctBucketSlotFrequencies = new HashMap<Integer, Counter>();
    Map<Long, Counter> bucketSlotFrequencies = new HashMap<Long, Counter>();
    Map<Integer, List<Hit>> buckets = new HashMap<Integer, List<Hit>>();

    for(Hit hit : hits) {
      Counter bucketCounter = bucketFrequencies.get(hit.getBucket());
      Counter bucketSlotCounter = bucketSlotFrequencies.get(hit.getBucketSlot());
      Counter distinctBucketSlotCounter = distinctBucketSlotFrequencies.get(hit.getBucket());

      if(bucketCounter == null) {
        bucketCounter = new Counter(1);

        bucketFrequencies.put(hit.getBucket(), bucketCounter);
      } else {
        bucketCounter.increment();
      }

      if(bucketSlotCounter == null) {
        bucketSlotCounter = new Counter(1);

        bucketSlotFrequencies.put(hit.getBucketSlot(), bucketSlotCounter);
      } else {
        bucketSlotCounter.increment();
      }

      if(distinctBucketSlotCounter == null) {
        distinctBucketSlotCounter = new Counter(0);

        distinctBucketSlotFrequencies.put(hit.getBucket(), distinctBucketSlotCounter);
      }

      if(bucketSlotCounter.getValue() == 1)
        distinctBucketSlotCounter.increment();

      List<Hit> bucket = buckets.get(hit.getBucket());

      if(bucket == null) {
        bucket = new ArrayList<Hit>();

        buckets.put(hit.getBucket(), bucket);
      }

      hit.setSortScore(distinctBucketSlotCounter.getValue() + slotShift * (bucketSlotCounter.getValue() - 1));

      bucket.add(hit);
    }

    List<Hit> res = new ArrayList<Hit>(hits.size());

    for(List<Hit> bucket : buckets.values()) {
      int index = 0;

      bucket.sort(new Comparator<Hit>() {
        public int compare(Hit o1, Hit o2) {
          return ((Double)o1.getSortScore()).compareTo(o2.getSortScore());
        }
      });

      for(Hit hit : bucket) {
        hit.setSortScore((index + 1) * (1.0 - hit.getBucketScore()) * (hits.size() / (double)bucketFrequencies.get(hit.getBucket()).getValue()) + (hit.getOriginalIndex() / (double)hits.size()));

        res.add(hit);

        index++;
      }
    }

    res.sort(new Comparator<Hit>() {
      public int compare(Hit o1, Hit o2) {
        return ((Double)o1.getSortScore()).compareTo(o2.getSortScore());
      }
    });

    return res;
  }

  public void sort(ObjectMapper objectMapper, JsonNode jsonResponse, long from, long newFrom, long size, long slotShift, boolean includeAggregations) {
    ArrayNode hits = (ArrayNode)jsonResponse.get("hits").get("hits");

    int total = hits.size();
    List<Hit> sortHits = new ArrayList<Hit>(total);
    int i;

    for(i = 0; i < hits.size(); i++)
      sortHits.add(new Hit(hits.get(i), i));

    List<Hit> sortedHits = sort(sortHits, slotShift);

    hits.removeAll();

    int start = (int)(from - newFrom);
    int stop = Math.min(sortedHits.size(), start + (int)size);

    for(i = start; i < stop; i++) {
      Hit hit = sortedHits.get(i);

      ((ObjectNode)hit.getHit()).put("bucket_sort", hit.getSortScore());

      hits.add(hit.getHit());
    }

    if(includeAggregations) {
      ObjectNode aggregation = objectMapper.createObjectNode();

      for(Map.Entry<Integer, Counter> entry : bucketFrequencies.entrySet())
        aggregation.put(entry.getKey().toString(), entry.getValue().getValue());

      ObjectNode aggregations = objectMapper.createObjectNode();

      aggregations.put("buckets", aggregation);

      ((ObjectNode)jsonResponse).put("aggregations", aggregations);
    }
  }
}
