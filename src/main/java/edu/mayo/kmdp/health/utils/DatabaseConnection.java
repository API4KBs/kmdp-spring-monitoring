package edu.mayo.kmdp.health.utils;

import edu.mayo.kmdp.health.datatype.Status;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

  public Status pingDatabase(String url, String user, String driver, String password) {
    Connection connection = null;
    try {
      Class.forName(driver);
      connection = DriverManager.getConnection(url, user, password);
      if (connection.isValid(5)) {
        return Status.UP;
      } else {
        return Status.DOWN;
      }
    } catch (ClassNotFoundException | SQLException e) {
      e.printStackTrace();
      return Status.IMPAIRED;
    } finally {
      if (connection != null) {
        try {
          connection.close();
        } catch (SQLException throwables) {
          throwables.printStackTrace();
        }
      }
    }
  }

}
