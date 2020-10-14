package com.elastic.support;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

public class BaseConfig {

    public String delimiter;
    public int connectionTimeout;
    public int connectionRequestTimeout;
    public int socketTimeout;
    public int maxTotalConn;
    public int maxConnPerRoute;
    public long idleExpire;

    public String diagHost = "https://api.github.com";
    public String diagQuery = "/repos/elastic/support-diagnostics/releases/latest";
    public String diagLatestRelease = "https://api.github.com/repos/elastic/support-diagnostics/releases/latest";

    public Map<String, String> dockerGlobal;
    public Map<String, String> dockerContainer;
    public String dockerContainerIds;
    public String dockerExecutablePath;

    protected Map configuration;

    public BaseConfig(Map configuration) {

        this.configuration = configuration;

        Map<String, String> githubSettings = (Map<String, String>) configuration.get("github-settings");

        if ( githubSettings != null){
            if (StringUtils.isNotEmpty(githubSettings.get("diagHost"))) {
                diagHost = githubSettings.get("diagHost");
            }
            if (StringUtils.isNotEmpty(githubSettings.get("diagQuery"))) {
                diagQuery = githubSettings.get("diagQuery");
            }
        }

        Map<String, Integer> restConfig = (Map<String, Integer>) configuration.get("rest-config");
        connectionTimeout = restConfig.get("connectTimeout") * 1000;
        connectionRequestTimeout = restConfig.get("requestTimeout") * 1000;
        socketTimeout = restConfig.get("socketTimeout") * 1000;
        maxTotalConn = restConfig.get("maxTotalConn");
        maxConnPerRoute = restConfig.get("maxConnPerRoute");
        idleExpire = restConfig.get("idleExpire");

        dockerGlobal = (Map<String, String>) configuration.get("docker-global");
        dockerContainer = (Map<String, String>) configuration.get("docker-container");
        dockerContainerIds = (String) configuration.get("docker-container-ids");
        dockerExecutablePath = (String) configuration.get("docker-executable-location");

        delimiter = ObjectUtils.defaultIfNull(configuration.get("credentials-delimiter"), ":").toString();

    }

}
