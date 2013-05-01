package org.ovirt.engine.core.utils.ssh;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import javax.naming.AuthenticationException;
import javax.naming.TimeLimitExceededException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import org.apache.commons.lang.SystemUtils;

import org.ovirt.engine.core.utils.crypt.OpenSSHUtils;

/*
 * Test properties
 * $ mvn -Dssh-host=host1 -Dssh-test-port=22 -Dssh-test-user=root -Dssh-test-password=password -Dssh-test-p12=a.p12 -Dssh-test-p12-alias=alias -Dssh-test-p12-password=password
 */

public class SSHDialogTest {

    private static final int BUFFER_SIZE = 10 * 1024;

    private class Sink
    implements Runnable, SSHDialog.Sink {

        private SSHDialog.Control _control;
        private BufferedReader _incoming;
        private PrintWriter _outgoing;
        private List<String> _expect;
        private List<String> _send;
        private Throwable _throwable;
        private Thread _thread;

        public Sink(String expect[], String send[]) {
            _expect = new LinkedList<String>(Arrays.asList(expect));
            _send = new LinkedList<String>(Arrays.asList(send));
            _thread = new Thread(this);
        }

        public void exception() throws Throwable {
            if (_throwable != null) {
                throw _throwable;
            }
            assertTrue(_expect.size() == 0);
            assertTrue(_send.size() == 0);
        }

        @Override
        public void setControl(SSHDialog.Control control) {
            _control = control;
        }

        @Override
        public void setStreams(InputStream incoming, OutputStream outgoing) {
            _incoming = incoming == null ? null : new BufferedReader(
                new InputStreamReader(
                    incoming,
                    Charset.forName("UTF-8")
                ),
                BUFFER_SIZE
            );
            _outgoing = outgoing == null ? null : new PrintWriter(
                new OutputStreamWriter(
                    outgoing,
                    Charset.forName("UTF-8")
                ),
                true
            );
        }

        @Override
        public void start() {
            _thread.start();
        }

        @Override
        public void stop() {
            if (_thread != null) {
                _thread.interrupt();
                while(true) {
                    try {
                        _thread.join();
                        break;
                    }
                    catch (InterruptedException e) {}
                }
                _thread = null;
            }
        }

        public void run()  {
            try {
                while (_expect.size() > 0) {
                    assertEquals(_expect.remove(0), _incoming.readLine());
                    if (_send.size() > 0) {
                        String tosend = _send.remove(0);
                        if (tosend != null) {
                            for (String s : tosend.split("\n")) {
                                _outgoing.println(s);
                            }
                        }
                    }
                }
            }
            catch (Throwable t) {
                if (_throwable == null) {
                    _throwable = t;
                }
            }
            finally {
                _control.disconnect();
            }
        }
    }

    private static String s_host;
    private static String s_user;
    private static String s_password;
    private static KeyPair s_keyPair;
    private static int s_port;

    private static SSHD s_sshd;

    private SSHDialog _sshdialog;

    private static KeyPair _getKeyPair(String p12, String alias, String password) throws KeyStoreException {

        KeyStore.PrivateKeyEntry entry;
        InputStream in = null;
        try {
            in = new FileInputStream(p12);
            KeyStore ks = KeyStore.getInstance("PKCS12");
            ks.load(in, password.toCharArray());

            entry = (KeyStore.PrivateKeyEntry)ks.getEntry(
                alias,
                new KeyStore.PasswordProtection(
                    password.toCharArray()
                )
            );
        }
        catch (Exception e) {
            throw new KeyStoreException(
                String.format(
                    "Failed to get certificate entry from key store: %1$s/%2$s",
                    p12,
                    alias
                ),
                e
            );
        }
        finally {
            if (in != null) {
                try {
                    in.close();
                }
                catch(IOException e) {}
            }
        }

        if (entry == null) {
            throw new KeyStoreException(
                String.format(
                    "Bad key store: %1$s/%2$s",
                    p12,
                    alias
                )
            );
        }

        return new KeyPair(
            entry.getCertificate().getPublicKey(),
            entry.getPrivateKey()
        );
    }

    @BeforeClass
    public static void init() throws IOException {
        assumeTrue(SystemUtils.IS_OS_UNIX);

        s_host = System.getProperty("ssh-host");
        if (s_host == null) {
            s_host = "localhost";
            s_user = "root";
            s_password = "password";
            try {
                s_keyPair = KeyPairGenerator.getInstance("RSA").generateKeyPair();
            }
            catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            }

            s_sshd = new SSHD();
            s_sshd.setUser(
                s_user,
                s_password,
                s_keyPair.getPublic()
            );
            try {
                    s_sshd.start();
            }
            catch(IOException e) {
                    throw new RuntimeException(e);
            }
            s_port = s_sshd.getPort();
        }
        else {
            s_port = Integer.parseInt(System.getProperty("ssh-test-port", "22"));
            s_user = System.getProperty("ssh-test-user", "root");
            s_password = System.getProperty("ssh-test-password", "password");
            try {
                s_keyPair = _getKeyPair(
                    System.getProperty("ssh-test-p12", "src/test/resources/key.p12"),
                    System.getProperty("ssh-test-p12-alias", "1"),
                    System.getProperty("ssh-test-p12-password", "NoSoup4U")
                );
            }
            catch (KeyStoreException e) {
                throw new RuntimeException(e);
            }
        }

        System.out.println("Key fingerprint is: " + OpenSSHUtils.getKeyString(s_keyPair.getPublic(), "test"));
    }

    @AfterClass
    public static void terminate() throws Exception {
        if (s_sshd != null) {
            s_sshd.stop();
        }
    }

    @Before
    public void setUp() {
        _sshdialog = new SSHDialog();
        _sshdialog.setHost(s_host, s_port);
        _sshdialog.setPassword(s_password);
        _sshdialog.setKeyPair(s_keyPair);
        _sshdialog.setSoftTimeout(10*1000);
        _sshdialog.setHardTimeout(30*1000);
    }

    @After
    public void tearDown() {
        if (_sshdialog != null) {
            _sshdialog.disconnect();
            _sshdialog = null;
        }
    }

    @Test
    public void testKeyPair() throws Exception {
        _sshdialog.connect();
        _sshdialog.authenticate();
    }

    @Test
    public void testPassword() throws Exception {
        _sshdialog.setKeyPair(null);
        _sshdialog.connect();
        _sshdialog.authenticate();
    }

    @Test(expected=AuthenticationException.class)
    public void testWrongKeyPair() throws Exception {
        _sshdialog.setKeyPair(
            KeyPairGenerator.getInstance("RSA").generateKeyPair()
        );
        _sshdialog.connect();
        _sshdialog.authenticate();
    }

    @Test(expected=AuthenticationException.class)
    public void testWrongPassword() throws Exception {
        _sshdialog.setKeyPair(null);
        _sshdialog.setPassword("bad");
        _sshdialog.connect();
        _sshdialog.authenticate();
    }

    @Test
    public void testSimple() throws Throwable {
        Sink sink = new Sink(
            new String[] {
                "start",
                "text1",
                "text2"
            },
            new String[] {
                "text1",
                "text2"
            }
        );
        _sshdialog.connect();
        _sshdialog.authenticate();
        _sshdialog.executeCommand(
            sink,
            "cat",
            new InputStream[] {
                new ByteArrayInputStream("start\n".getBytes("UTF-8"))
            }
        );
        sink.exception();
    }

    @Test(expected=TimeLimitExceededException.class)
    public void testTimeout() throws Throwable {
        Sink sink = new Sink(
            new String[] {
                "start"
            },
            new String[] {
            }
        );
        _sshdialog.setSoftTimeout(1*1000);
        _sshdialog.connect();
        _sshdialog.authenticate();
        _sshdialog.executeCommand(
            sink,
            "cat",
            null
        );
        sink.exception();
    }

    @Test(expected=IOException.class)
    public void testStderr() throws Throwable {
        Sink sink = new Sink(
            new String[] {
                "start",
                "text1",
                "text2"
            },
            new String[] {
                "text1",
                "text2"
            }
        );
        _sshdialog.connect();
        _sshdialog.authenticate();
        _sshdialog.executeCommand(
            sink,
            "echo message >&2 && cat",
            new InputStream[] {
                new ByteArrayInputStream("start\n".getBytes("UTF-8"))
            }
        );
        sink.exception();
    }

    @Test
    public void testLong() throws Throwable {
        final String LINE = "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAASDSSSSSSSSSSSSSSSSSSSSSSSDDDDDD";
        final int NUM = 10000;
        final int FACTOR = 5;

        StringBuilder longText = new StringBuilder();
        for (int i=0;i<NUM/FACTOR;i++) {
            longText.append(LINE).append("\n");
        }

        List<String> expect = new LinkedList<String>();
        expect.add("start");
        for (int i=0;i<NUM;i++) {
            expect.add(LINE);
        }

        List<String> send = new LinkedList<String>();
        for (int i=0;i<NUM;i++) {
            if (i % (NUM/FACTOR) == 0) {
                send.add(longText.toString());
            }
            else {
                send.add(null);
            }
        }

        Sink sink = new Sink(
            expect.toArray(new String[0]),
            send.toArray(new String[0])
        );
        _sshdialog.connect();
        _sshdialog.authenticate();
        _sshdialog.executeCommand(
            sink,
            "echo start && sleep 4 && cat",
            null
        );
        sink.exception();
    }
}
