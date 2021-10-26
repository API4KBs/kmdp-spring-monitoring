package edu.mayo.kmdp.health;

import edu.mayo.kmdp.health.datatype.VersionData;
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
  public ResponseEntity<VersionData> getVersionData() {
    try {
      var versionData = new VersionData();
      versionData.setVersion(buildProperties.getVersion());
      return ResponseHelper.succeed(versionData);
    } catch (Exception e) {
      return ResponseHelper.fail();
    }
  }
}
