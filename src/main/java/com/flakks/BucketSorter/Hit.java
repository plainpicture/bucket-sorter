
package com.flakks.BucketSorter;

import org.json.simple.JSONObject;
import org.json.simple.JSONArray;

public class Hit {
  private JSONObject hit;
  private String id, bucket, slot, bucketSlot;
  private double bucketScore, sortScore;
  private int originalIndex;
  
  public Hit(JSONObject hit, int originalIndex) {
    JSONObject fields = (JSONObject)hit.get("fields");

    this.hit = hit;
    this.originalIndex = originalIndex;

    this.id = ((JSONArray)fields.get("id")).get(0).toString();
    //this.bucket = ((JSONArray)fields.get("license")).get(0).toString() + ":" + ((JSONArray)fields.get("collection_id")).get(0).toString();
    this.bucket = fields.get("collection_id") == null ? "_null_" : ((JSONArray)fields.get("collection_id")).get(0).toString();
    this.slot = ((JSONArray)fields.get("supplier_id")).get(0).toString();
    this.bucketSlot = bucket + ":" + slot;

    this.bucketScore = (double)((JSONArray)fields.get("collection_score")).get(0);

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

  public String getBucket() {
    return bucket;
  }

  public String getSlot() {
    return slot;
  }

  public String getBucketSlot() {
    return bucketSlot;
  }

  public double getBucketScore() {
    return bucketScore;
  }
}
