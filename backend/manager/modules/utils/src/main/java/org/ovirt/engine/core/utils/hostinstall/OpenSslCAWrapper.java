package org.ovirt.engine.core.utils.hostinstall;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.Charset;

import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.utils.EngineLocalConfig;
import org.ovirt.engine.core.utils.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OpenSslCAWrapper {

    private static final Logger log = LoggerFactory.getLogger(OpenSslCAWrapper.class);

    private static String escapeSubjectComponent(String input) {
        StringBuilder ret = new StringBuilder();

        for (char x : input.toCharArray()) {
            if (x == '/' || x == '\\') {
                ret.append('\\');
            }
            ret.append(x);
        }

        return ret.toString();
    }

    public static String signCertificateRequest(
        String request,
        String hostname
    ) throws IOException {
        EngineLocalConfig config = EngineLocalConfig.getInstance();

        try (
            final OutputStream os = new FileOutputStream(
                new File(
                    new File(config.getPKIDir(), "requests"),
                    String.format("%s.req", hostname)
                )
            )
        ) {
            os.write(request.getBytes(Charset.forName("UTF-8")));
        }

        if (
            !new OpenSslCAWrapper().signCertificateRequest(
                new File(new File(config.getUsrDir(), "bin"), "pki-enroll-request.sh"),
                hostname
            )
        ) {
            throw new RuntimeException("Certificate enrollment failed");
        }

        return FileUtil.readAllText(
            new File(new File(config.getPKIDir(), "certs"),
            String.format("%s.cer", hostname)
        ).getPath());
    }

    public final boolean signCertificateRequest(
        File executable,
        String hostname
    ) {
        log.debug("Entered signCertificateRequest");
        boolean returnValue = true;
        if (executable.exists()) {
            String organization = Config.<String> getValue(ConfigValues.OrganizationName);
            int days = Config.<Integer> getValue(ConfigValues.VdsCertificateValidityInYears) * 365;
            Integer signatureTimeout = Config.<Integer> getValue(ConfigValues.SignCertTimeoutInSeconds);
            returnValue = runCommandArray(
                new String[] {
                    executable.getAbsolutePath(),
                    String.format("--name=%s", hostname),
                    String.format("--subject=/O=%s/CN=%s", escapeSubjectComponent(organization), escapeSubjectComponent(hostname)),
                    String.format("--days=%s", days),
                    String.format("--timeout=%s", signatureTimeout / 2)
                },
                signatureTimeout
            );
        } else {
            log.error("Sign certificate request file '{}' not found", executable.getPath());
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
        try {
            log.debug("Running Sign Certificate request script");
            Process process = getRuntime().exec(command_array);
            try (
                final BufferedReader stdOutput = new BufferedReader(new InputStreamReader(process.getInputStream(), Charset.forName("UTF-8")));
                final BufferedReader stdError = new BufferedReader(new InputStreamReader(process.getErrorStream(), Charset.forName("UTF-8")));
            ) {
                int exitCode = 0;
                boolean completed = false;
                for (int x = 0; x < signatureTimeout; x++) {
                    try {
                        Thread.sleep(1000);
                        exitCode = process.exitValue();
                        completed = true;
                        break;
                    } catch (IllegalThreadStateException | InterruptedException e) {
                        // keep going
                    }
                }
                outputString = readAllLines(stdOutput);
                errorString = readAllLines(stdError);
                if (!completed) {
                    process.destroy();
                    returnValue = false;
                    log.error("Sign Certificate request script killed due to timeout");
                    logOutputAndErrors(outputString, errorString);
                } else if (exitCode != 0) {
                    returnValue = false;
                    log.error("Sign Certificate request failed with exit code {}", exitCode);
                    logOutputAndErrors(outputString, errorString);
                } else {
                    log.debug("Successfully completed certificate signing script");
                }
            }
        } catch (IOException e) {
            log.error("Exception signing the certificate", e);
            logOutputAndErrors(outputString, errorString);
            returnValue = false;
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
     * Logs the given output to DEBUG, and the given errors to ERROR
     * @param output
     *            The formatted output from the script
     * @param errors
     *            The formatted errors from the script
     */
    private void logOutputAndErrors(String output, String errors) {
        if (errors != null && errors.length() != 0) {
            log.error("Sign Certificate request script errors:\n{}", errors);
        }
        if (output != null && output.length() != 0) {
            log.debug("Sign Certificate request script output:\n{}", output);
        }
    }

    /**
     * This method is meant to enable testing, since Runtime cannot be mocked statically
     */
    protected Runtime getRuntime() {
        return Runtime.getRuntime();
    }
}
