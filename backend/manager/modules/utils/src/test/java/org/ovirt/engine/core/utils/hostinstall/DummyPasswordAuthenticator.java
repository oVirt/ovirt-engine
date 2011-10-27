package org.ovirt.engine.core.utils.hostinstall;

import org.apache.sshd.server.PasswordAuthenticator;
import org.apache.sshd.server.session.ServerSession;

public class DummyPasswordAuthenticator implements PasswordAuthenticator {

    @Override
    public boolean authenticate(String username, String password, ServerSession session) {
        return username != null && username.equals(password);
    }
}
