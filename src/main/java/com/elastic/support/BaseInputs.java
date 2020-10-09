package com.elastic.support;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.elastic.support.diagnostics.ShowHelpException;
import com.elastic.support.util.ResourceCache;
import com.elastic.support.util.SystemProperties;

import com.elastic.support.util.SystemUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.beryx.textio.*;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class BaseInputs {

    private static final Logger logger = LogManager.getLogger(BaseInputs.class);
    public static final String outputDirDescription = "Fully qualified path to an output directory. If it does not exist the diagnostic will attempt to create it. If not specified the diagnostic directory will be used: ";
    protected List<String> emptyList = new ArrayList<>();
    protected JCommander jCommander;

    // Input Fields

    @Parameter(names = {"-?", "--help"}, description = "Help contents.", help = true)
    public boolean help;

    // If no output directory was specified default to the working directory
    @Parameter(names = {"-o", "--out", "--output", "--outputDir"}, description = outputDirDescription)
    public String outputDir = SystemProperties.userDir;
    public boolean interactive = false;

    // Stop the diag from checking itself for latest version.


    // End Input Fields

    // Input Readers
    // Generic - change the read label only
    // Warning: Setting default values may leak into later prompts if not reset. Better to use a new Reader.
    protected StringInputReader standardStringReader = ResourceCache.textIO.newStringInputReader()
            .withMinLength(0)
            .withInputTrimming(true);
    protected BooleanInputReader standardBooleanReader = ResourceCache.textIO.newBooleanInputReader();
    protected StringInputReader  standardPasswordReader = ResourceCache.textIO.newStringInputReader()
            .withInputMasking(true)
            .withInputTrimming(true)
            .withMinLength(0);
    protected StringInputReader standardFileReader = ResourceCache.textIO.newStringInputReader()
            .withInputTrimming(true)
            .withValueChecker((String val, String propname) -> validateFile(val));
    // End Input Readers

    public boolean runningInDocker = SystemUtils.isRunningInDocker();

    public BaseInputs(){
        if(runningInDocker){
            outputDir = "/diagnostic-output";
        }
    }

    public abstract void runInteractive();

    public List<String> parseInputs(String[] args){
        logger.info(Constants.CONSOLE, "Processing diagnosticInputs...");
        jCommander = new JCommander(this);
        jCommander.setCaseSensitiveOptions(true);
        jCommander.parse(args);
        // If we're in help just shut down.
        if (help) {
            jCommander.usage();
            throw new ShowHelpException();
        }

        return ObjectUtils.defaultIfNull(validateDir(outputDir), emptyList);

    }

    public void usage(){
        if(jCommander != null){
            jCommander.usage();
        }
        else{
            logger.error(Constants.CONSOLE, "Please rerun with the --help option or with no arguments for interactive mode.");
        }
    }

    protected void runOutputDirInteractive(){
        String output = ResourceCache.textIO.newStringInputReader()
                .withMinLength(0)
                .withValueChecker(( String val, String propname) -> validateOutputDirectory(val))
                .read(SystemProperties.lineSeparator + outputDirDescription);
        if(StringUtils.isNotEmpty(output)){
            outputDir = output;
        }
    }

    public List<String> validatePort(int val){
        if (val < 1 || val > 65535){
            return Collections.singletonList("Outside the valid range of port values. 1-65535 ");
        }
        return null;
    }

    public List<String> validateOutputDirectory(String val){
        try {
            if (StringUtils.isEmpty(val.trim())) {
                return null;
            }

            File file = new File(val);
            if(!file.exists()){
                file.mkdir();
            }
        } catch (Exception e) {
            logger.error( e);
            return Collections.singletonList("Output directory did not exist and could not be created. " + Constants.CHECK_LOG);
        }

        return null;

    }

    public List<String> validateFile(String val) {
        if (StringUtils.isEmpty(val.trim())) {
            return null;
        }

        File file = new File(val);

        if (!file.exists()) {
            return Collections.singletonList("Specified file could not be located.");
        }

        return null;

    }

    public List<String> validateDir(String val) {
        if (StringUtils.isEmpty(val.trim())) {
            return null;
        }

        File file = new File(val);

        if (!file.exists() || !file.isDirectory()) {
            return Collections.singletonList("Specified directory location could not be located or is not a directory.");
        }

        return null;

    }

    public List<String> validateRequiredFile(String val) {
        if (StringUtils.isEmpty(val.trim())) {
            return Collections.singletonList("Input file is required.");
        }

        File file = new File(val);

        if (!file.exists() || file.isDirectory()) {
            return Collections.singletonList("Specified required file location could not be located or is a directory.");
        }

        return null;

    }

    @Override
    public String toString() {
        return "BaseInputs{" +
                "outputDir='" + outputDir + '\'' +
                '}';
    }

}
