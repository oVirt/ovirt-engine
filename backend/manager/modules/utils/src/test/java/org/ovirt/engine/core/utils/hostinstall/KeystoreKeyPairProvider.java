package org.ovirt.engine.core.utils.hostinstall;

import java.security.KeyPair;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.List;

import org.apache.sshd.common.keyprovider.AbstractKeyPairProvider;
import java.util.logging.Logger;

public class KeystoreKeyPairProvider extends AbstractKeyPairProvider {

    private static final Logger LOG = Logger.getLogger(KeystoreKeyPairProvider.class.getName());

    private String[] files;
    private String password;
    private String alias;

    public KeystoreKeyPairProvider() {
    }

    public KeystoreKeyPairProvider(String[] files) {
        this.files = files;
    }

    public KeystoreKeyPairProvider(String[] files, String pass, String alias) {
        this.files = files;
        this.password = pass;
        this.alias = alias;
    }

    public String[] getFiles() {
        return files;
    }

    public void setFiles(String[] files) {
        this.files = files;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String pass) {
        this.password = pass;
    }

    public String getAlias() {
        return this.alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public KeyPair[] loadKeys() {
        List<KeyPair> keys = new ArrayList<KeyPair>();
        for (int i = 0; i < files.length; i++) {
            KeyStore ks = null;
            char[] pass = null;
            java.io.FileInputStream fis = null;
            try {
                ks = KeyStore.getInstance("JKS");
                // get user password and file input stream
                pass = this.password.toCharArray();
                fis = new java.io.FileInputStream(files[i]);
                ks.load(fis, pass);
                fis.close();
            } catch (Exception e1) {
                System.out.println("E1:" + e1);
            }

            try {
                fis.close();
            } catch (Exception a) {
            }
            fis = null;

            try {
                KeyStore.PrivateKeyEntry pkEntry = (KeyStore.PrivateKeyEntry) ks.getEntry(this.alias,
                        new KeyStore.PasswordProtection(pass));
                keys.add(new KeyPair(ks.getCertificate(this.alias).getPublicKey(), pkEntry.getPrivateKey()));
            } catch (Exception e1) {
                LOG.severe(String.format("Unable to read key %s: %s", files[i], e1));
                System.out.println("E1:" + e1);
            } finally {
                ks = null;
            }
        }

        return keys.toArray(new KeyPair[keys.size()]);
    }
}
