package org.ovirt.engine.core.utils.hostinstall;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeNotNull;
import static org.junit.Assume.assumeTrue;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.KeyStore;

import org.apache.commons.lang.SystemUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.config.IConfigUtilsInterface;
import org.ovirt.engine.core.engineencryptutils.OpenSSHUtils;
import org.ovirt.engine.core.utils.archivers.tar.Tar;
import org.ovirt.engine.core.utils.ssh.SSHD;

/*
 * Test properties
 * $ mvn -Dssh-host=host1 -Dssh-test-port=22 -Dssh-test-user=root -Dssh-test-password=password -Dssh-test-p12=a.p12 -Dssh-test-p12-password=password
 *
 * Default SSH public key is:
 * ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAAAgQCF7Rhlve8ikOono3zHN2kkyCqauNSdX9w6lwq3uLNFi7ryyENSpCsQADjCO5EzABUxU+0RHh6OG6TRFCRbI57NN77isfKyLqjsVOkhPB4D86GhmefmnYKPSAA2JxVB9s0BIA8jAgrEy4QFjmxt1EHAi2UAG3PjCC+qANF7CnR47Q==
 *
 * TODO
 *
 * In future the installer should accept PublicKey and not keystore.
 */

public class VdsInstallerSSHTest {
    class MyVdsInstallerCallback implements IVdsInstallerCallback {
        public String error;
        public String message;
        public String fail;
        public boolean connected;
        public boolean endTransfer;

        public MyVdsInstallerCallback() {
            reset();
        }

        public void reset() {
            error = null;
            message = null;
            fail = null;
            connected = false;
            endTransfer = false;
        }

        @Override
        public void addError(String error) {
            if (this.error == null) {
                this.error = "";
            }
            this.error += error + "\n";
        }

        @Override
        public void addMessage(String message) {
            if (this.message == null) {
                this.message = "";
            }
            this.message += message + "\n";
        }

        @Override
        public void connected() {
            this.connected = true;
        }

        @Override
        public void endTransfer() {
            this.endTransfer = true;
        }

        @Override
        public void failed(String error) {
            this.fail = error;
        }
    }

    static String host;
    static String user;
    static String password;
    static String hostKstore;
    static String hostKstorePassword;
    int port;

    SSHD sshd;

    @Before
    public void setup() throws IOException {
        assumeTrue(SystemUtils.IS_OS_UNIX);
        IConfigUtilsInterface confInstance = new DefaultValuesConfigUtil();
        Config.setConfigUtils(confInstance);

        hostKstore = System.getProperty("ssh-test-p12", "src/test/resources/key.p12");
        hostKstorePassword = System.getProperty("ssh-test-p12-password", "NoSoup4U");

        host = System.getProperty("ssh-host");

        if (host == null) {
            host = "localhost";
            user = "root";
            password = "password";

            sshd = new SSHD();
            try {
                KeyStore ks = KeyStore.getInstance("PKCS12");
                ks.load(new FileInputStream(hostKstore), hostKstorePassword.toCharArray());
                sshd.setUser(
                    user,
                    password,
                    ks.getCertificate(
                        Config.<String>GetValue(
                            ConfigValues.CertAlias
                        )
                    ).getPublicKey()
                );
            }
            catch (Throwable e) {
                throw new RuntimeException(e);
            }
            try {
                    sshd.start();
            }
            catch(IOException e) {
                    throw new RuntimeException(e);
            }
            port = sshd.getPort();
        }
        else {
            port = Integer.parseInt(System.getProperty("ssh-test-port", "22"));
            user = System.getProperty("ssh-test-user", "root");
            password = System.getProperty("ssh-test-password", "password");
        }
    }

    @After
    public void tearDown() throws Exception {
        if (sshd != null) {
            sshd.stop();
        }
    }

    @Test
    public void testPassword() throws Exception {
        MyVdsInstallerCallback callbacks = new MyVdsInstallerCallback();
        VdsInstallerSSH vssh = new VdsInstallerSSH();
        vssh.setPort(port);
        vssh.setCallback(callbacks);

        try {
            assertTrue(vssh.connect(host, password));
            assertTrue(callbacks.connected);
            assertFalse(callbacks.endTransfer);
            assertNotNull(callbacks.message);
            assertNull(callbacks.error);
            assertNull(callbacks.fail);
        }
        finally {
            vssh.shutdown();
            vssh = null;
        }
    }

    @Test
    public void testPasswordFail() throws Exception {
        MyVdsInstallerCallback callbacks = new MyVdsInstallerCallback();
        VdsInstallerSSH vssh = new VdsInstallerSSH();
        vssh.setPort(port);
        vssh.setCallback(callbacks);

        try {
            assertFalse(vssh.connect(host, password+"A"));
            assertFalse(callbacks.connected);
            assertFalse(callbacks.endTransfer);
            assertNotNull(callbacks.message);
            assertNull(callbacks.error);
            assertNotNull(callbacks.fail);
        }
        finally {
            vssh.shutdown();
            vssh = null;
        }
    }

    @Test
    public void testPK() throws Exception {
        MyVdsInstallerCallback callbacks = new MyVdsInstallerCallback();
        VdsInstallerSSH vssh = new VdsInstallerSSH();
        vssh.setPort(port);
        vssh.setCallback(callbacks);

        try {
            assertTrue(vssh.connect(host, hostKstore, hostKstorePassword));
            assertTrue(callbacks.connected);
            assertFalse(callbacks.endTransfer);
            assertNotNull(callbacks.message);
            assertNull(callbacks.error);
            assertNull(callbacks.fail);
        }
        finally {
            vssh.shutdown();
            vssh = null;
        }
    }

    @Test
    public void testCommandOK() throws Exception {
        MyVdsInstallerCallback callbacks = new MyVdsInstallerCallback();
        VdsInstallerSSH vssh = new VdsInstallerSSH();
        vssh.setPort(port);
        vssh.setCallback(callbacks);

        try {
            assertTrue(vssh.connect(host, password));
            callbacks.reset();
            assertTrue(vssh.executeCommand("echo test1 && echo test2"));
            assertFalse(callbacks.connected);
            assertFalse(callbacks.endTransfer);
            assertEquals("test1\ntest2\n", callbacks.message);
            assertNull(callbacks.error);
            assertNull(callbacks.fail);
        }
        finally {
            vssh.shutdown();
            vssh = null;
        }
    }

    @Test
    public void testCommandOKStdin() throws Exception {
        MyVdsInstallerCallback callbacks = new MyVdsInstallerCallback();
        VdsInstallerSSH vssh = new VdsInstallerSSH();
        vssh.setPort(port);
        vssh.setCallback(callbacks);

        try {
            assertTrue(vssh.connect(host, password));
            callbacks.reset();
            assertTrue(vssh.executeCommand("cat", new ByteArrayInputStream("Ping".getBytes("UTF-8"))));
            assertFalse(callbacks.connected);
            assertFalse(callbacks.endTransfer);
            assertEquals("Ping\n", callbacks.message);
            assertNull(callbacks.error);
            assertNull(callbacks.fail);
        }
        finally {
            vssh.shutdown();
            vssh = null;
        }
    }

    @Test
    public void testCommandOKTar() throws Exception {
        MyVdsInstallerCallback callbacks = new MyVdsInstallerCallback();
        VdsInstallerSSH vssh = new VdsInstallerSSH();
        vssh.setPort(port);
        vssh.setCallback(callbacks);

        File tmpTar = null;
        File tmpDir = null;

        try {
            tmpTar = File.createTempFile("test1", "tar");
            tmpDir = File.createTempFile("test1", "tmp");
            tmpDir.delete();
            tmpDir.mkdir();

            OutputStream osScript = null;
            OutputStream osTar = null;
            try {
                File script = new File(tmpDir, "script");
                osScript = new FileOutputStream(script);
                osScript.write("echo ok\n".getBytes("UTF-8"));
                osScript.close();
                osScript = null;
                script.setExecutable(true);

                osTar = new FileOutputStream(tmpTar);
                Tar.doTar(osTar, tmpDir);
            }
            finally {
                for (OutputStream os : new OutputStream[] {osScript, osTar}) {
                    if (os != null) {
                        try {
                            os.close();
                        }
                        catch(IOException e) {
                            // ignore
                        }
                    }
                }
            }

            InputStream is = null;
            try {
                is = new FileInputStream(tmpTar);
                assertTrue(vssh.connect(host, password));
                callbacks.reset();
                assertTrue(
                    vssh.executeCommand(
                        (
                            "MYTMP=$(mktemp); " +
                            "trap \"rm -fr ${MYTMP}\" 0; " +
                            "rm -fr ${MYTMP} && " +
                            "mkdir ${MYTMP} && " +
                            "tar -C ${MYTMP} -x && " +
                            "${MYTMP}/script"
                        ),
                        is
                    )
                );
                assertFalse(callbacks.connected);
                assertFalse(callbacks.endTransfer);
                assertEquals("ok\n", callbacks.message);
                assertNull(callbacks.error);
                assertNull(callbacks.fail);
            }
            finally {
                if (is != null) {
                    try {
                        is.close();
                    }
                    catch(Exception e) {
                        // ignore we want test exception
                    }
                }
            }
        }
        finally {
            if (tmpDir != null) {
                tmpDir.delete();
            }
            if (tmpTar != null) {
                tmpTar.delete();
            }
            vssh.shutdown();
            vssh = null;
        }
    }

    @Test
    public void testCommandNoneZero() throws Exception {
        MyVdsInstallerCallback callbacks = new MyVdsInstallerCallback();
        VdsInstallerSSH vssh = new VdsInstallerSSH();
        vssh.setPort(port);
        vssh.setCallback(callbacks);

        try {
            assertTrue(vssh.connect(host, password));
            callbacks.reset();
            assertFalse(vssh.executeCommand("echo test1 && echo test2 && false"));
            assertFalse(callbacks.connected);
            assertFalse(callbacks.endTransfer);
            assertEquals("test1\ntest2\n", callbacks.message);
            assertNull(callbacks.error);
            assertNotNull(callbacks.fail);
        }
        finally {
            vssh.shutdown();
            vssh = null;
        }
    }

    @Test
    public void testInvalidCommand() throws Exception {
        MyVdsInstallerCallback callbacks = new MyVdsInstallerCallback();
        VdsInstallerSSH vssh = new VdsInstallerSSH();
        vssh.setPort(port);
        vssh.setCallback(callbacks);

        try {
            assertTrue(vssh.connect(host, password));
            callbacks.reset();
            assertFalse(vssh.executeCommand("/xxxx"));
            assertFalse(callbacks.connected);
            assertFalse(callbacks.endTransfer);
            assertNull(callbacks.message);
            assertNotNull(callbacks.error);
            assertNotNull(callbacks.fail);
        }
        finally {
            vssh.shutdown();
            vssh = null;
        }
    }

    @Test
    public void testCommandStderr() throws Exception {
        MyVdsInstallerCallback callbacks = new MyVdsInstallerCallback();
        VdsInstallerSSH vssh = new VdsInstallerSSH();
        vssh.setPort(port);
        vssh.setCallback(callbacks);

        try {
            assertTrue(vssh.connect(host, password));
            callbacks.reset();
            assertFalse(vssh.executeCommand("echo test1 && echo test2 && echo test3 >&2"));
            assertFalse(callbacks.connected);
            assertFalse(callbacks.endTransfer);
            assertEquals("test1\ntest2\n", callbacks.message);
            assertEquals("test3\n\n", callbacks.error);
            assertNull(callbacks.fail);
        }
        finally {
            vssh.shutdown();
            vssh = null;
        }
    }

    @Test
    public void testCommandStderrAndNonZero() throws Exception {
        MyVdsInstallerCallback callbacks = new MyVdsInstallerCallback();
        VdsInstallerSSH vssh = new VdsInstallerSSH();
        vssh.setPort(port);
        vssh.setCallback(callbacks);

        try {
            assertTrue(vssh.connect(host, password));
            callbacks.reset();
            assertFalse(vssh.executeCommand("echo test1 && echo test2 && echo test3 >&2 && false"));
            assertFalse(callbacks.connected);
            assertFalse(callbacks.endTransfer);
            assertEquals("test1\ntest2\n", callbacks.message);
            assertEquals("test3\n\n", callbacks.error);
            assertNotNull(callbacks.fail);
        }
        finally {
            vssh.shutdown();
            vssh = null;
        }
    }

    @Test
    public void testSendReceive() throws Exception {
        File local1 = File.createTempFile("ssh-test-", ".tmp");
        OutputStream os = new FileOutputStream(local1);
        os.write("hello world!".getBytes());
        os.close();
        String remote = String.format("/tmp/ssh-test-%1$s.tmp", System.currentTimeMillis());
        File local2 = File.createTempFile("ssh-test-", ".tmp");

        MyVdsInstallerCallback callbacks = new MyVdsInstallerCallback();
        VdsInstallerSSH vssh = new VdsInstallerSSH();
        vssh.setPort(port);
        vssh.setCallback(callbacks);

        try {
            assertTrue(vssh.connect(host, password));

            callbacks.reset();
            assertTrue(vssh.sendFile(local1.getAbsolutePath(), remote));
            assertFalse(callbacks.connected);
            assertTrue(callbacks.endTransfer);
            assertNull(callbacks.message);
            assertNull(callbacks.error);
            assertNull(callbacks.fail);

            callbacks.reset();
            assertTrue(vssh.receiveFile(remote, local2.getAbsolutePath()));
            assertFalse(callbacks.connected);
            assertTrue(callbacks.endTransfer);
            assertNull(callbacks.message);
            assertNull(callbacks.error);
            assertNull(callbacks.fail);

            callbacks.reset();
            assertFalse(vssh.sendFile(local1.getAbsolutePath(), "/none/exist/path/file"));
            assertFalse(callbacks.connected);
            assertFalse(callbacks.endTransfer);
            assertNull(callbacks.message);
            assertNotNull(callbacks.error);
            assertNull(callbacks.fail);

            callbacks.reset();
            assertFalse(vssh.sendFile(local1.getAbsolutePath()+"A", "/none/exist/path/file"));
            assertFalse(callbacks.connected);
            assertFalse(callbacks.endTransfer);
            assertNull(callbacks.message);
            assertNull(callbacks.error);
            assertNotNull(callbacks.fail);

            callbacks.reset();
            assertFalse(vssh.receiveFile("/none/exist/path/file", local2.getAbsolutePath()));
            assertFalse(callbacks.connected);
            assertFalse(callbacks.endTransfer);
            assertNull(callbacks.message);
            assertNotNull(callbacks.error);
            assertNull(callbacks.fail);

            callbacks.reset();
            assertFalse(vssh.receiveFile(remote, "/none/exist/path/file"));
            assertFalse(callbacks.connected);
            assertFalse(callbacks.endTransfer);
            assertNull(callbacks.message);
            assertNull(callbacks.error);
            assertNotNull(callbacks.fail);
        }
        finally {
            local1.delete();
            local2.delete();
            vssh.executeCommand(String.format("rm -f '%1$s'", remote));

            vssh.shutdown();
            vssh = null;
        }
    }

    @Test
    public void testFingerprint() throws Exception {
        assumeNotNull(sshd);
        VdsInstallerSSH vdsi = new VdsInstallerSSH();
        vdsi.setPort(port);
        try {
            assertEquals(
                OpenSSHUtils.getKeyFingerprintString(sshd.getKey()),
                vdsi.getServerKeyFingerprint(host, 5000)
            );
        }
        finally {
            vdsi.shutdown();
            vdsi = null;
        }
    }
}
