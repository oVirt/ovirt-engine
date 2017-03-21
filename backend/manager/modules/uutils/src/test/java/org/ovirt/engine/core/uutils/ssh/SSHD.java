package org.ovirt.engine.core.uutils.ssh;

import java.io.IOException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.util.LinkedList;
import java.util.List;

import org.apache.sshd.SshServer;
import org.apache.sshd.common.KeyPairProvider;
import org.apache.sshd.server.PasswordAuthenticator;
import org.apache.sshd.server.PublickeyAuthenticator;
import org.apache.sshd.server.session.ServerSession;
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
        KeyPair keyPair;

        public MyKeyPairProvider(KeyPair keyPair) {
            this.keyPair = keyPair;
        }

        @Override
        public KeyPair loadKey(String type) {
            return keyPair;
        }

        /* >=0.10 */
        // @Override
        public Iterable<KeyPair> loadKeys() {
            List<KeyPair> ret = new LinkedList<>();
            ret.add(keyPair);
            return ret;
        }

        @Override
        public String getKeyTypes() {
            return SSH_RSA;
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
                        new String[] {
                                "/bin/sh",
                                "-i"
                        }));
        sshd.setCommandFactory(
                command -> new ProcessShellFactory(
                        new String[] {
                                "/bin/sh",
                                "-c",
                                command
                        }).create());
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
            } catch (InterruptedException ignore) {
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
