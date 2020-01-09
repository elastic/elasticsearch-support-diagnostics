package com.elastic.support.util;


import com.elastic.support.Constants;
import com.elastic.support.diagnostics.DiagnosticException;
import com.elastic.support.diagnostics.DiagnosticInputs;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;


public class SystemUtils {

    private static final Logger logger = LogManager.getLogger(SystemUtils.class);

    public static void writeToFile(String content, String dest) {
        try {
            logger.info("Writing to {}", dest);
            File outFile = new File(dest);
            FileUtils.writeStringToFile(outFile, content, "UTF-8");
        } catch (IOException e) {
            logger.log(SystemProperties.DIAG, "Error writing content for {}", dest, e);
            throw new DiagnosticException("Error writing " + dest + " See diagnostic.log for details.");
        }
    }

    public static void streamClose(String path, InputStream instream) {

        if (instream != null) {
            try {
                instream.close();
            } catch (Throwable t) {
                logger.error("Error encountered when attempting to close file {}", path);
            }
        } else {
            logger.error("Error encountered when attempting to close file: null InputStream {}", path);
        }

    }

    public static void nukeDirectory(String dir){
        try {
            File tmp = new File(dir);
            tmp.setWritable(true, false);
            FileUtils.deleteDirectory(tmp);
            logger.info("Deleted directory: {}.", dir);
        } catch (IOException e) {
            logger.error("Delete of directory:{} failed. Usually this indicates a permission issue", dir, e);
        }
    }

    public static String getHostName() {
        String s = null;

        BufferedReader stdInput = null;
        BufferedReader stdError = null;

        try {
            Process p = Runtime.getRuntime().exec("hostname");

            stdInput = new BufferedReader(new
                    InputStreamReader(p.getInputStream()));

            stdError = new BufferedReader(new
                    InputStreamReader(p.getErrorStream()));

            s = stdInput.readLine();

        } catch (IOException e) {
            logger.error("Error retrieving hostname.", e);
        }
        finally {
            try {
                if(stdError != null){
                    stdError.close();
                }
            } catch (IOException e) {
                logger.error("Couldn't close stderror stream");
            }
            try {
                if(stdInput != null){
                    stdInput.close();
                }
            } catch (IOException e) {
                logger.error("Couldn't close stdinput stream");
            }
        }

        return s;
    }

    public static Set<String> getNetworkInterfaces(){
        Set<String> ipAndHosts = new HashSet<>();

        try {
            // Get the system hostname and add it.
            String hostName = getHostName();
            ipAndHosts.add(hostName);
            Enumeration<NetworkInterface> nics = NetworkInterface.getNetworkInterfaces();

            while (nics.hasMoreElements()) {
                NetworkInterface nic = nics.nextElement();
                ipAndHosts.add(nic.getDisplayName());
                Enumeration<InetAddress> inets = nic.getInetAddresses();

                while (inets.hasMoreElements()) {
                    InetAddress inet = inets.nextElement();
                    ipAndHosts.add(inet.getHostAddress());
                    ipAndHosts.add(inet.getHostName());
                    ipAndHosts.add(inet.getCanonicalHostName());
                }
            }
        } catch (Exception e) {
            logger.error("Error occurred acquiring IP's and hostnames", e);
        }

        return ipAndHosts;

    }

    public static String buildStringFromChar(int len, char seed){

        char[] charArray = new char[len];
        Arrays.fill(charArray, seed);
        return new String(charArray);

    }

    public static String getCurrentDate() {
        //DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return dtf.format(ZonedDateTime.now());
    }

    public static String getCurrentTime() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm");
        return dtf.format(ZonedDateTime.now());
    }

    public static String parseOperatingSystemName(String osName){

        osName = osName.toLowerCase();

        if (osName.contains("windows")) {
            return Constants.winPlatform;

        } else if (osName.contains("linux")) {
            return Constants.linuxPlatform;

        } else if (osName.contains("darwin") || osName.contains("mac")) {
            return Constants.macPlatform;

        } else {
            // default it to Linux
            logger.error("Unsupported OS -defaulting to Linux: {}", osName);
            return  Constants.linuxPlatform;
        }
    }

}