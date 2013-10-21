package org.ovirt.engine.core.authentication.provisional;

import java.util.List;

import org.ovirt.engine.core.authentication.AuthenticationResult;
import org.ovirt.engine.core.bll.adbroker.UserAuthenticationResult;
import org.ovirt.engine.core.common.errors.VdcBllMessages;

public class ProvisionalAuthenticationResult extends AuthenticationResult<UserAuthenticationResult> {

    public ProvisionalAuthenticationResult(UserAuthenticationResult detailedInfo) {
        super(detailedInfo);
    }

    @Override
    public boolean isSuccessful() {
        return detailedInfo.isSuccessful();
    }

    @Override
    public List<VdcBllMessages> resolveMessage() {
        return detailedInfo.getErrorMessages();
    }

}
