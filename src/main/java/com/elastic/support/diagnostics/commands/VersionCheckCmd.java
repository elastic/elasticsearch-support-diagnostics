package com.elastic.support.diagnostics.commands;

import com.elastic.support.config.DiagnosticInputs;
import com.elastic.support.config.Version;
import com.elastic.support.diagnostics.DiagnosticException;
import com.elastic.support.diagnostics.chain.Command;
import com.elastic.support.diagnostics.chain.DiagnosticContext;
import com.elastic.support.rest.RestClient;
import com.elastic.support.rest.RestResult;
import com.elastic.support.util.JsonYamlUtils;
import com.fasterxml.jackson.databind.JsonNode;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class VersionCheckCmd implements Command {

    /**
     * Gets the version of Elasticsearch that is running. This also
     * acts as a sanity check. If there are connection issues and it fails
     * this will bet the first indication since this is lightweight enough
     * that is should usually succeed. If we don't have a version we
     * won't be able to generate the correct call selection later on.
     */
    private final Logger logger = LogManager.getLogger(VersionCheckCmd.class);

    public void execute(DiagnosticContext context) {

        // Get the version number from the JSON returned
        // by just submitting the host/port combo
        logger.info("Getting Elasticsearch Version.");

        try {
            DiagnosticInputs diagnosticInputs = context.getDiagnosticInputs();
            RestClient restClient = context.getEsRestClient();
            RestResult res = restClient.execQuery("/");
            if(res.getStatus() != 200)){
                throw new DiagnosticException("Unrecoverable ES query error.");
            }

            String result = res.toString();
            JsonNode root = JsonYamlUtils.createJsonNodeFromString(result);
            String versionNumber = root.path("version").path("number").asText();
            Version version = new Version(versionNumber);
            context.setVersion(version);
        } catch (Exception e) {
            logger.error("Could not retrieve the Elasticsearch version - unable to continue.");
            throw new DiagnosticException("Undetermined error", e);
        }

    }
}
