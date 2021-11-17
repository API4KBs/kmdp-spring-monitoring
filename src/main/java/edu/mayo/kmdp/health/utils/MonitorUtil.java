package edu.mayo.kmdp.health.utils;

import static edu.mayo.kmdp.util.DateTimeUtil.serializeAsDateTime;
import static org.springframework.web.servlet.support.ServletUriComponentsBuilder.fromCurrentContextPath;

import edu.mayo.kmdp.health.datatype.ApplicationComponent;
import edu.mayo.kmdp.health.datatype.BuildInfo;
import edu.mayo.kmdp.health.datatype.ServiceNowInfo;
import edu.mayo.kmdp.health.datatype.Status;
import edu.mayo.kmdp.util.Util;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.springframework.boot.info.BuildProperties;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MutablePropertySources;

public final class MonitorUtil {

  private MonitorUtil() {
    // functions only
  }

  /**
   * Pulls the well-known SN config properties from the given environment
   *
   * @param environment
   * @return
   */
  public static ServiceNowInfo getServiceNowInfo(Environment environment) {
    var sn = new ServiceNowInfo();
    sn.setId(environment.getProperty(PropKey.SN_ID.key));
    sn.setUrl(environment.getProperty(PropKey.SN_URL.key));
    sn.setDisplay(environment.getProperty(PropKey.SN_DISPLAY.key));
    return sn;
  }

  /**
   * Pulls the well-known Build properties from the given build context
   *
   * @param build
   * @return
   */
  public static BuildInfo getBuildProps(BuildProperties build) {
    var buildProps = new BuildInfo();
    buildProps.setArtifactName(build.getName());
    buildProps.setVersion(build.getVersion());
    buildProps.setGroupId(build.getGroup());
    buildProps.setArtifactId(build.getArtifact());
    buildProps.setBuildTime(formatInstant(build.getTime()));
    return buildProps;
  }

  /**
   * Pulls the base URL the server is deployed at
   *
   * @return
   */
  public static String getServiceBaseUrl() {
    return fromCurrentContextPath().build().toUriString();
  }

  /**
   * Obfuscates a secret password / token / key
   * <p>
   * Replaces the characters in the String with '*', except for the first min(N,4) characters
   *
   * @param secret
   * @param N
   * @return
   */
  public static String obfuscate(String secret, int N) {
    if (Util.isEmpty(secret)) {
      return null;
    }
    String trimPass = secret.trim();
    int len = trimPass.length();
    int j = Math.min(4, N);
    return trimPass.substring(0, j) + "*".repeat(len - j);
  }

  /**
   * Formats a timestamp. Provided for consistency across monitoring endpoints
   *
   * @param time
   * @return
   */
  public static String formatInstant(Instant time) {
    return serializeAsDateTime(time.atZone(ZoneId.systemDefault()));
  }

  /**
   * Default rule that maps the status of the components to the status of the whole application
   * <p>
   * Uses the 'worst' of the statuses:
   * <ul>
   *   <li>If any component is DOWN, the server is DOWN</li>
   *   <li>else if any component is IMPAIRED, the server is IMPAIRED</li>
   *   <li>else the server is UP</li>
   * </ul>
   *
   * @param components
   * @return
   */
  public static Status defaultAggregateStatus(List<ApplicationComponent> components) {
    return components.stream()
        .map(ApplicationComponent::getStatus)
        .reduce((s1, s2) -> s1.ordinal() > s2.ordinal() ? s1 : s2)
        .orElse(Status.UP);
  }

  /**
   * Converts java Properties into a Map of the given type Sorts entries by Key, for Map types that
   * support ordering
   *
   * @param prop
   * @return
   */
  public static Map<String, String> streamConvert(Properties prop,
      Supplier<Map<String, String>> mapTypeConstructor) {
    return prop.entrySet().stream()
        .sorted(Comparator.comparing(e -> e.getKey().toString()))
        .collect(
            Collectors.toMap(
                e -> String.valueOf(e.getKey()),
                e -> String.valueOf(e.getValue()),
                (prev, next) -> next,
                mapTypeConstructor));
  }

  /**
   * Gathers configuration properties from the environment
   * <p>
   * Excludes "system" properties, to focus on "application" properties
   *
   * @param environment
   * @return
   */
  public static Properties getEnvironmentProperties(ConfigurableEnvironment environment) {
    Properties props = new Properties();
    MutablePropertySources propSrcs = environment.getPropertySources();
    StreamSupport.stream(propSrcs.spliterator(), false)
        .filter(ps -> ps instanceof EnumerablePropertySource)
        // exclude 'system' sources which include all the OS/VM related properties...
        .filter(ps -> !"systemEnvironment".equals(ps.getName()))
        .filter(ps -> !"systemProperties".equals(ps.getName()))
        .map(ps -> ((EnumerablePropertySource<?>) ps).getPropertyNames())
        .flatMap(Arrays::stream)
        .forEach(propName -> props.setProperty(propName, environment.getProperty(propName)));

    // ...except for ensuring CATALINA_OPTS is pulled despite being 'system'
    String opts = environment.getProperty("CATALINA_OPTS");
    if (opts != null) {
      Arrays.stream(opts.split("\\s"))
          .filter(Util::isNotEmpty)
          .map(s -> s.replace("-D", ""))
          .filter(p -> p.contains("="))
          .forEach(p -> {
            int eq = p.indexOf('=');
            String key = p.substring(0, eq);
            props.setProperty(key, environment.getProperty(key));
          });
    }
    return props;
  }


  /**
   * Retyrns application-specific environment properties, by default defined as properties whose
   * names start with a prefix, itself defined by the value of the well-known property {@link
   * PropKey#APP_PROPS_PREFIX}
   *
   * @param env
   * @return
   */
  public static Map<String, String> getAppProperties(ConfigurableEnvironment env) {
    String prefix = env.getProperty(PropKey.APP_PROPS_PREFIX.key);
    if (prefix == null) {
      return Collections.emptyMap();
    }
    Properties props = getEnvironmentProperties(env);
    return props.stringPropertyNames().stream()
        .filter(name -> name.startsWith(prefix))
        .collect(Collectors.toMap(
            n -> n,
            n -> (String) props.get(n)
        ));
  }
}
