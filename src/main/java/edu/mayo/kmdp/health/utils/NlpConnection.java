package edu.mayo.kmdp.health.utils;

import com.google.common.collect.Lists;
import edu.mayo.kmdp.health.HealthEndPoint;
import edu.mayo.kmdp.health.datatype.Status;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;

public class NlpConnection {

  public Status pingNlpService(String url, String token)  {
    var headers = new HttpHeaders();
    headers.setBearerAuth(token);
    headers.setAccept(Lists.newArrayList(MediaType.APPLICATION_JSON));
    var entity = new HttpEntity<>(headers);
    url = url +"/projects/mea";

    try {
      var restTemplate = new RestTemplate();
      restTemplate.setUriTemplateHandler(new DefaultUriBuilderFactory(url));
      var response = restTemplate.exchange(url, HttpMethod.GET, entity, HealthEndPoint.class);
      if (response.getStatusCode().value() == 200) {
        return Status.UP;
      } else {
        return Status.DOWN;
      }
    } catch (Exception e)  {
      return Status.IMPAIRED;
    }
  }

}
