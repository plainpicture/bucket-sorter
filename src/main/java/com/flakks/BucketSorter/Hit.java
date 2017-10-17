
package com.flakks.BucketSorter;

import org.json.simple.JSONObject;
import org.json.simple.JSONArray;

public class Hit {
  private JSONObject hit;
  private String id;
  private int bucket, slot;
  private long bucketSlot;
  private double bucketScore, sortScore;
  private int originalIndex;
  
  public Hit(JSONObject hit, int originalIndex) {
    JSONObject fields = (JSONObject)hit.get("fields");

    this.hit = hit;
    this.originalIndex = originalIndex;

    this.id = (String)((JSONArray)fields.get("id")).get(0);
    this.bucket = (int)(long)((JSONArray)fields.get("bucket")).get(0);
    this.slot = (int)(long)((JSONArray)fields.get("slot")).get(0);

    this.bucketSlot = ((long)bucket << 32) | slot;

    this.bucketScore = (double)((JSONArray)fields.get("bucket_score")).get(0);

    hit.put("_id", id);
  }

  public String getId() {
    return id;
  }

  public void setSortScore(double sortScore) {
    this.sortScore = sortScore;
  }

  public double getSortScore() {
    return sortScore;
  }

  public int getOriginalIndex() {
    return originalIndex;
  }

  public JSONObject getHit() {
    return hit;
  }

  public int getBucket() {
    return bucket;
  }

  public int getSlot() {
    return slot;
  }

  public double getBucketScore() {
    return bucketScore;
  }

  public long getBucketSlot() {
    return bucketSlot;
  }
}
