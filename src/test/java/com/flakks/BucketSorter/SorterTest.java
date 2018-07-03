package com.flakks.BucketSorter;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

public class SorterTest extends TestCase {
  private static ObjectMapper mapper = new ObjectMapper();

  private ObjectNode createHit(long id, long bucket, long slot, double bucketScore) {
    ObjectNode fields = mapper.createObjectNode();
    fields.put("id", mapper.createArrayNode().add(id));
    fields.put("bucket", mapper.createArrayNode().add(bucket));
    fields.put("slot", mapper.createArrayNode().add(slot));
    fields.put("bucket_score", mapper.createArrayNode().add(bucketScore));

    ObjectNode hit = mapper.createObjectNode();
    hit.put("fields", fields);

    return hit;
  }

  public void testSort() {
    ArrayNode hits = mapper.createArrayNode();
    hits.add(createHit(1, 1, 1, 0.5));
    hits.add(createHit(2, 1, 1, 0.6));
    hits.add(createHit(3, 2, 1, 0.8));
    hits.add(createHit(4, 2, 2, 0.6));
    hits.add(createHit(5, 2, 1, 0.5));
    hits.add(createHit(6, 3, 1, 0.2));

    ObjectNode outerHits = mapper.createObjectNode();
    outerHits.put("hits", hits);

    ObjectNode root = mapper.createObjectNode();
    root.put("hits", outerHits);

    Sorter sorter = new Sorter();
    sorter.sort(mapper, root, 0, 0, 5, 5, true);

    List actual = new ArrayList();

    for(JsonNode hit : hits)
      actual.add(hit.get("fields").get("id").get(0).asInt());

    assertEquals(Arrays.asList(3, 1, 4, 2, 5), actual);
  }
}
