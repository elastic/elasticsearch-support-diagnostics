package com.elastic.support.diagnostics.commands;

import com.elastic.support.config.DiagConfig;
import com.elastic.support.diagnostics.chain.DiagnosticContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

public class RunClusterQueriesCmd extends BaseQueryCmd {

    /**
     * Builds the list of queries for Elasticsearch version that was retrieved previously,
     * then executes them and saves the result to temporary storage.
     */

    private static final Logger logger = LogManager.getLogger(RunClusterQueriesCmd.class);

    public void execute(DiagnosticContext context) {

        String version = context.getVersion();

        DiagConfig diagConfig = context.getDiagsConfig();
        Map restCalls = diagConfig.getRestCalls();

        Map<String, String> entries = buildStatementsByVersion(version, restCalls);

        runQueries(context.getEsRestClient(), entries, context.getTempDir(), diagConfig);

    }

    public Map<String, String> buildStatementsByVersion(String version, Map calls) {

        String[] ver = version.split("\\.");
        int major = Integer.parseInt(ver[0]);
        int minor = Integer.parseInt(ver[1]);

        Map statements = new LinkedHashMap<>();

        // First get the statements that work for all versions
        statements.putAll((Map) calls.get("common"));

        // Go through any additional calls up to the current version
        Map versionSpecificCalls = ((Map) calls.get("versions"));
        for (int ma = 1; ma <= major; ma++) {
            String majKey = "major-" + ma;

            // See if there are calls specific to this major version
            // If nothing skip to next major version
            Map majorVersionCalls = getCalls(majKey, versionSpecificCalls);
            if (majorVersionCalls.size() == 0) {
                continue;
            }

            //If we are on a lower major version get all the minors
            if (ma < major) {
                Collection values = majorVersionCalls.values();
                for (Object verEntry : values) {
                    statements.putAll((Map) verEntry);
                }
            }
            // Otherwise just get the ones at or below the input minor
            else {
                for (int mi = 0; mi <= minor; mi++) {
                    String minKey = "minor-" + mi;
                    statements.putAll(getCalls(minKey, majorVersionCalls));
                }
            }
        }

        return statements;
    }

    // Don't want to check each potential addiition to the base statements for emptiness so just
    // use an emoty map if there are no results for this version check.
    public Map getCalls(String key, Map calls) {
        Map result = (Map) calls.get(key);
        if (result == null) {
            return new LinkedHashMap();
        }
        return result;
    }

}
