package com.flakks.BucketSorter;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

public class HitTest extends TestCase {
  private static ObjectMapper mapper = new ObjectMapper();

  private ObjectNode defaultHit() {
    ObjectNode fields = mapper.createObjectNode();
    fields.put("id", mapper.createArrayNode().add(1));
    fields.put("bucket", mapper.createArrayNode().add(1));
    fields.put("slot", mapper.createArrayNode().add(1));
    fields.put("bucket_score", mapper.createArrayNode().add(1.0));

    ObjectNode root = mapper.createObjectNode();
    root.put("fields", fields);

    return root;
  }

  public void testSortScore() {
    Hit hit = new Hit(defaultHit(), 1);

    hit.setSortScore(0.3);

    assertEquals(0.3, hit.getSortScore(), 0.0001);
  }

  public void testGetOriginalIndex() {
    Hit hit = new Hit(defaultHit(), 1);

    assertEquals(1, hit.getOriginalIndex());
  }

  public void testGetHit() {
    ObjectNode node =  defaultHit();

    Hit hit = new Hit(node, 1);

    assertEquals(node, hit.getHit());
  }

  public void testGetBucket() {
    ArrayNode bucket = mapper.createArrayNode();
    bucket.add(3);

    ObjectNode node = defaultHit();
    ((ObjectNode)node.get("fields")).put("bucket", bucket);

    Hit hit = new Hit(node, 1);

    assertEquals(3, hit.getBucket());
  }

  public void testGetSlot() {
    ObjectNode node = defaultHit();
    ((ObjectNode)node.get("fields")).put("slot", mapper.createArrayNode().add(3));

    Hit hit = new Hit(node, 1);

    assertEquals(3, hit.getSlot());
  }

  public void testGetBucketScore() {
    ObjectNode node = defaultHit();
    ((ObjectNode)node.get("fields")).put("bucket_score", mapper.createArrayNode().add(0.5));

    Hit hit = new Hit(node, 1);

    assertEquals(0.5, hit.getBucketScore(), 0.0001);
  }

  public void testGetBucketSlot() {
    ObjectNode node = defaultHit();
    ((ObjectNode)node.get("fields")).put("bucket", mapper.createArrayNode().add(1));
    ((ObjectNode)node.get("fields")).put("slot", mapper.createArrayNode().add(2));

    Hit hit = new Hit(node, 1);

    assertEquals(4294967298l, hit.getBucketSlot());
  }
}
