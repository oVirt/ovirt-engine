package org.ovirt.engine.core.utils.hostinstall;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.KeyStore;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.config.IConfigUtilsInterface;
import org.ovirt.engine.core.utils.ssh.SSHD;

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

    static String user = "root";
    static String password = "password";
    static String hostKstore = "src/test/resources/.hostKstore";
    static String hostKstorePassword = "NoSoup4U";
    static String host = "localhost";
    int port;

    SSHD sshd;

    @Before
    public void setup() throws IOException {
        IConfigUtilsInterface confInstance = new DefaultValuesConfigUtil();
        Config.setConfigUtils(confInstance);

        sshd = new SSHD();
        try {
            KeyStore ks = KeyStore.getInstance("JKS");
            ks.load(new FileInputStream(hostKstore), /*hostKstorePassword.toCharArray()*/null);
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
            assertFalse(vssh.executeCommand("xxxx"));
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
}
