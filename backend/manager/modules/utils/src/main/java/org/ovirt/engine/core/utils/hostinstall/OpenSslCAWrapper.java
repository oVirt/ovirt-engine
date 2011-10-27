package org.ovirt.engine.core.utils.hostinstall;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.compat.LogCompat;
import org.ovirt.engine.core.compat.LogFactoryCompat;
import org.ovirt.engine.core.compat.backendcompat.File;

public class OpenSslCAWrapper implements ICAWrapper {
    public final boolean SignCertificateRequest(String requestFileName, int days, String signedCertificateFileName) {
        log.debug("Entered SignCertificateRequest");
        boolean returnValue = true;
        String signRequestBatch = Config.resolveSignScriptPath();
        if (File.Exists(signRequestBatch)) {
            Integer signatureTimeout = Config.<Integer> GetValue(ConfigValues.SignCertTimeoutInSeconds);
            String[] command_array =
                    createCommandArray(signatureTimeout, signRequestBatch, requestFileName, days,
                            signedCertificateFileName);
            returnValue = runCommandArray(command_array, signatureTimeout);
        } else {
            log.error(String.format("Sign certificate request file '%s' not found", signRequestBatch));
            returnValue = false;
        }
        log.debug("End of SignCertificateRequest");
        return returnValue;
    }

    /**
     * Runs the SignReq.sh script
     * @param command_array
     * @param signatureTimeout
     * @return whether or not the certificate signing was successful
     */
    private boolean runCommandArray(String[] command_array, int signatureTimeout) {
        boolean returnValue = true;
        String outputString = null;
        String errorString = null;
        BufferedReader stdOutput = null;
        BufferedReader stdError = null;
        try {
            log.debug("Running Sign Certificate request script");
            Process process = getRuntime().exec(command_array);
            stdOutput = new BufferedReader(new InputStreamReader(process.getInputStream()));
            stdError = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            int exitCode = 0;
            boolean completed = false;
            for (int x = 0; x < signatureTimeout; x++) {
                try {
                    Thread.sleep(1000);
                    exitCode = process.exitValue();
                    completed = true;
                    break;
                } catch (IllegalThreadStateException e) {
                    // keep going
                } catch (InterruptedException ie) {
                    // keep going
                }
            }
            outputString += readAllLines(stdOutput);
            errorString += readAllLines(stdError);
            if (!completed) {
                process.destroy();
                returnValue = false;
                log.error("Sign Certificate request script killed due to timeout");
                logOutputAndErrors(outputString, errorString);
            } else if (exitCode != 0) {
                returnValue = false;
                log.error("Sign Certificate request failed with exit code " + exitCode);
                logOutputAndErrors(outputString, errorString);
            } else {
                log.debug("Successfully completed certificate signing script");
            }
        } catch (IOException e) {
            log.error("Exception signing the certificate", e);
            logOutputAndErrors(outputString, errorString);
            returnValue = false;
        } finally {
            // Close the BufferedReaders
            try {
                if (stdOutput != null) {
                    stdOutput.close();
                }
                if (stdError != null) {
                    stdError.close();
                }
            } catch (IOException ex) {
                log.error("Unable to close BufferedReader");
            }
        }
        return returnValue;
    }

    /**
     * Returns a formatted String from the content of the given bufferedReader
     * @param bufferedReader
     */
    private String readAllLines(BufferedReader bufferedReader) {
        String tempString;
        String returnString = null;
        try {
            while ((tempString = bufferedReader.readLine()) != null) {
                returnString += tempString + '\n';
            }
        } catch (IOException e) {
            log.error("IOException while trying to read from buffer", e);
        }
        return returnString.toString();
    }

    /**
     * Prepares a String array in order to run the SignReq.sh script
     * @param signatureTimeout
     * @param signRequestBatch
     *            the script name
     * @param requestFileName
     * @param days
     * @param signedCertificateFileName
     * @return the command_array ready to run
     */
    private String[] createCommandArray(Integer signatureTimeout,
            String signRequestBatch,
            String requestFileName,
            int days,
            String signedCertificateFileName) {
        log.debug("Building command array for Sign Certificate request script");
        String baseDirectoryPath = Config.resolveCABasePath();
        String keystorePass = Config.<String> GetValue(ConfigValues.keystorePass);
        String lockfileName = Config.<String> GetValue(ConfigValues.SignLockFile);
        Calendar today = Calendar.getInstance();
        today.add(Calendar.DATE, -1);
        SimpleDateFormat format = new SimpleDateFormat("yyMMddHHmmssZ");
        String yesterday = format.format(today.getTime());
        String[] command_array = { signRequestBatch, requestFileName, signedCertificateFileName, "" + days,
                baseDirectoryPath, yesterday, keystorePass, lockfileName, "" + (signatureTimeout / 2) };
        log.debug("Finished building command array for Sign Certificate request script");
        return command_array;
    }

    /**
     * Logs the given output to DEBUG, and the given errors to ERROR
     * @param output
     *            The formatted output from the script
     * @param errors
     *            The formatted errors from the script
     */
    private void logOutputAndErrors(String output, String errors) {
        if (errors != null && errors.length() != 0) {
            log.error("Sign Certificate request script errors:\n" + errors);
        }
        if (output != null && errors.length() != 0) {
            log.debug("Sign Certificate request script output:\n" + output);
        }
    }

    /**
     * This method is meant to enable testing, since Runtime cannot be mocked statically
     */
    protected Runtime getRuntime() {
        return Runtime.getRuntime();
    }

    private static LogCompat log = LogFactoryCompat.getLog(OpenSslCAWrapper.class);
}
