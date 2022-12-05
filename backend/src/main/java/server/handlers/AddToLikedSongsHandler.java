package server.handlers;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import java.util.HashMap;
import java.util.Map;
import server.APIUtility;
import server.ServerResponse;
import server.deserializationObjects.ErrorObj;
import spark.QueryParamsMap;
import spark.Request;
import spark.Response;
import spark.Route;

public class AddToLikedSongsHandler implements Route {

  @Override
  public Object handle(Request request, Response response) throws Exception {
    Map<String, Object> resp = new HashMap<>();
    try {
      QueryParamsMap params = request.queryMap();
      if (!params.hasKey("token") || !params.hasKey("id")) {
        resp.put("result", "error_bad_request");
        return new ServerResponse().serialize(resp);
      } else if (params.get("id").value().equals("") || params.get("token").value().equals("")) {
        resp.put("result", "error_no_token");
        return new ServerResponse().serialize(resp);
      }
      String token = params.get("token").value();
      String id = params.get("id").value();
      String url = "https://api.spotify.com/v1/me/tracks?ids=" + id;

      APIUtility idURL = new APIUtility(url);
      String JSONBody = idURL.putAPIRequest(token);

      Moshi moshi = new Moshi.Builder().build();
      JsonAdapter<ErrorObj> errorAdapter = moshi.adapter(ErrorObj.class);
      ErrorObj errObj = errorAdapter.fromJson(JSONBody);

      if (errObj.error == null) {
        resp.put("result", "success");
      } else {
        resp.put("result", "error_bad_token");
      }
      return new ServerResponse().serialize(resp);
    } catch (Exception e) {
      System.out.println(e.getMessage());
      e.printStackTrace();
      resp.put("result", "error_bad_token");
      return new ServerResponse().serialize(resp);
    }
  }
}
