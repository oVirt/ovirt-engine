package org.ovirt.engine.core.authentication.result;

import java.util.Collections;
import java.util.List;

import org.ovirt.engine.core.authentication.AuthenticationResult;
import org.ovirt.engine.core.common.errors.VdcBllMessages;

public class BooleanAuthenticationResult extends AuthenticationResult<Boolean> {

    public BooleanAuthenticationResult(Boolean detailedInfo) {
        super(detailedInfo);
    }

    @Override
    public boolean isSuccessful() {
        return detailedInfo;
    }

    @Override
    public List<VdcBllMessages> resolveMessage() {
        return Collections.emptyList();
    }

}
