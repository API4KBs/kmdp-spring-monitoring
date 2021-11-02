package edu.mayo.kmdp.health;

import static edu.mayo.kmdp.health.utils.MonitorUtil.getBuildProps;
import static edu.mayo.kmdp.health.utils.MonitorUtil.getServiceNowInfo;

import edu.mayo.kmdp.health.datatype.Flags;
import edu.mayo.kmdp.health.datatype.MiscProperties;
import edu.mayo.kmdp.health.datatype.SchemaMetaInfo;
import edu.mayo.kmdp.health.datatype.StateData;
import edu.mayo.kmdp.health.utils.MonitorUtil;
import edu.mayo.kmdp.health.utils.PropKey;
import edu.mayo.kmdp.util.ws.ResponseHelper;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Predicate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.info.BuildProperties;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class StateEndPoint implements StateApiDelegate {

  public static final String FLAG_PREFIX = "edu.mayo.kmdp.application.flag";

  @Autowired
  private ConfigurableEnvironment environment;

  @Autowired
  BuildProperties build;

  @Autowired(required = false)
  Predicate<String> flagTester;

  static SchemaMetaInfo schemaMetaInfo() {
    var info = new SchemaMetaInfo();
    info.setUrl("https://schemas.kmd.mayo.edu/state-endpoint.json");
    info.setVersion("0.0.1");
    return info;
  }

  @Override
  public ResponseEntity<StateData> getStateData() {
    var state = new StateData();
    var props = MonitorUtil.streamConvert(
        MonitorUtil.getEnvironmentProperties(environment),
        LinkedHashMap::new);

    state.setServiceNowReference(getServiceNowInfo(environment));
    state.setBuildConfiguration(getBuildProps(build));
    state.setDeploymentEnvironmentConfiguration(getDeploymentProperties(props));
    state.setFeatures(getFeatureFlags(props));

    state.setSchemaInfo(schemaMetaInfo());

    return ResponseHelper.succeed(state);
  }

  protected Flags getFeatureFlags(Map<String, String> envProperties) {
    var flags = new Flags();
    envProperties.entrySet().stream()
        .filter(e -> isFeatureFlag(e.getKey()))
        .forEach(e -> flags.put(e.getKey(), Boolean.valueOf(e.getValue())));
    return flags;
  }

  private boolean isFeatureFlag(String key) {
    return flagTester != null
        ? flagTester.test(key)
        : key.toLowerCase().startsWith(FLAG_PREFIX);
  }

  protected MiscProperties getDeploymentProperties(Map<String, String> envProperties) {
    var deployProperties = new MiscProperties();
    envProperties.entrySet().stream()
        .filter(e -> !PropKey.isKnownProperty(e.getKey()))
        .filter(e -> !isFeatureFlag(e.getKey()))
        .forEach(e -> deployProperties.put(e.getKey(), e.getValue()));
    return deployProperties;
  }


}
