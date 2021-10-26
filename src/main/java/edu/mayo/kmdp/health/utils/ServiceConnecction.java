package edu.mayo.kmdp.health.utils;

import edu.mayo.kmdp.health.datatype.Status;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class ServiceConnecction {

  public Status pingService(String defaultRepo) {
    HttpURLConnection connection = null;
    try {
      if (defaultRepo.isEmpty()) {
        throw new IOException("Service was not found.");
      } else {
        var u = new URL(defaultRepo);

        connection = (HttpURLConnection) u.openConnection();
        connection.setRequestMethod("HEAD");
        int code = connection.getResponseCode();

        if (code == 200) {
          return Status.UP;
        } else {
          return Status.DOWN;
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
      return Status.IMPAIRED;
    } finally {
      if (connection != null) {
        connection.disconnect();
      }
    }
  }

}
