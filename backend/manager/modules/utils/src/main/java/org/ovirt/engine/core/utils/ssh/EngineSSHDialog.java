package org.ovirt.engine.core.utils.ssh;

import java.io.IOException;
import java.security.KeyStoreException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * SSH dialog to be used with engine defaults
 */
public class EngineSSHDialog extends SSHDialog {

    private static final Log log = LogFactory.getLog(EngineSSHDialog.class);

    protected SSHClient _getSSHClient() {
        return new EngineSSHClient();
    }

    /**
     * Get host fingerprint.
     * @return fingerprint.
     */
    public String getHostFingerprint() throws IOException {
        return ((EngineSSHClient)_client).getHostFingerprint();
    }

    /**
     * Use default engine ssh key.
     */
    public void useDefaultKeyPair() throws KeyStoreException {
        ((EngineSSHClient)_client).useDefaultKeyPair();
    }
}
