package org.ovirt.engine.core.authentication.nop;

import java.util.Collections;
import java.util.List;

import org.ovirt.engine.core.authentication.AuthenticationResult;
import org.ovirt.engine.core.common.errors.VdcBllMessages;

public class NopAuthenticationResult extends AuthenticationResult<Object> {

    public NopAuthenticationResult() {
        super(null);
    }

    @Override
    public boolean isSuccessful() {
        return true;
    }

    @Override
    public List<VdcBllMessages> resolveMessage() {
        return Collections.emptyList();
    }
}
