package edu.mayo.kmdp.health;

import edu.mayo.kmdp.util.ws.ResponseHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.info.BuildProperties;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class VersionEndPoint implements VersionApiDelegate {

  @Autowired
  BuildProperties buildProperties;

  @Override
  public ResponseEntity<String> getVersionData() {
    try {
      return ResponseHelper.succeed(buildProperties.getVersion());
    } catch (Exception e) {
      return ResponseHelper.fail();
    }
  }
}
