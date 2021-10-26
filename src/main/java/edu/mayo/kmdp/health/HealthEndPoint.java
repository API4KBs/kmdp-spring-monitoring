package edu.mayo.kmdp.health;

import edu.mayo.kmdp.health.datatype.Components;
import edu.mayo.kmdp.health.datatype.DeploymentEnvironment;
import edu.mayo.kmdp.health.datatype.Element;
import edu.mayo.kmdp.health.datatype.HealthData;
import edu.mayo.kmdp.health.datatype.ServiceUrl;
import edu.mayo.kmdp.health.utils.DatabaseConnection;
import edu.mayo.kmdp.health.utils.NlpConnection;
import edu.mayo.kmdp.health.utils.PropKey;
import edu.mayo.kmdp.health.utils.ServiceConnecction;
import edu.mayo.kmdp.util.ws.ResponseHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

  @Value("${udpPassword}")
  private String udpPassword;
  @Value("${clarityPassword}")
  private String clarityPassword;
  @Value("${mongoPassword}")
  private String mongoPassword;

  private static final String STATUS = "status";
  private static final String USER = "username";
  private static final String URL = "url";
  private static final String DRIVER = "driver";

  @Override
  public ResponseEntity<HealthData> getHealthData() {
    var healthData = new HealthData();

    healthData.setServiceUrl(getServiceUrl());
    healthData.setDeploymentEnvironment(getDeploymentEnvironment());
    healthData.setVersion(buildProperties.getVersion());
    healthData.setComponents(getComponents());

    return ResponseHelper.succeed(healthData);
  }

  private ServiceUrl getServiceUrl() {
    var server = new ServiceUrl();
    var repoUrl = this.environment.getProperty(PropKey.SERVICE_URL);
    if (repoUrl != null) {
      server.setUrl(repoUrl);
      var serviceConnection = new ServiceConnecction();
      var status = serviceConnection.pingService(repoUrl);
      server.setStatus(status);
    }
    return server;
  }

  private Components getComponents()  {
    var components = new Components();
    components.put("fhir", getFhirComponent());
    components.put("nlp", getNlpComponent());
    components.put("triotech", getTrisotechComponent());
    components.put("kars", getKarsComponent());
    components.put("udp", getUdpComponent());
    components.put("clarity", getClarityComponent());
    components.put("mongo", getMongoComponent());
    return components;
  }

  private Element getUdpComponent() {
    var url = this.environment.getProperty(PropKey.UDP_URL);
    var user = this.environment.getProperty(PropKey.UDP_USER);
    var driver = this.environment.getProperty(PropKey.UDP_DRIVER);
    if (url != null) {
      return getDatabaseComponent(url, user, driver, udpPassword);
    }
    return new Element();
  }

  private Element getClarityComponent() {
    var url = this.environment.getProperty(PropKey.CLARITY_URL);
    var user = this.environment.getProperty(PropKey.CLARITY_USER);
    var driver = this.environment.getProperty(PropKey.CLARITY_DRIVER);
    if (url != null) {
      return getDatabaseComponent(url, user, driver, clarityPassword);
    }
    return new Element();
  }

  // TODO - get the mongo connection
  private Element getMongoComponent() {
    var element = new Element();
    var url = this.environment.getProperty(PropKey.MONGO_URL);
    var db = this.environment.getProperty(PropKey.MONGO_DB);
    if (url != null) {
      element = getServiceComponent(url);
      element.put("database", db);
    }
    return element;
  }

  // TODO - get the fhir connection
  private Element getFhirComponent() {
    var element = new Element();
    var url = this.environment.getProperty(PropKey.FHIR_URL);
    var repoPrefix = this.environment.getProperty(PropKey.FHIR_PREFIX);
    if (url != null) {
      element = getServiceComponent(url);
      element.put("urlPrefix", repoPrefix);
    }
    return element;
  }

  private Element getNlpComponent() {
    var url = this.environment.getProperty(PropKey.NLP_URL);
    var token = this.environment.getProperty("fhir.token");
    if (url != null) {
      return getNlpServiceComponent(url, token);
    }
    return new Element();
  }

  private Element getTrisotechComponent() {
    var url = this.environment.getProperty(PropKey.TRISO_URL);
    if (url != null) {
      return getServiceComponent(url);
    }
    return new Element();
  }

  private Element getKarsComponent() {
    var url = this.environment.getProperty(PropKey.KASRS_URL);
    if (url != null) {
      return getServiceComponent(url);
    }
    return new Element();
  }

  private Element getDatabaseComponent(String url, String user, String driver, String pass)  {
    var element = new Element();
    element.put(URL, url);
    element.put(USER, user);
    element.put(DRIVER, driver);
    var databaseConnection = new DatabaseConnection();
    var status = databaseConnection.pingDatabase(url, user, driver, pass);
    element.put(STATUS, status.toString());
    return element;
  }

  private Element getServiceComponent(String url)  {
    var element = new Element();
    element.put(URL, url);
    var serviceConnection = new ServiceConnecction();
    var status = serviceConnection.pingService(url);
    element.put(STATUS, status.toString());
    return element;
  }

  private Element getNlpServiceComponent(String url, String token)  {
    var element = new Element();
    element.put(URL, url);
    var connection = new NlpConnection();
    var status = connection.pingNlpService(url, token);
    element.put(STATUS, status.toString());
    return element;
  }

  private DeploymentEnvironment getDeploymentEnvironment() {
    String env = this.environment.getProperty(PropKey.ENV);
    if (env != null) {
      switch (env) {
        case "dev":
          return DeploymentEnvironment.DEV;
        case "test":
          return DeploymentEnvironment.TEST;
        case "int":
          return DeploymentEnvironment.INT;
        case "prod":
          return DeploymentEnvironment.PROD;
        case "uat":
          return DeploymentEnvironment.UAT;
        case "local":
          return DeploymentEnvironment.LOCAL;
        default:
          return DeploymentEnvironment.UNKNOWN;
      }
    }
    return DeploymentEnvironment.UNKNOWN;
  }

}
