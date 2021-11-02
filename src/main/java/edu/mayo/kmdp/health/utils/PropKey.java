package edu.mayo.kmdp.health.utils;

import java.util.Arrays;

public enum PropKey {

  ENV("env"),

  SN_ID("edu.mayo.kmdp.application.serviceNow.id"),
  SN_URL("edu.mayo.kmdp.application.serviceNow.url"),
  SN_DISPLAY("edu.mayo.kmdp.application.serviceNow.display");

  String key;

  PropKey(String key) {
    this.key = key;
  }

  public String getKey() {
    return key;
  }

  public static boolean isKnownProperty(String k) {
    return Arrays.stream(PropKey.values())
        .anyMatch(p -> p.key.equalsIgnoreCase(k));
  }

}
