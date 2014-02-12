package org.ovirt.engine.core.aaa.result;

import java.util.Collections;
import java.util.List;

import org.ovirt.engine.core.aaa.AuthenticationResult;

public class BooleanAuthenticationResult extends AuthenticationResult {

    private boolean value;

    public BooleanAuthenticationResult(boolean value) {
        this.value = value;
    }

    @Override
    public boolean isSuccessful() {
        return value;
    }

    @Override
    public List<String> resolveMessage() {
        return Collections.emptyList();
    }

}
