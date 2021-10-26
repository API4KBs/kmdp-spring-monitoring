package edu.mayo.kmdp.health;

import edu.mayo.kmdp.health.datatype.BuildProps;
import edu.mayo.kmdp.health.datatype.ImplementationStrategy;
import edu.mayo.kmdp.health.datatype.ServiceNow;
import edu.mayo.kmdp.health.datatype.StateData;
import edu.mayo.kmdp.health.datatype.SystemInfo;
import edu.mayo.kmdp.health.utils.PropKey;
import edu.mayo.kmdp.util.ws.ResponseHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.info.BuildProperties;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class StateEndPoint implements StateApiDelegate {

  @Autowired
  private Environment environment;

  @Autowired
  BuildProperties buildProperties;

  @Override
  public ResponseEntity<StateData> getStateData() {
    var stateData = new StateData();

    stateData.setBuildProps(getBuildProps());
    stateData.setSystemInfo(getSystemInfo());
    stateData.setImplementationStrategy(getImplementationStrategy());
    stateData.setServiceNow(getServiceNow());

    return ResponseHelper.succeed(stateData);
  }

  private BuildProps getBuildProps() {
    var buildProps = new BuildProps();
    buildProps.put("name", buildProperties.getName());
    buildProps.put("version", buildProperties.getVersion());
    buildProps.put("group", buildProperties.getGroup());
    buildProps.put("artifact", buildProperties.getArtifact());
    buildProps.put("time", buildProperties.getTime().toString());
    return buildProps;
  }

  private SystemInfo getSystemInfo() {
    var systemInfo = new SystemInfo();
    systemInfo.put("scanPackages", environment.getProperty(PropKey.SCAN));
    systemInfo.put("repositoryName", environment.getProperty(PropKey.REPO_NAME));
    systemInfo.put("repositoryId", environment.getProperty(PropKey.REPO_ID));
    systemInfo.put("repositoryExpire", environment.getProperty(PropKey.REPO_EXPIRE));
    systemInfo.put("repositoryPath", environment.getProperty(PropKey.REPO_PATH));
    systemInfo.put("splunkUrl", environment.getProperty(PropKey.SPLUNK_URL));
    systemInfo.put("splunkIndexName", environment.getProperty(PropKey.SPLUNK_INDEX));
    systemInfo.put("splunkSourceType", environment.getProperty(PropKey.SPLUNK_SOURCE));
    systemInfo.put("useSeparateFhirValueSetServer", environment.getProperty(PropKey.FHIR_USE_SEP));
    return systemInfo;
  }

  private ImplementationStrategy getImplementationStrategy() {
    var impStrat = new ImplementationStrategy();
    impStrat.put("useSpringCache", this.environment.getProperty(PropKey.SPRING_CACHE));
    return impStrat;
  }

  private ServiceNow getServiceNow() {
    var serviceNow = new ServiceNow();
    serviceNow.setId(environment.getProperty(PropKey.SN_ID));
    serviceNow.setUrl(environment.getProperty(PropKey.SN_URL));
    serviceNow.setDisplay(environment.getProperty(PropKey.SN_DISPLAY));
    return serviceNow;
  }

}
