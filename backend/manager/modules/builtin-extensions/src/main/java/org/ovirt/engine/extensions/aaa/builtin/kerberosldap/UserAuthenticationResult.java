package org.ovirt.engine.extensions.aaa.builtin.kerberosldap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.aaa.LdapUser;
import org.ovirt.engine.core.common.errors.VdcBllMessages;

public class UserAuthenticationResult {
    private List<VdcBllMessages> errorMessages = new ArrayList<VdcBllMessages>();
    private LdapUser user;

    public LdapUser getUser() {
        return user;
    }

    public UserAuthenticationResult(VdcBllMessages... messages) {
        errorMessages.addAll(Arrays.asList(messages));
    }

    public UserAuthenticationResult(LdapUser user, VdcBllMessages... messages) {
        this(messages);
        this.user = user;
    }


    public List<VdcBllMessages> getErrorMessages() {
        return errorMessages;
    }

    public boolean isSuccessful() {
        return errorMessages.size() == 0;
    }
}
