
package com.flakks.BucketSorter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class Hit {
  private JsonNode hit;
  private Long id;
  private int bucket, slot;
  private long bucketSlot;
  private double bucketScore, sortScore;
  private int originalIndex;

  public Hit(JsonNode hit, int originalIndex) {
    JsonNode fields = hit.get("fields");

    this.hit = hit;
    this.originalIndex = originalIndex;

    this.id = fields.get("id").get(0).asLong();
    this.bucket = fields.get("bucket").get(0).asInt();
    this.slot = fields.get("slot").get(0).asInt();
    this.bucketScore = fields.get("bucket_score").get(0).asDouble();
    this.bucketSlot = ((long)bucket << 32) | (long)slot;

    ((ObjectNode)hit).put("_id", id.toString());
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

  public JsonNode getHit() {
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
