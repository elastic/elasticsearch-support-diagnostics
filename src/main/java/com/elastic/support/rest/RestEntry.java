package com.elastic.support.rest;

import com.elastic.support.util.SystemProperties;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public class RestEntry {

    private static final Logger logger = LogManager.getLogger(RestEntry.class);

    public static final String MISSING = "missing";

    public RestEntry(String name, String subdir, String extension, boolean retry, String url, boolean showErrors){
        this.name = name;
        this.subdir = subdir;
        this.extension = extension;
        this.retry = retry;
        this.url = url;
        this.showErrors = showErrors;
    }

    public String name;

    public String getName() {
        return name;
    }

    public String  url;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String subdir = SystemProperties.fileSeparator;

    public String getSubdir() {
        return subdir;
    }

    public String extension = "json";

    public String getExtension() {
        return extension;
    }

    public boolean retry = false;

    public boolean isRetry() {
        return retry;
    }

    public boolean showErrors = true;


   /**
    * Creates and returns a copy of this object.
    *
    * @return a new RestEntry object independent of this one.
    */
    @Override
    public Object clone() {
        try {
            return (RestEntry) super.clone();
        } catch (CloneNotSupportedException e) {
            return new RestEntry(this.getName(),
                                this.getSubdir(),
                                this.getExtension(),
                                this.isRetry(),
                                this.getUrl(),
                                this.showErrors);
        }
    }


}
