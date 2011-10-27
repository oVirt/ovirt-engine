package org.ovirt.engine.core.utils.hostinstall;

import java.security.PublicKey;

import org.apache.sshd.server.PublickeyAuthenticator;
import org.apache.sshd.server.session.ServerSession;

public class DummyPublickeyAuthenticator implements PublickeyAuthenticator {

    @Override
    public boolean authenticate(String username, PublicKey key, ServerSession session) {
        return true;
    }
}
