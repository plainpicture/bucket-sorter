
package com.flakks.BucketSorter;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import java.io.IOException;
import java.util.Date;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.stream.Collectors;

public class App extends AbstractHandler {
  public static void main(String[] args) throws Exception {
    Server server = new Server(19401);
    server.setHandler(new App());
    server.start();
    server.join();
  }

  public void handle(String target, Request request, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws IOException, ServletException {
    try {
      long t1, t2;

      t1 = new Date().getTime();

      ObjectMapper objectMapper = new ObjectMapper();
      ObjectNode jsonRequest = (ObjectNode)objectMapper.readTree(request.getReader());

      long from = jsonRequest.get("from").asLong();
      long size = jsonRequest.get("size").asLong();

      JsonNode bucketSort = jsonRequest.get("bucket_sort");

      long window = bucketSort.get("window").asLong();

      long newFrom = (from / window) * window;

      ArrayNode docvalueFields = objectMapper.createArrayNode();
      docvalueFields.add("id");
      docvalueFields.add("bucket");
      docvalueFields.add("slot");
      docvalueFields.add("bucket_score");

      jsonRequest.put("from", newFrom);
      jsonRequest.put("size", window);
      jsonRequest.put("stored_fields", "_none_");
      jsonRequest.set("docvalue_fields", docvalueFields);

      jsonRequest.remove("_source");
      jsonRequest.remove("bucket_sort");

    

      HttpResponse<String> unirestResponse = Unirest.post(bucketSort.get("base_url").asText() + request.getRequestURI())
        .header("accept", "application/json")
        .header("content-type", "application/json")
        .body(jsonRequest.toString())
        .asString();


      httpServletResponse.setStatus(unirestResponse.getStatus());
      httpServletResponse.setCharacterEncoding("utf-8");

      if(unirestResponse.getStatus() < 200 || unirestResponse.getStatus() > 299) {
        httpServletResponse.getWriter().print(unirestResponse.getBody());
        httpServletResponse.setStatus(unirestResponse.getStatus());
        request.setHandled(true);

        return;
      }

      ObjectNode jsonResponse = (ObjectNode)objectMapper.readTree(unirestResponse.getBody());

      new Sorter().sort(objectMapper, jsonResponse, from, newFrom, size, bucketSort.get("slot_shift").asLong(), bucketSort.get("aggregations").asBoolean());

      t2 = new Date().getTime();

      jsonResponse.set("original_took", jsonResponse.get("took"));
      jsonResponse.put("took", t2 - t1);

      httpServletResponse.setContentType("application/json");
      httpServletResponse.getWriter().print(jsonResponse.toString());

      request.setHandled(true);
    } catch(UnirestException e) {
      e.printStackTrace();
    }
  }
}
