package org.ovirt.engine.core.uutils.ssh;

import java.io.IOException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.apache.sshd.common.keyprovider.KeyPairProvider;
import org.apache.sshd.common.session.SessionContext;
import org.apache.sshd.server.SshServer;
import org.apache.sshd.server.auth.password.PasswordAuthenticator;
import org.apache.sshd.server.auth.pubkey.PublickeyAuthenticator;
import org.apache.sshd.server.session.ServerSession;
import org.apache.sshd.server.shell.ProcessShellCommandFactory;
import org.apache.sshd.server.shell.ProcessShellFactory;

public class SSHD {

    static class MyPasswordAuthenticator implements PasswordAuthenticator {
        String user;
        String password;

        public MyPasswordAuthenticator(String user, String password) {
            this.user = user;
            this.password = password;
        }

        @Override
        public boolean authenticate(String user, String password, ServerSession session) {
            return this.user.equals(user) && this.password.equals(password);
        }
    }

    static class MyPublickeyAuthenticator implements PublickeyAuthenticator {
        String user;
        PublicKey key;

        public MyPublickeyAuthenticator(String user, PublicKey key) {
            this.user = user;
            this.key = key;
        }

        @Override
        public boolean authenticate(String user, PublicKey key, ServerSession session) {
            return this.user.equals(user) && this.key.equals(key);
        }
    }

    static class MyKeyPairProvider implements KeyPairProvider {
        private static final Iterable<String> KEY_TYPES =
                Collections.singletonList(SSH_RSA);
        KeyPair keyPair;

        public MyKeyPairProvider(KeyPair keyPair) {
            this.keyPair = keyPair;
        }

        @Override
        public KeyPair loadKey(SessionContext context, String type) {
            return keyPair;
        }

        @Override
        public Iterable<KeyPair> loadKeys(SessionContext session) {
            List<KeyPair> ret = new LinkedList<>();
            ret.add(keyPair);
            return ret;
        }

        @Override
        public Iterable<String> getKeyTypes(SessionContext context) {
            return KEY_TYPES;
        }
    }

    KeyPair keyPair;
    SshServer sshd;

    public SSHD() {
        try {
            keyPair = KeyPairGenerator.getInstance("RSA").generateKeyPair();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        sshd = SshServer.setUpDefaultServer();
        sshd.setKeyPairProvider(new MyKeyPairProvider(keyPair));
        sshd.setShellFactory(
                new ProcessShellFactory(
                        "/bin/sh",
                        "-i"));
        sshd.setCommandFactory(
                (channelSession, command) -> new ProcessShellCommandFactory()
                        .createCommand(
                                channelSession,
                                String.join(" ",
                                        new String[] {
                                                "/bin/sh",
                                                "-c",
                                                command
                                        })));
    }

    public int getPort() {
        return sshd.getPort();
    }

    public PublicKey getKey() {
        return keyPair.getPublic();
    }

    public void setUser(String user, String password, PublicKey key) {
        sshd.setPasswordAuthenticator(new MyPasswordAuthenticator(user, password));
        sshd.setPublickeyAuthenticator(new MyPublickeyAuthenticator(user, key));
    }

    public void start() throws IOException {
        sshd.start();
    }

    public void stop() {
        while (sshd != null) {
            try {
                sshd.stop(true);
                sshd = null;
            } catch (IOException ignore) {
            }
        }
    }

    public static void main(String[] args) throws Exception {
        KeyPair keyPair = KeyPairGenerator.getInstance("RSA").generateKeyPair();

        SSHD sshd = new SSHD();
        sshd.setUser("root", "password", keyPair.getPublic());
        sshd.start();
        System.out.println("Port: " + sshd.getPort());
    }
}
