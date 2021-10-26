package edu.mayo.kmdp.health.utils;

public class PropKey {

  private PropKey() {

  }

  // Health properties
  public static final String ENV = "env";
  public static final String SERVICE_URL = "edu.mayo.kmdp.application.service.url";
  public static final String TRISO_URL = "edu.mayo.kmdp.application.triso.url";
  public static final String KASRS_URL = "edu.mayo.kmdp.application.kasrs.url";
  public static final String MONGO_DB = "edu.mayo.kmdp.application.mongo.db";
  public static final String MONGO_URL = "edu.mayo.kmdp.application.mongo.url";
  public static final String FHIR_URL = "edu.mayo.kmdp.application.fhir.url";
  public static final String FHIR_PREFIX = "edu.mayo.kmdp.application.fhir.urlPrefix";
  public static final String UDP_URL = "edu.mayo.kmdp.application.udp.url";
  public static final String UDP_USER = "edu.mayo.kmdp.application.udp.username";
  public static final String UDP_DRIVER = "edu.mayo.kmdp.application.udp.driver";
  public static final String CLARITY_URL = "edu.mayo.kmdp.application.clarity.url";
  public static final String CLARITY_USER = "edu.mayo.kmdp.application.clarity.username";
  public static final String CLARITY_DRIVER = "edu.mayo.kmdp.application.clarity.driver";
  public static final String NLP_URL = "edu.mayo.kmdp.application.nlp.url";

  // State properties
  public static final String SCAN = "scan.packages";
  public static final String REPO_NAME = "edu.mayo.kmdp.application.repositoryName";
  public static final String REPO_ID = "edu.mayo.kmdp.application.repositoryId";
  public static final String REPO_EXPIRE = "edu.mayo.kmdp.application.expiration";
  public static final String REPO_PATH = "edu.mayo.kmdp.application.repositoryPath";
  public static final String SPLUNK_URL = "edu.mayo.kmdp.splunk.url";
  public static final String SPLUNK_INDEX = "edu.mayo.kmdp.splunk.index.name";
  public static final String SPLUNK_SOURCE = "edu.mayo.kmdp.splunk.source.type";
  public static final String SN_ID = "edu.mayo.kmdp.application.serviceNow.id";
  public static final String SN_URL = "edu.mayo.kmdp.application.serviceNow.url";
  public static final String SN_DISPLAY = "edu.mayo.kmdp.application.serviceNow.display";
  public static final String FHIR_USE_SEP = "edu.mayo.kmdp.application.fhir.useSeparateFhirValueSetServer";
  public static final String SPRING_CACHE = "spring.cache.type";


}
