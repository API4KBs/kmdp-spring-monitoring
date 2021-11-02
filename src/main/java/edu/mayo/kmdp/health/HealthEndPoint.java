package edu.mayo.kmdp.health;

import static edu.mayo.kmdp.health.utils.MonitorUtil.getServiceNowInfo;

import edu.mayo.kmdp.health.datatype.ApplicationComponent;
import edu.mayo.kmdp.health.datatype.DeploymentEnvironment;
import edu.mayo.kmdp.health.datatype.HealthData;
import edu.mayo.kmdp.health.datatype.SchemaMetaInfo;
import edu.mayo.kmdp.health.datatype.Status;
import edu.mayo.kmdp.health.utils.MonitorUtil;
import edu.mayo.kmdp.health.utils.PropKey;
import edu.mayo.kmdp.util.ws.ResponseHelper;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.info.BuildProperties;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class HealthEndPoint implements HealthApiDelegate {

  @Autowired
  private Environment environment;
  @Autowired
  BuildProperties buildProperties;

  @Autowired(required = false)
  List<Supplier<ApplicationComponent>> appComponentSuppliers;

  @Autowired(required = false)
  Function<List<ApplicationComponent>, Status> statusMapper;


  static SchemaMetaInfo schemaMetaInfo() {
    var info = new SchemaMetaInfo();
    info.setUrl("https://schemas.kmd.mayo.edu/health-endpoint.json");
    info.setVersion("0.0.1");
    return info;
  }

  @Override
  public ResponseEntity<HealthData> getHealthData() {
    var health = new HealthData();

    List<ApplicationComponent> comps = appComponentSuppliers.stream()
        .map(Supplier::get)
        .collect(Collectors.toList());
    var snInfo = getServiceNowInfo(environment);

    health.setName(snInfo.getDisplay());
    health.setStatus(detectServerStatus(comps));
    health.setDeploymentEnvironment(getDeploymentEnvironment());
    health.setVersion(buildProperties.getVersion());
    health.setServiceNowReference(snInfo);
    health.setComponents(new ArrayList<>(comps));

    health.setAt(MonitorUtil.formatInstant(Instant.now()));
    health.setSchemaInfo(schemaMetaInfo());

    return ResponseHelper.succeed(health);
  }

  private Status detectServerStatus(List<ApplicationComponent> comps) {
    return statusMapper != null
        ? statusMapper.apply(comps)
        : MonitorUtil.defaultAggregateStatus(comps);
  }

  protected DeploymentEnvironment getDeploymentEnvironment() {
    return Stream.concat(
            Optional.ofNullable(environment.getProperty(PropKey.ENV.getKey())).stream(),
            Arrays.stream(environment.getActiveProfiles()))
        .flatMap(env -> Arrays.stream(DeploymentEnvironment.values())
            .filter(e -> e.name().equalsIgnoreCase(env))
            .findFirst().stream())
        .findFirst()
        .orElse(DeploymentEnvironment.UNKNOWN);
  }

}
