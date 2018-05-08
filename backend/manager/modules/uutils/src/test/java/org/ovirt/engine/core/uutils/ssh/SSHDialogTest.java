package org.ovirt.engine.core.uutils.ssh;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
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

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;

/*
 * Test properties
 * $ mvn -Dssh-host=host1 -Dssh-test-port=22 -Dssh-test-user=root -Dssh-test-password=password -Dssh-test-p12=a.p12 -Dssh-test-p12-alias=alias -Dssh-test-p12-password=password
 */
@DisabledOnOs({OS.WINDOWS, OS.OTHER})
@Tag("ssh")
public class SSHDialogTest {

    private static final int BUFFER_SIZE = 10 * 1024;

    private static class Sink implements Runnable, SSHDialog.Sink {

        private SSHDialog.Control control;
        private BufferedReader incoming;
        private PrintWriter outgoing;
        private List<String> expect;
        private List<String> send;
        private Throwable throwable;
        private Thread thread;

        public Sink(String[] expect, String[] send) {
            this.expect = new LinkedList<>(Arrays.asList(expect));
            this.send = new LinkedList<>(Arrays.asList(send));
            thread = new Thread(this);
        }

        public void exception() throws Throwable {
            if (throwable != null) {
                throw throwable;
            }
            assertEquals(0, expect.size());
            assertEquals(0, send.size());
        }

        @Override
        public void setControl(SSHDialog.Control control) {
            this.control = control;
        }

        @Override
        public void setStreams(InputStream incoming, OutputStream outgoing) {
            this.incoming = incoming == null ? null : new BufferedReader(
                    new InputStreamReader(
                            incoming,
                            StandardCharsets.UTF_8),
                    BUFFER_SIZE);
            this.outgoing = outgoing == null ? null : new PrintWriter(
                    new OutputStreamWriter(
                            outgoing,
                            StandardCharsets.UTF_8),
                    true);
        }

        @Override
        public void start() {
            thread.start();
        }

        @Override
        public void stop() {
            if (thread != null) {
                thread.interrupt();
                while (true) {
                    try {
                        thread.join();
                        break;
                    } catch (InterruptedException ignore) {
                    }
                }
                thread = null;
            }
        }

        public void run() {
            try {
                while (expect.size() > 0) {
                    assertEquals(expect.remove(0), incoming.readLine());
                    if (send.size() > 0) {
                        String tosend = send.remove(0);
                        if (tosend != null) {
                            for (String s : tosend.split("\n")) {
                                outgoing.println(s);
                            }
                        }
                    }
                }
            } catch (Throwable t) {
                if (throwable == null) {
                    throwable = t;
                }
            } finally {
                try {
                    control.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    private static String sshHost;
    private static String sshUser;
    private static String sshPassword;
    private static KeyPair sshKeyPair;
    private static int sshPort;

    private static SSHD sshd;

    private SSHDialog sshDialog;

    private static KeyPair getKeyPair(String p12, String alias, String password) throws KeyStoreException {

        KeyStore.PrivateKeyEntry entry;
        try (InputStream in = new FileInputStream(p12)) {
            KeyStore ks = KeyStore.getInstance("PKCS12");
            ks.load(in, password.toCharArray());

            entry = (KeyStore.PrivateKeyEntry) ks.getEntry(
                    alias,
                    new KeyStore.PasswordProtection(
                            password.toCharArray()));
        } catch (Exception e) {
            throw new KeyStoreException(
                    String.format(
                            "Failed to get certificate entry from key store: %1$s/%2$s",
                            p12,
                            alias),
                    e);
        }

        if (entry == null) {
            throw new KeyStoreException(
                    String.format(
                            "Bad key store: %1$s/%2$s",
                            p12,
                            alias));
        }

        return new KeyPair(
                entry.getCertificate().getPublicKey(),
                entry.getPrivateKey());
    }

    @BeforeAll
    public static void init() {
        sshHost = System.getProperty("ssh-host");
        if (sshHost == null) {
            sshHost = "localhost";
            sshUser = "root";
            sshPassword = "password";
            try {
                sshKeyPair = KeyPairGenerator.getInstance("RSA").generateKeyPair();
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            }

            sshd = new SSHD();
            sshd.setUser(
                    sshUser,
                    sshPassword,
                    sshKeyPair.getPublic());
            try {
                sshd.start();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            sshPort = sshd.getPort();
        } else {
            sshPort = Integer.parseInt(System.getProperty("ssh-test-port", "22"));
            sshUser = System.getProperty("ssh-test-user", "root");
            sshPassword = System.getProperty("ssh-test-password", "password");
            try {
                sshKeyPair = getKeyPair(
                        System.getProperty("ssh-test-p12", "src/test/resources/key.p12"),
                        System.getProperty("ssh-test-p12-alias", "1"),
                        System.getProperty("ssh-test-p12-password", "NoSoup4U"));
            } catch (KeyStoreException e) {
                throw new RuntimeException(e);
            }
        }

        System.out.println("Key fingerprint is: " + OpenSSHUtils.getKeyString(sshKeyPair.getPublic(), "test"));
    }

    @AfterAll
    public static void terminate() {
        if (sshd != null) {
            sshd.stop();
        }
    }

    @BeforeEach
    public void setUp() {
        sshDialog = new SSHDialog();
        sshDialog.setHost(sshHost, sshPort);
        sshDialog.setPassword(sshPassword);
        sshDialog.setKeyPair(sshKeyPair);
        sshDialog.setSoftTimeout(10 * 1000);
        sshDialog.setHardTimeout(30 * 1000);
    }

    @AfterEach
    public void tearDown() throws Exception {
        if (sshDialog != null) {
            sshDialog.close();
            sshDialog = null;
        }
    }

    @Test
    public void testKeyPair() throws Exception {
        sshDialog.connect();
        sshDialog.authenticate();
    }

    @Test
    public void testPassword() throws Exception {
        sshDialog.setKeyPair(null);
        sshDialog.connect();
        sshDialog.authenticate();
    }

    @Test
    public void testWrongKeyPair() throws Exception {
        sshDialog.setKeyPair(
                KeyPairGenerator.getInstance("RSA").generateKeyPair());
        sshDialog.connect();
        assertThrows(AuthenticationException.class, sshDialog::authenticate);
    }

    @Test
    public void testWrongPassword() throws Exception {
        sshDialog.setKeyPair(null);
        sshDialog.setPassword("bad");
        sshDialog.connect();
        assertThrows(AuthenticationException.class, sshDialog::authenticate);
    }

    @Test
    public void testSimple() throws Throwable {
        try (final InputStream start = new ByteArrayInputStream("start\n".getBytes("UTF-8"))) {
            Sink sink = new Sink(
                    new String[] {
                            "start",
                            "text1",
                            "text2"
                    },
                    new String[] {
                            "text1",
                            "text2"
                    });
            sshDialog.connect();
            sshDialog.authenticate();
            sshDialog.executeCommand(
                    sink,
                    "cat",
                    new InputStream[] { start });
            sink.exception();
        }
    }

    @Test
    public void testTimeout() throws Throwable {
        Sink sink = new Sink(
                new String[] {
                        "start"
                },
                new String[] {
                });
        sshDialog.setSoftTimeout(1 * 1000);
        sshDialog.connect();
        sshDialog.authenticate();
        assertThrows(TimeLimitExceededException.class, () -> {
            sshDialog.executeCommand(
                    sink,
                    "cat",
                    null);
            sink.exception();
        });
    }

    @Test
    public void testStderr() throws Throwable {
        try (final InputStream start = new ByteArrayInputStream("start\n".getBytes("UTF-8"))) {
            Sink sink = new Sink(
                    new String[] {
                            "start",
                            "text1",
                            "text2"
                    },
                    new String[] {
                            "text1",
                            "text2"
                    });
            assertThrows(RuntimeException.class, () -> {
                sshDialog.connect();
                sshDialog.authenticate();
                sshDialog.executeCommand(
                        sink,
                        "echo message >&2 && cat",
                        new InputStream[] { start });
                sink.exception();
            });
        }
    }

    @Test
    public void testLong() throws Throwable {
        final String LINE = "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAASDSSSSSSSSSSSSSSSSSSSSSSSDDDDDD";
        final int NUM = 10000;
        final int FACTOR = 5;

        StringBuilder longText = new StringBuilder();
        for (int i = 0; i < NUM / FACTOR; i++) {
            longText.append(LINE).append("\n");
        }

        List<String> expect = new LinkedList<>();
        expect.add("start");
        for (int i = 0; i < NUM; i++) {
            expect.add(LINE);
        }

        List<String> send = new LinkedList<>();
        for (int i = 0; i < NUM; i++) {
            if (i % (NUM / FACTOR) == 0) {
                send.add(longText.toString());
            } else {
                send.add(null);
            }
        }

        Sink sink = new Sink(
                expect.toArray(new String[0]),
                send.toArray(new String[0]));
        sshDialog.connect();
        sshDialog.authenticate();
        sshDialog.executeCommand(
                sink,
                "echo start && sleep 4 && cat",
                null);
        sink.exception();
    }

    private static class ReaderSink implements Runnable, SSHDialog.Sink {

        private SSHDialog.Control control;
        private BufferedReader incoming;
        private PrintWriter outgoing;
        private Throwable throwable;
        private Thread thread;
        private int delay;
        private String last;

        public ReaderSink(int delay) {
            thread = new Thread(this);
            this.delay = delay;
        }

        public String getLast() {
            return last;
        }

        public void exception() throws Throwable {
            if (throwable != null) {
                throw throwable;
            }
        }

        @Override
        public void setControl(SSHDialog.Control control) {
            this.control = control;
        }

        @Override
        public void setStreams(InputStream incoming, OutputStream outgoing) {
            this.incoming = incoming == null ? null : new BufferedReader(
                    new InputStreamReader(
                            incoming,
                            StandardCharsets.UTF_8),
                    BUFFER_SIZE);
            this.outgoing = outgoing == null ? null : new PrintWriter(
                    new OutputStreamWriter(
                            outgoing,
                            StandardCharsets.UTF_8),
                    true);
        }

        @Override
        public void start() {
            thread.start();
        }

        @Override
        public void stop() {
            if (thread != null) {
                while (true) {
                    try {
                        thread.join();
                        break;
                    } catch (InterruptedException ignore) {
                    }
                }
                thread = null;
            }
        }

        public void run() {
            try {
                String l;
                while ((l = incoming.readLine()) != null) {
                    last = l;
                    Thread.sleep(delay);
                }
            } catch (Throwable t) {
                if (throwable == null) {
                    throwable = t;
                }
            } finally {
                try {
                    control.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    @Test
    public void testDelay() throws Throwable {
        ReaderSink sink = new ReaderSink(10);
        sshDialog.setSoftTimeout(60 * 1000);
        sshDialog.setHardTimeout(60 * 1000);
        sshDialog.connect();
        sshDialog.authenticate();
        sshDialog.executeCommand(
                sink,
                "x=0;while [ $x -lt 100 ]; do echo line$x; x=$(($x+1)); done",
                null);
        sink.exception();
        assertEquals("line99", sink.getLast());
    }
}
