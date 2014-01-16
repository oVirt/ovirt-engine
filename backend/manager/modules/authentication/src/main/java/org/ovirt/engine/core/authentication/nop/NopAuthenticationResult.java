package org.ovirt.engine.core.authentication.nop;

import java.util.Collections;
import java.util.List;

import org.ovirt.engine.core.authentication.AuthenticationResult;

public class NopAuthenticationResult extends AuthenticationResult {

    public NopAuthenticationResult() {
    }

    @Override
    public boolean isSuccessful() {
        return true;
    }

    @Override
    public List<String> resolveMessage() {
        return Collections.emptyList();
    }
}
