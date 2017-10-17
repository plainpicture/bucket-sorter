package com.flakks.BucketSorter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Comparator;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

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
        double score = index * (1.0 - hit.getBucketScore()) * (hits.size() / (double)bucketFrequencies.get(hit.getBucket()).getValue()) + (hit.getOriginalIndex() / (double)hits.size());

        hit.setSortScore(score);

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

  public void sort(JSONObject jsonResponse, long from, long newFrom, long size, long slotShift, boolean includeAggregations) {
    JSONArray hits = (JSONArray)((JSONObject)jsonResponse.get("hits")).get("hits");

    int total = hits.size();
    List<Hit> sortHits = new ArrayList<Hit>(total);
    int i;

    for(i = 0; i < hits.size(); i++)
      sortHits.add(new Hit((JSONObject)hits.get(i), i));

    List<Hit> sortedHits = sort(sortHits, slotShift);

    hits.clear();

    int start = (int)(from - newFrom);
    int stop = Math.min(sortedHits.size(), start + (int)size);

    for(i = start; i < stop; i++)
      hits.add(sortedHits.get(i).getHit());

    if(includeAggregations) {
      JSONObject aggregation = new JSONObject();

      for(Map.Entry<Integer, Counter> entry : bucketFrequencies.entrySet())
        aggregation.put(entry.getKey(), entry.getValue().getValue());

      JSONObject aggregations = new JSONObject();

      aggregations.put("buckets", aggregation);

      jsonResponse.put("aggregations", aggregations);
    }
  }
}
