package org.ovirt.engine.core.utils.hostinstall;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.nio.channels.FileChannel;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Arrays;
import java.util.concurrent.TimeoutException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import javax.naming.TimeLimitExceededException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.sshd.ClientChannel;
import org.apache.sshd.ClientSession;
import org.apache.sshd.SshClient;
import org.apache.sshd.client.future.AuthFuture;
import org.apache.sshd.client.future.ConnectFuture;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.compat.backendcompat.Path;

/**
 * This Class implements file upload/download and command execution over SSH. Currently the code is very ugly (!!!)
 * since it implements file transfer using exec channel and large file upload using reverse SSH. The reason for this is
 * mina's lack of SFTP/SCP functionality. Once Mina completes the SFTP (or SCP) functionality, this class should be
 * fixed to use mina's built-in file transfer functionality. In this case the current UploadFile should be deleted (with
 * uploadLargeFile), and leave only uploadSmallfile with proper SFTP implementation, which should be renamed to
 * UploadFile.
 */

public class MinaInstallWrapper implements IVdsInstallWrapper {

    private static Log log = LogFactory.getLog(MinaInstallWrapper.class);

    private IVdsInstallCallBack callback;
    private SshClient client = null;
    private ClientSession session = null;
    private HostKeyVerifier hkvVerifier = null;
    private String host = null;
    private int maxSSHTimeout = 600000; // value is in milisec's.
    private int nSSHPort = 22;

    private final static int ONE_SECOND_MILI = 1000;
    private final static int DEFAULT_BUF_SIZE = 1024;
    private final static int LARGE_BUF_SIZE = 32768;
    private final static long ONE_MB = 1048576l;
    private final static long TWENTY_MBI = 20971520l;
    private final static long MAX_FILE_COUNT = 65536;

    public MinaInstallWrapper() {
        this(null);
    }

    protected MinaInstallWrapper(final SshClient sshClient) {
        try {
            if(sshClient == null) {
                this.client = SshClient.setUpDefaultClient();
            } else {
                client = sshClient;
            }

            client.start();
        } catch (Throwable t) {
            log.error("Unable to create SSH client. Please check mina jars: ", t);
        }
        maxSSHTimeout = ONE_SECOND_MILI * Config.<Integer> GetValue(ConfigValues.SSHInactivityTimoutSeconds);
        hkvVerifier = new HostKeyVerifier();
        client.setServerKeyVerifier(hkvVerifier);
    }

    /***
     * We use finalize in order to properly close the SSH client. This is important in order to avoid leaks
     * (memory/threads).
     */
    protected void finalize() throws Throwable {
        try {
            wrapperShutdown();
        } finally {
            super.finalize();
        }
    }

    /*
     * Start org.ovirt.engine.core.utils.hostinstall.IVdsInstallWrapper implementation
     */

    public final void InitCallback(IVdsInstallCallBack callback) {
        this.callback = callback;
    }

    public final boolean ConnectToServer(String server) {
        return ConnectToServer(server, Config.resolveKeyStorePath(),
                Config.<String> GetValue(ConfigValues.keystorePass));
    }

    public final boolean ConnectToServer(String server, String rootPassword) {
        Credentials creds = prepareCredentials(rootPassword);
        return _do_connect(server, creds);
    }

    public final boolean ConnectToServer(String server, String rootPassword, long timeout) {
        Credentials creds = prepareCredentials(rootPassword);
        return _do_connect(server, creds, timeout);
    }

    private Credentials prepareCredentials(String rootPassword) {
        Credentials creds = new Credentials();
        creds.setPassword(rootPassword);
        creds.setUsername("root");
        return creds;
    }

    public final boolean ConnectToServer(String server, String certPath, String password) {
        Credentials creds = new Credentials();
        creds.setPassphrase(password);
        creds.setCertPath(certPath);
        creds.setUsername("root");
        return _do_connect(server, creds);
    }

    /***
     * This method executes a given command in remote SSH shell. The method returns false if session was not closed
     * normally.
     *
     * Note: This method is using piped (in+out streams). This is possible only when producer & consumer are different
     * threads. If it happens to run on the same thread it may create deadlocks.
     */
    public final boolean RunSSHCommand(String command) {
        boolean fReturn = true;
        log.info(String.format("Invoking %s on %s", command, host));
        ClientChannel channel = null;
        PipedInputStream pisOut = null;
        PipedOutputStream out = null;
        PipedInputStream pisErr = null;
        PipedOutputStream err = null;

        try {
            channel = session.createExecChannel(command);
            pisOut = new PipedInputStream();
            out = new PipedOutputStream(pisOut);
            pisErr = new PipedInputStream();
            err = new PipedOutputStream(pisErr);
            ByteArrayInputStream in = new ByteArrayInputStream(new byte[0]);

            channel.setIn(in);
            channel.setOut(out);
            channel.setErr(err);

            log.debug("open");
            channel.open();
            log.debug("wait close");

            long timeout = System.currentTimeMillis();
            while (true) {
                int nStat = channel.waitFor(ClientChannel.CLOSED, ONE_SECOND_MILI);
                if (pisOut.available() > 0) {
                    String sshMessage = readLineFromInput(pisOut);
                    log.debug(sshMessage);
                    callbackAddMessage(sshMessage);
                }

                if (pisErr.available() > 0) {
                    String errorSshMessage = readInput(pisErr);
                    log.error(errorSshMessage);
                    callbackAddError(errorSshMessage);
                }

                if ((nStat & ClientChannel.CLOSED) == ClientChannel.CLOSED) {
                    log.debug("Got channel close: " + nStat);
                    break;
                } else if ((nStat & ClientChannel.EOF) == ClientChannel.EOF) {
                    log.debug("Got channel EOF: " + nStat);
                    fReturn = false;
                    break;
                }

                if (timeout + maxSSHTimeout <= System.currentTimeMillis()) {
                    String message = (
                            "The required action is taking longer than allowed by configuration." +
                                    " Verify host networking and storage settings."
                            );
                    log.error(message + " Current timeout is set to: " + maxSSHTimeout);
                    throw new TimeLimitExceededException(message);
                }
            }

            if (!fReturn && pisErr.available() > 0) {
                byte[] bytes = new byte[pisErr.available()];
                pisErr.read(bytes);
                String errorSshMessage = readInput(pisErr);
                log.error(errorSshMessage);
                callbackAddError(errorSshMessage);
            }
        } catch (Exception e) {
            callbackFailed(e.getMessage());
            log.error("Error running command " + command, e);
            fReturn = false;
        } finally {
            if (channel != null) {
                channel.close(true);
                channel = null;
            }
            try {
                if (pisOut != null) {
                    pisOut.close();
                    pisOut = null;
                }
                if (out != null) {
                    out.close();
                    out = null;
                }
                if (pisErr != null) {
                    pisErr.close();
                    pisErr = null;
                }
                if (err != null) {
                    err.close();
                    err = null;
                }
            } catch (IOException e) {
                log.debug("Caught pipe closing exception", e);
            }
        }
        log.info("RunSSHCommand returns " + fReturn);
        return fReturn;
    }

    /***
     * This implementation uses gzip at the remote end to create a binary stream we gunzip on local machine to the
     * desired destination. Note, this implementation should be replaced by mina SSHD's standard scp/sftp implementation
     * once available.
     *
     * @param remoteSource
     *            source file on the remote machine. This should be a valid path and file name.
     * @param localDestination
     *            destination file on the local machine. This should be a valid path and file name.
     */
    public final boolean DownloadFile(String remoteSource, String localDestination) {
        log.info(String.format("Downloading file %s from %s to %s", remoteSource, host, localDestination));

        boolean fReturn = true;
        ClientChannel channel = null;
        File tempFile = null;
        FileOutputStream fout = null;

        try {
            // 1. Get exec channel
            log.debug("create channel");
            channel = session.createExecChannel("gzip -1 -q -c " + remoteSource);

            ByteArrayInputStream in = new ByteArrayInputStream(new byte[0]);
            channel.setIn(in);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            channel.setOut(out);
            ByteArrayOutputStream err = new ByteArrayOutputStream();
            channel.setErr(err);

            // 2. Start transfer
            log.debug("open channel");
            channel.open();

            log.debug("wait close");
            long timeout = System.currentTimeMillis();
            while (true) {
                int nStat = channel.waitFor(ClientChannel.CLOSED, ONE_SECOND_MILI);
                String sshMessage = new String(out.toByteArray());
                if (sshMessage.length() > 0) {
                    log.debug(sshMessage);
                }

                String errorSshMessage = new String(err.toByteArray());
                if (errorSshMessage.length() > 0) {
                    log.error(errorSshMessage);
                    callbackAddError(errorSshMessage);
                }

                if ((nStat & ClientChannel.CLOSED) == ClientChannel.CLOSED) {
                    log.debug("Got channel close: " + nStat);
                    break;
                } else if ((nStat & ClientChannel.EOF) == ClientChannel.EOF) {
                    log.debug("Got channel EOF: " + nStat);
                    fReturn = false;
                    break;
                }

                if (timeout + maxSSHTimeout <= System.currentTimeMillis()) {
                    log.error("Transfer time exceeded internal SSH timeout: " + maxSSHTimeout);
                    throw new TimeLimitExceededException(
                            "Transfer time exceeded internal SSH timeout: "
                                    + maxSSHTimeout);
                }
            }

            // 3. Check incoming data length
            int nDataLength = out.toByteArray().length;
            log.debug("Incoming data length:" + nDataLength);
            if (nDataLength <= 0) {
                log.error("Got zero bytes. File empty or not found!");
                throw new FileNotFoundException("Downloaded zero bytes from " + remoteSource);
            }

            // 4. Write incoming data into a temp file
            tempFile = File.createTempFile("dlz.", null);
            FileOutputStream file = new FileOutputStream(tempFile);
            file.write(out.toByteArray());
            // Close the output stream
            file.close();

            // 5. Unzip temp file into local file.
            GZIPInputStream zipin = new GZIPInputStream(new FileInputStream(tempFile));
            byte[] buf = new byte[DEFAULT_BUF_SIZE];
            // Open the output file
            fout = new FileOutputStream(localDestination);
            MessageDigest digest = java.security.MessageDigest.getInstance("MD5");
            // Transfer bytes from the GZIP stream to the output file
            int len;
            while ((len = zipin.read(buf)) > 0) {
                fout.write(buf, 0, len);
                digest.update(buf, 0, len);
            }
            fout.close();
            zipin.close();
            if (!tempFile.delete()) {
                log.warn("Warn: unable to delete " + tempFile.getName());
            }
            tempFile = null;

            // 6. Get source hash and compare the results
            String remoteStringHash = getRemoteHash(remoteSource);
            if (remoteStringHash != null) {
                String localStringHash = byteArrayToHexString(digest.digest(), false);
                log.debug("Debug: Comparing local hash string " + localStringHash + " to remote hash string "
                        + remoteStringHash);
                fReturn = localStringHash.equalsIgnoreCase(remoteStringHash);
            } else {
                fReturn = false;
            }

            if (fReturn) {
                callbackEndTransfer();
            } else {
                callbackAddError("Exit Status from transfer: -1");
                log.error("err : " + new String(err.toByteArray()));
            }
        } catch (Throwable t) {
            callbackAddError(t.getMessage());
            log.error("Error downloading file: ", t);
        } finally {
            if (channel != null) {
                channel.close(true);
                channel = null;
            }
            if (tempFile != null) {
                if (!tempFile.delete()) {
                    log.warn("Warn: unable to delete " + tempFile.getName());
                }
                tempFile = null;
            }
            if (fout != null) {
                try {
                    fout.close();
                } catch (IOException e) {
                }
                fout = null;
            }
        }

        log.info("return " + fReturn);
        return fReturn;
    }

    /***
     * This implementation checks for file size, and uses the relevant implementation to upload the given file. Note:
     * this implementation should be replaced by mina- SSHD's standard scp/sftp implementation once available.
     *
     * @param source
     *            Source file on the local machine. This should be a valid path and file name.
     * @param destination
     *            Destination file on the remote machine. This should be a valid path and file name.
     */
    public final boolean UploadFile(String source, String destination) {
        log.info(String.format("Uploading file %s to %s on %s", source, destination, host));
        boolean fReturn = true;
        File file = new File(source);
        if (!file.exists()) {
            this.callbackFailed(String.format("Upload failed. File: %s not exist", source));
            log.error("File to upload does not exist: " + source);
            fReturn = false;
        } else {
            long lSize = file.length();
            if (lSize > ONE_MB) // Size is bigger than 1 MB
            {
                log.debug("Upload using uploadLargeFile");
                fReturn = uploadLargeFile(source, destination);
            } else {
                log.debug("Upload using uploadSmallFile");
                fReturn = uploadSmallFile(source, destination);
            }
        }
        return fReturn;
    }

    /*
     * End org.ovirt.engine.core.hostinstall.IVdsInstallWrapper implementation
     *
     *
     * Start supporting methods for MinaInstallWrapper implementation
     */

    /***
     * Establish SSH connection to the given host, using the given parameters. Connection authentication may be using
     * password or public key authentication, according to the given data in the Credentials object. If successful, a
     * session object will be maintained for this instance.
     *
     * @param server
     *            Host IP or name we wish to connect to.
     * @param creds
     *            Authentication data
     * @return boolean (success/failure).
     */
    private boolean _do_connect(String server, Credentials creds) {
        return _do_connect(server, creds, maxSSHTimeout);
    }

    private boolean _do_connect(String server, Credentials creds, long timeout) {
        boolean fReturn = true;
        host = server;
        FileInputStream fis = null;
        try {
            log.debug("_do_connect entry");
            ConnectFuture future = client.connect(host, nSSHPort);
            if(future.await(timeout)) {
                session = future.getSession();
            } else {
                throwTimeout();
            }

            if (creds.getCertPath() != null) {
                log.debug("Using Public Key Authentication.");
                char[] pass = creds.getPassphrase().toCharArray();
                KeyStore ks = KeyStore.getInstance("JKS");
                fis = new FileInputStream(creds.getCertPath());
                ks.load(fis, pass);
                fis.close();

                PrivateKey kPrivate = null;

                try {
                    kPrivate = ((KeyStore.PrivateKeyEntry) ks.getEntry(
                            Config.<String> GetValue(ConfigValues.CertAlias), new KeyStore.PasswordProtection(pass)))
                            .getPrivateKey();
                } catch (Exception e) {
                    String message = ("Failed to get certificate entry for alias: " + Config
                            .<String> GetValue(ConfigValues.CertAlias));
                    log.error(message, e);
                    throw new Exception(message);
                }
                Arrays.fill(pass, '\0');
                PublicKey kPublic = ks.getCertificate(Config.<String> GetValue(ConfigValues.CertAlias)).getPublicKey();
                KeyPair pair = new KeyPair(kPublic, kPrivate);

                AuthFuture authKeyFuture = session.authPublicKey(creds.getUsername(), pair);
                if(!authKeyFuture.await(timeout)) {
                    throwTimeout();
                }
                if (!authKeyFuture.isSuccess()) {
                    throw new Exception("Failed connecting to " + host
                            + " using Public-Key! Please verify that the host accepts public-key authentication");
                }
            } else {
                log.debug("Using password authentication.");
                AuthFuture authPasswordFuture = session.authPassword(creds.getUsername(), creds.getPassword());
                if(!authPasswordFuture.await(timeout)) {
                    throwTimeout();
                }
                if (!authPasswordFuture.isSuccess()) {
                    throw new Exception(
                            "Failed connecting to "
                                    + host
                                    + " using given password! Please verify your password is correct and that the host accepts password-based authentication");
                }
            }
            callbackConnected();
        } catch (Exception e) {
            callbackFailed(e.getMessage());
            fReturn = false;
            log.error("Could not connect to server " + host + ": " + e.getMessage());
            log.debug("Error details for connection error in " + host + ":", e);
            if (fis != null) {
                try {
                    fis.close();
                } catch (Exception ee) {
                }
                fis = null;
            }
        }
        return fReturn;
    }

    /***
     * Upload a large file (such as ISO) to a remote host, using the given parameters. This implementation Copies the
     * given source file to a public accessible local folder, then connects to the host and runs wget to local machine
     * is order to fetch the file. Note: this implementation should be replaced by mina SSHD's standard scp/sftp
     * implementation once available.
     *
     * @param localSource
     *            Local large file to be uploaded. This should be a valid path and file name.
     * @param remoteDest
     *            Destination file on remote machine. This should be a valid path and file name.
     * @return boolean (success/failure).
     */
    private boolean uploadLargeFile(String localSource, String remoteDest) {
        log.debug("uploadLargeFile entry");
        long lTime = System.currentTimeMillis();
        String strBaseFileName = new File(localSource).getName() + lTime + ".txt";
        boolean fReturn = true;
        ClientChannel channel = null;
        String localHost = null;
        String copyDest = null;

        // Fast copy the selected file to a public site, to be downloaded by the
        // RHEV-H machine.
        try {
            callbackAddMessage("Preparing ISO file");
            copyDest = Path.Combine(
                    Path.Combine(Path.Combine(System.getProperty("jboss.server.home.dir"), "deploy"), "ROOT.war"),
                    strBaseFileName);

            fastFileCopy(localSource, copyDest, false);
        } catch (Throwable t) {
            log.error("uploadLargeFile: Unable to copy local file: ", t);
            callbackFailed("Failed to get a hold on ISO file");
            fReturn = false;
        }

        // Get our IP as RHEV-H will see it (we may have more than one NIC).
        if (fReturn) {
            callbackAddMessage("Determining current address from host perspective");
            localHost = getLocalIP();
            log.debug("local IP: " + localHost);
            if (localHost == null) {
                log.error("uploadLargeFile: Unable to get local IP");
                callbackFailed("Failed to upload ISO file: remote RHEV-H unable to calculate oVirt IP address");
                fReturn = false;
            } else {
                localHost = localHost.trim().replace("\n", "");
            }
        }

        if (fReturn) {
            try {
                String cmd = String.format("wget http://%s:%s/%s -O %s; echo $?", localHost,
                        Config.<String> GetValue(ConfigValues.PublicURLPort), strBaseFileName, remoteDest);
                log.debug("uploadLargeFile executing: " + cmd);
                channel = session.createExecChannel(cmd);

                ByteArrayOutputStream out = new ByteArrayOutputStream();
                ByteArrayOutputStream err = new ByteArrayOutputStream();
                ByteArrayInputStream in = new ByteArrayInputStream(new byte[0]);
                channel.setIn(in);
                channel.setOut(out);
                channel.setErr(err);

                callbackAddMessage("Starting ISO upload");
                log.debug("open");
                channel.open();
                log.debug("wait close");
                if (!waitWithTimeout(channel, 2 * maxSSHTimeout)) {
                    throw new TimeLimitExceededException("Timeout waiting for ISO upload results!");
                }

                // Check output
                String strOut = new String(out.toByteArray());
                log.debug("stdout: " + strOut);

                int nResult = Integer.parseInt(strOut.trim());
                fReturn = (nResult == 0);
                if (fReturn) {
                    callbackAddMessage("ISO upload ended successfully");
                    callbackEndTransfer();
                } else {
                    callbackFailed("Failed to upload ISO file. Return status: " + nResult);
                }
            } catch (Exception e) {
                callbackFailed("Error uploading iso file " + e.getMessage());
                log.error("Error uploading iso file " + localSource, e);
                fReturn = false;
            } finally {
                if (channel != null) {
                    channel.close(true);
                    channel = null;
                }
                File fileCopyDest = new File(copyDest);
                if (fileCopyDest.exists()) {
                    // Delete temp ISO file from public area.
                    fileCopyDest.delete();
                }
                fileCopyDest = null;
            }
        }

        log.info("uploadLargeFile returns " + fReturn);
        return fReturn;
    }

    /***
     * This implementation uses zip stream on local machine to create a binary stream we gunzip on remote machine to the
     * desired destination. Note, this implementation should be replaced by mina SSHD's standard scp/sftp implementation
     * once available.
     *
     * @param source
     *            Source file on the local machine. This should be a valid path and file name.
     * @param destination
     *            Destination file on the remote machine. This should be a valid path and file name.
     */
    private boolean uploadSmallFile(String source, String destination) {
        log.info(String.format("Uploading file %s to %s on %s", source, destination, host));
        File file = new File(source);
        boolean fReturn = true;
        // Create the GZIP output stream
        ByteArrayOutputStream baosGZipped = null;
        GZIPOutputStream gzOut = null;
        byte[] hash = null;
        ClientChannel channel = null;
        ByteArrayInputStream in = null;

        if (file.exists()) {
            try {
                // Create the GZIP output stream
                baosGZipped = new ByteArrayOutputStream();
                gzOut = new GZIPOutputStream(baosGZipped);

                MessageDigest digest = java.security.MessageDigest.getInstance("MD5");

                // Open the input file
                FileInputStream src = new FileInputStream(source);
                // Transfer bytes from the input file to the GZIP output stream
                byte[] buf = new byte[DEFAULT_BUF_SIZE];
                int len;
                while ((len = src.read(buf)) > 0) {
                    gzOut.write(buf, 0, len);
                    digest.update(buf, 0, len);
                }
                src.close();

                hash = digest.digest();

                // Complete the GZIP file
                gzOut.finish();
                gzOut.flush();
                gzOut.close();
                gzOut = null;
            } catch (Throwable t) {
                callbackAddError(t.getMessage());
                log.error(t);
                fReturn = false;
            } finally {
                if (gzOut != null) {
                    try {
                        gzOut.close();
                    } catch (IOException e) {
                    }
                    gzOut = null;
                }
            }
        } else {
            this.callbackFailed(String.format("Upload failed. File: %s not exist", source));
            log.error("File to upload does not exist: " + source);
            fReturn = false;
        }

        if (fReturn) {
            try {
                log.debug("create channel");
                channel = session.createExecChannel("gunzip -c >" + destination);

                in = new ByteArrayInputStream(baosGZipped.toByteArray());
                channel.setIn(in);
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                channel.setOut(out);
                ByteArrayOutputStream err = new ByteArrayOutputStream();
                channel.setErr(err);

                log.debug("open");
                channel.open();

                log.debug("wait close");

                int nBytesLeft = in.available();
                log.debug("Bytes left2: " + nBytesLeft);
                int nMaxTimeoutSec = Config.<Integer> GetValue(ConfigValues.SSHInactivityTimoutSeconds);
                for (int i = 0; nBytesLeft > 0 && i < nMaxTimeoutSec; i++) {
                    channel.waitFor(ClientChannel.CLOSED, ONE_SECOND_MILI);
                    nBytesLeft = in.available();
                    log.debug("Bytes left: " + nBytesLeft);
                }
                in.close();
                in = null;

                log.debug("++++debug: waiting for channel to close");
                if (!waitWithTimeout(channel, nMaxTimeoutSec * ONE_SECOND_MILI)) {
                    throw new TimeLimitExceededException("Failed to close channel in less than " + nMaxTimeoutSec
                            + " seconds");
                }
                log.debug("++++debug: channel closed");

                channel = null;
                if (nBytesLeft > 0) {
                    throw new TimeLimitExceededException("Failed to transfer all data. There are " + nBytesLeft
                            + " bytes left.");
                }

                String localStringHash = null;
                String remoteStringHash = getRemoteHash(destination);
                if (remoteStringHash != null) {
                    localStringHash = byteArrayToHexString(hash, false);
                    log.debug("Debug: Comparing local hash string " + localStringHash + " to remote hash string "
                            + remoteStringHash);
                    fReturn = localStringHash.equalsIgnoreCase(remoteStringHash);
                } else {
                    fReturn = false;
                }

                if (fReturn) {
                    callbackEndTransfer();
                } else {
                    callbackAddError("Failed to upload file: bad hash.");
                    log.error("err : " + new String(err.toByteArray()));
                }
            } catch (Exception e) {
                callbackAddError(e.getMessage());
                log.error(e);
                fReturn = false;
            } finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e) {
                    }
                    in = null;
                }
                if (channel != null) {
                    channel.close(false);
                    channel = null;
                }
            }
        } // if gzipping file went fine

        log.info("return " + fReturn);
        return fReturn;
    }

    protected void callbackAddError(String message) {
        if (haveCallback()) {
            callback.AddError(message);
        }
    }

    protected void callbackAddMessage(String message) {
        if (haveCallback()) {
            callback.AddMessage(message);
        }
    }

    protected void callbackConnected() {
        byte[] fp = hkvVerifier.getServerFingerprint();
        String strFingerprint = "Unknown";
        if (fp != null) {
            strFingerprint = byteArrayToHexString(fp, true);
        } else {
            log.error("Unable to get host fingerprint!");
        }
        if (haveCallback()) {
            callback.AddMessage(
                    String.format(
                            "<BSTRAP component='RHEV_INSTALL' status='OK' message='Connected to Host %s with SSH key fingerprint: %s'/>",
                            host,
                            strFingerprint
                            )
                    );
            callback.Connected();
        }
    }

    protected void callbackEndTransfer() {
        if (haveCallback()) {
            callback.EndTransfer();
        }
    }

    protected void callbackFailed(String message) {
        if (haveCallback()) {
            callback.Failed(message);
        }
    }

    protected boolean haveCallback() {
        return callback != null;
    }

    /***
     * A utility method to wait for channel to close.
     * @param channel
     *            A reference to client channel.
     * @param timeoutInMilliSeconds
     *            Timeout period in milliseconds to wait for.
     * @return . * True- if got channel CLOSED within timeout period. . * False- Any other exit reason.
     */
    public boolean waitWithTimeout(ClientChannel channel, int timeoutInMilliSeconds) {
        boolean fReturn = true;
        boolean fQuit = false;
        int nStat = 0;
        int timeoutInSeconds = timeoutInMilliSeconds / ONE_SECOND_MILI;
        for (int i = 0; fQuit == false && i < timeoutInSeconds; i++) {
            nStat = channel.waitFor(ClientChannel.CLOSED, ONE_SECOND_MILI);
            if ((nStat & ClientChannel.CLOSED) == ClientChannel.CLOSED) {
                log.debug("Got channel CLOSED: " + nStat);
                fQuit = true;
            } else if ((nStat & ClientChannel.EOF) == ClientChannel.EOF) {
                log.debug("Got channel EOF: " + nStat);
                fReturn = false;
                fQuit = true;
            }
            log.debug("Waiting for: " + i + " seconds.");
        }
        if ((nStat & ClientChannel.CLOSED) != ClientChannel.CLOSED) {
            log.debug("Quit waiting for close. Stat is:" + nStat);
            fReturn = false;
        }
        return fReturn;
    }

    /***
     * A utility method to convert bytes into a printable string.
     *
     * @param b
     *            An array of bytes to be printed
     * @param fFormat
     *            Should we use pretty format (add [] for each byte).
     * @return A printable string.
     */
    public static String byteArrayToHexString(byte[] b, boolean fFormat) {
        StringBuffer sb = new StringBuffer(b.length * 2);
        for (int i = 0; i < b.length; i++) {
            int v = b[i] & 0xff;
            if (v < 16) {
                sb.append('0');
            }
            sb.append(Integer.toHexString(v));
            if (fFormat && (i < b.length - 1)) {
                sb.append(':');
            }
        }
        log.debug("Converted bytes " + Arrays.toString(b) + "to hexString " + sb.toString().toLowerCase());
        return sb.toString().toLowerCase();
    }

    /**
     * Calculate md5sum hash for a given file in a remote machine.
     *
     * @param strFileName
     *            : a valid path and file name in the remote machine.
     * @return: boolean (success/failure)
     * @throws Exception
     */
    private String getRemoteHash(String strFileName) {
        String strReturn = null;
        ClientChannel channel = null;
        log.debug("getRemoteHash entry for " + strFileName);
        try {
            channel =
                    session.createExecChannel("echo `md5sum " + strFileName + " ; ls -l " + strFileName
                            + " ; echo 'lsof:' && lsof | grep " + strFileName + " ; echo 'cat:' && cat " + strFileName
                            + "`");
            ByteArrayInputStream in = new ByteArrayInputStream(new byte[0]);
            channel.setIn(in);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            channel.setOut(out);
            ByteArrayOutputStream err = new ByteArrayOutputStream();
            channel.setErr(err);
            channel.open();
            if (!waitWithTimeout(channel, maxSSHTimeout)) {
                throw new TimeLimitExceededException("Timeout waiting for RemoteHash results!");
            }
            log.debug("+++++++++++++++++++++debug getRemoteHash: The ByteArrayOutputStream is " + out);

            String strOut = new String(out.toByteArray());
            log.debug("debug getRemoteHash: The ByteArrayOutputStream.toByteArray() is " + strOut);

            String strOutNew = out.toString();
            log.debug("debug getRemoteHash: The ByteArrayOutputStream.toString() is " + strOutNew);

            strReturn = strOutNew.split(" ")[0];
        } catch (Exception e) {
            log.error("Error checking remote hash: ", e);
        } finally {
            if (channel != null) {
                channel.close(true);
                channel = null;
            }
        }
        log.debug("getRemoteHash return: " + strReturn);
        return strReturn;
    }

    /***
     * Consume data from input stream and convert is into a String
     *
     * @param input
     *            An input stream.
     * @return A string object representing the contents of the given stream.
     */
    protected String readInput(InputStream input) {
        StringBuilder builder = new StringBuilder();
        byte[] tmp = new byte[DEFAULT_BUF_SIZE];
        try {
            while (input.available() > 0) {
                int i = input.read(tmp, 0, DEFAULT_BUF_SIZE);
                if (i < 0)
                    break;
                builder.append(new String(tmp, 0, i));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return builder.toString();
    }

    /***
     * Consume String lines from input stream
     *
     * @param input
     *            An input stream
     * @return A string object representing the contents of the given stream split into lines.
     */
    protected String readLineFromInput(InputStream input) {
        InputStreamReader isrReader = new InputStreamReader(input);
        BufferedReader in = new BufferedReader(isrReader);

        StringBuilder builder = new StringBuilder();
        try {
            while (in.ready()) {
                builder.append(in.readLine() + '\n');
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return builder.toString();
    }

    /**
     * Optimized copy source file to destination, based on file size. If destination is a path then source file name is
     * appended. If destination file exists then: overwrite=true, destination file is replaced; overwrite=false,
     * exception is thrown.
     *
     * @param src
     *            source file
     * @param dst
     *            destination file or path
     * @param overwrite
     *            overwrite destination file
     * @exception IOException
     *                I/O problem
     * @exception IllegalArgumentException
     *                illegal argument
     */
    public void fastFileCopy(final String srcFile, String dstFile, final boolean overwrite) throws IOException,
            IllegalArgumentException {
        File src = new File(srcFile);
        File dst = new File(dstFile);

        // checks
        if (!src.isFile() || !src.exists()) {
            throw new IllegalArgumentException("Source file '" + src.getAbsolutePath() + "' not found!");
        }

        if (dst.exists()) {
            if (dst.isDirectory()) // Directory? -> use source file name
            {
                dst = new File(dst, src.getName());
            } else if (dst.isFile()) {
                if (!overwrite) {
                    throw new IllegalArgumentException("Destination file '" + dst.getAbsolutePath()
                            + "' already exists!");
                }
            } else {
                throw new IllegalArgumentException("Invalid destination object '" + dst.getAbsolutePath() + "'!");
            }
        }

        File dstParent = dst.getParentFile();
        if (!dstParent.exists()) {
            if (!dstParent.mkdirs()) {
                throw new IOException("Failed to create directory " + dstParent.getAbsolutePath());
            }
        }

        long fileSize = src.length();
        if (fileSize > TWENTY_MBI) // for larger files (20Mb) use streams
        {
            FileInputStream in = new FileInputStream(src);
            FileOutputStream out = new FileOutputStream(dst);

            try {
                int doneCnt = -1, bufSize = LARGE_BUF_SIZE;
                byte buf[] = new byte[LARGE_BUF_SIZE];
                while ((doneCnt = in.read(buf, 0, bufSize)) >= 0) {
                    if (doneCnt == 0) {
                        Thread.yield();
                    } else {
                        out.write(buf, 0, doneCnt);
                    }
                }
                out.flush();
            } finally {
                try {
                    in.close();
                } catch (IOException e) {
                }

                try {
                    out.close();
                } catch (IOException e) {
                }
            }
        } else // smaller files, use channels
        {
            FileInputStream fis = new FileInputStream(src);
            FileOutputStream fos = new FileOutputStream(dst);
            FileChannel in = fis.getChannel(), out = fos.getChannel();

            try {
                long offs = 0;
                long doneCnt = 0;
                long copyCnt = Math.min(MAX_FILE_COUNT, fileSize);
                do {
                    doneCnt = in.transferTo(offs, copyCnt, out);
                    offs += doneCnt;
                    fileSize -= doneCnt;
                } while (fileSize > 0);
            } finally // cleanup
            {
                try {
                    in.close();
                } catch (IOException e) {
                }

                try {
                    out.close();
                } catch (IOException e) {
                }

                try {
                    fis.close();
                } catch (IOException e) {
                }

                try {
                    fos.close();
                } catch (IOException e) {
                }
            }

        } // else smaller files, use channels
    }

    /***
     * A method to extract local machine's IP from remote host. This is needed since local machine may have several
     * NIC's and several addresses. So we should be able to tell which address the remote machine sees.
     *
     * @return Extracted address or null if failed to extract.
     */
    private String getLocalIP() {
        log.info("getLocalIP entry");
        String strOut = null;

        try {
            ClientChannel channel = session.createExecChannel("echo $SSH_CLIENT | sed 's/ .*$//g'");

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ByteArrayOutputStream err = new ByteArrayOutputStream();
            ByteArrayInputStream in = new ByteArrayInputStream(new byte[0]);
            channel.setIn(in);
            channel.setOut(out);
            channel.setErr(err);

            log.debug("open");
            channel.open();
            log.debug("wait close");
            if (!waitWithTimeout(channel, maxSSHTimeout)) {
                throw new TimeLimitExceededException("Timeout waiting for IP results!");
            }

            strOut = new String(out.toByteArray());
        } catch (Exception e) {
            log.error("getLocalIP: Unable to get IP: ", e);
        }
        log.info("getLocalIP return: " + strOut);
        return strOut;
    }

    @Override
    public void wrapperShutdown() {
        if (session != null) {
            session.close(true);
            session = null;
        }
        if (client != null) {
            client.stop();
            client = null;
        }
    }

    public int getPort() {
        return this.nSSHPort;
    }

    public void setPort(int port) {
        this.nSSHPort = port;
    }

    private void throwTimeout() throws TimeoutException {
        throw new TimeoutException("SSH connection timed out connecting to " + host);
    }
}
