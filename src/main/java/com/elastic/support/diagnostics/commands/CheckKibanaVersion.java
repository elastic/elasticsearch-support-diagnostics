package com.elastic.support.diagnostics.commands;

import com.elastic.support.Constants;
import com.elastic.support.diagnostics.DiagnosticException;
import com.elastic.support.diagnostics.DiagnosticInputs;
import com.elastic.support.diagnostics.chain.Command;
import com.elastic.support.diagnostics.chain.DiagnosticContext;
import com.elastic.support.rest.RestClient;
import com.elastic.support.rest.RestEntryConfig;
import com.elastic.support.rest.RestResult;
import com.elastic.support.util.JsonYamlUtils;
import com.elastic.support.util.ResourceCache;
import com.elastic.support.util.SystemProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.vdurmont.semver4j.Semver;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;


public class CheckKibanaVersion implements Command {

    /**
     * Gets the version of Kibana that is running. This also
     * acts as a sanity check. If there are connection issues and it fails
     * this will bet the first indication since this is lightweight enough
     * that is should usually succeed. If we don't have a version we
     * won't be able to generate the correct call selection later on.
     */
    private static final Logger logger = LogManager.getLogger(CheckKibanaVersion.class);

    public void execute(DiagnosticContext context) {

        // Get the version number from the JSON returned
        // by just submitting the host/port combo
        logger.info(Constants.CONSOLE, "Getting Kibana Version.");
        DiagnosticInputs inputs = context.diagnosticInputs;

        try {
           RestClient restClient = RestClient.getClient(
                    context.diagnosticInputs.host,
                    context.diagnosticInputs.port,
                    context.diagnosticInputs.scheme,
                    context.diagnosticInputs.user,
                    context.diagnosticInputs.password,
                    context.diagnosticInputs.proxyHost,
                    context.diagnosticInputs.proxyPort,
                    context.diagnosticInputs.proxyUser,
                    context.diagnosticInputs.proxyPassword,
                    context.diagnosticInputs.pkiKeystore,
                    context.diagnosticInputs.pkiKeystorePass,
                    context.diagnosticInputs.skipVerification,
                    context.diagsConfig.connectionTimeout,
                    context.diagsConfig.connectionRequestTimeout,
                    context.diagsConfig.socketTimeout);

           // Add it to the global cache - automatically closed on exit.
            ResourceCache.addRestClient(Constants.restInputHost, restClient);
            context.version = getKibanaVersion(restClient);
            String version = context.version.getValue();
            RestEntryConfig builder = new RestEntryConfig(version);
            Map restCalls = JsonYamlUtils.readYamlFromClasspath(Constants.KIBANA_REST, true);

            context.elasticRestCalls = builder.buildEntryMap(restCalls);

        } catch (DiagnosticException de) {
            throw de;
        } catch (Exception e) {
            logger.error( "Unanticipated error:", e);
            throw new DiagnosticException(String.format("Could not retrieve the Kibana version due to a system or network error - unable to continue. %s%s%s", e.getMessage(), SystemProperties.lineSeparator, Constants.CHECK_LOG));
        }
    }

    public static Semver getKibanaVersion(RestClient client){
            RestResult res = client.execQuery("/api/settings");
            if (! res.isValid()) {
                throw new DiagnosticException( res.formatStatusMessage( "Could not retrieve the Kibana version - unable to continue."));
            }
            String result = res.toString();
            logger.info(Constants.CONSOLE, "result:");
            logger.info(Constants.CONSOLE, result);
            JsonNode root = JsonYamlUtils.createJsonNodeFromString(result);
            String version = root.path("settings").path("kibana").path("version").asText();
            logger.info(Constants.CONSOLE, String.format("Kibana Version is :%s", version));
            return new Semver(version, Semver.SemverType.NPM);
    }

}
