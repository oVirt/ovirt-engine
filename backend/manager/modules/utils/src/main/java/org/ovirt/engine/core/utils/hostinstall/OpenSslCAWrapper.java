package org.ovirt.engine.core.utils.hostinstall;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

import org.apache.commons.codec.binary.Base64;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.utils.FileUtil;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;

public class OpenSslCAWrapper {

    public static String getCACertificate() throws Exception {

        InputStream in = null;

        try {
            in = new FileInputStream(Config.resolveCACertificatePath());

            final CertificateFactory cf = CertificateFactory.getInstance("X.509");
            final Certificate certificate = cf.generateCertificate(in);

            return String.format(
                (
                    "-----BEGIN CERTIFICATE-----%1$c" +
                    "%2$s" +
                    "-----END CERTIFICATE-----%1$c"
                ),
                '\n',
                new Base64(
                    76,
                    new byte[] { (byte)'\n' }
                ).encodeToString(
                    certificate.getEncoded()
                )
            );
        }
        finally {
            if (in != null) {
                in.close();
            }
        }
    }

    public static String signCertificateRequest(
        String request,
        String label,
        String hostname
    ) throws IOException {
        File pkicertdir = new File(Config.resolveCABasePath(), "certs");
        File pkireqdir = new File(Config.resolveCABasePath(), "requests");
        String reqFileName = String.format("%1$sreq.pem", label);
        String certFileName = String.format("%1$scert.pem", label);

        OutputStream os = null;
        try {
            os = new FileOutputStream(
                new File(
                    pkireqdir,
                    reqFileName
                )
            );
            os.write(request.getBytes("UTF-8"));
        }
        finally {
            if (os != null) {
                try {
                    os.close();
                }
                catch (IOException e) {
                    log.error("error during close", e);
                }
            }
        }

        if (
            !new OpenSslCAWrapper().signCertificateRequest(
                reqFileName,
                hostname,
                Config.<Integer> GetValue(ConfigValues.VdsCertificateValidityInYears) * 365,
                certFileName
            )
        ) {
            throw new RuntimeException("Certificate enrollment failed");
        }

        return FileUtil.readAllText(new File(pkicertdir, certFileName).getPath());
    }

    public final boolean signCertificateRequest(
        String requestFileName,
        String hostname,
        int days,
        String signedCertificateFileName
    ) {
        log.debug("Entered signCertificateRequest");
        boolean returnValue = true;
        String signRequestBatch = Config.resolveSignScriptPath();
        if (new File(signRequestBatch).exists()) {
            String organization = Config.<String> GetValue(ConfigValues.OrganizationName);
            Integer signatureTimeout = Config.<Integer> GetValue(ConfigValues.SignCertTimeoutInSeconds);
            String[] command_array =
                    createCommandArray(signatureTimeout, signRequestBatch, requestFileName,
                            hostname, organization, days,
                            signedCertificateFileName);
            returnValue = runCommandArray(command_array, signatureTimeout);
        } else {
            log.error(String.format("Sign certificate request file '%s' not found", signRequestBatch));
            returnValue = false;
        }
        log.debug("End of signCertificateRequest");
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
            stdOutput = new BufferedReader(new InputStreamReader(process.getInputStream(), "UTF-8"));
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
        StringBuilder returnString = new StringBuilder();
        try {
            while ((tempString = bufferedReader.readLine()) != null) {
                returnString.append(tempString).append('\n');
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
            String hostname,
            String organization,
            int days,
            String signedCertificateFileName) {
        log.debug("Building command array for Sign Certificate request script");
        String baseDirectoryPath = Config.resolveCABasePath();
        String keystorePass = Config.<String> GetValue(ConfigValues.keystorePass);
        Calendar yesterday = Calendar.getInstance();
        yesterday.add(Calendar.DATE, -1);
        SimpleDateFormat format = new SimpleDateFormat("yyMMddHHmmssZ");
        format.setTimeZone(TimeZone.getTimeZone("UTC"));
        String[] command_array = { signRequestBatch, requestFileName, signedCertificateFileName, "" + days,
                baseDirectoryPath, format.format(yesterday.getTime()), keystorePass,
                hostname, organization,
                "" + (signatureTimeout / 2) };
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
        if (output != null && output.length() != 0) {
            log.debug("Sign Certificate request script output:\n" + output);
        }
    }

    /**
     * This method is meant to enable testing, since Runtime cannot be mocked statically
     */
    protected Runtime getRuntime() {
        return Runtime.getRuntime();
    }

    private static Log log = LogFactory.getLog(OpenSslCAWrapper.class);
}
