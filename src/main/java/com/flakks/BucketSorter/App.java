
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
import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import java.util.stream.Collectors;

public class App extends AbstractHandler {
  public static void main(String[] args) throws Exception {
    Server server = new Server(19400);
    server.setHandler(new App());
    server.start();
    server.join();
  }
  
  @Override
  public void handle(String target, Request request, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws IOException, ServletException {
    try {
      long t1, t2;

      JSONParser jsonParser = new JSONParser();
      JSONObject jsonRequest = (JSONObject)jsonParser.parse(request.getReader());
      
      long from = (Long)jsonRequest.get("from");
      long size = (Long)jsonRequest.get("size");
      
      JSONObject bucketSort = (JSONObject)jsonRequest.get("bucket_sort");

      long window = (Long)bucketSort.get("window");
      
      long newFrom = (from / window) * window;

      JSONArray docvalueFields = new JSONArray();
      docvalueFields.add("id");
      //docvalueFields.add("license");
      docvalueFields.add("collection_id");
      docvalueFields.add("supplier_id");
      docvalueFields.add("collection_score");

      jsonRequest.put("from", newFrom);
      jsonRequest.put("size", window);
      jsonRequest.put("stored_fields", "_none_");
      jsonRequest.put("docvalue_fields", docvalueFields);

      jsonRequest.remove("_source");
      jsonRequest.remove("bucket_sort");

      HttpResponse<String> unirestResponse = Unirest.post(((String)bucketSort.get("base_url")) + request.getRequestURI())
        .header("accept", "application/json")
        .header("content-type", "application/json")
        .body(jsonRequest.toString())
        .asString();
      
      httpServletResponse.setStatus(unirestResponse.getStatus());
      httpServletResponse.setCharacterEncoding("utf-8");
      
      if(unirestResponse.getStatus() < 200 || unirestResponse.getStatus() > 299) {
        httpServletResponse.getWriter().print(unirestResponse.getBody());
        request.setHandled(true);
        
        return;
      }

      JSONObject jsonResponse = (JSONObject)jsonParser.parse(unirestResponse.getBody());

      t1 = new Date().getTime();
      
      new Sorter().sort(jsonResponse, from, newFrom, size, (boolean)bucketSort.get("aggregations"));

      t2 = new Date().getTime();

      jsonResponse.put("took", ((Long)jsonResponse.get("took")) + (t2 - t1));

      System.out.println(t2 - t1);

      httpServletResponse.setContentType("application/json");
      httpServletResponse.getWriter().print(jsonResponse.toString());

      request.setHandled(true);
    } catch(ParseException | UnirestException e) {
      e.printStackTrace();
    }        
  }
}
