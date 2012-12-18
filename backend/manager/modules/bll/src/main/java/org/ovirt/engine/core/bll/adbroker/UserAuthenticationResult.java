package org.ovirt.engine.core.bll.adbroker;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.LdapUser;
import org.ovirt.engine.core.dal.VdcBllMessages;

/**
 *
 * @author yzaslavs
 *
 */

public class UserAuthenticationResult {
    private List<VdcBllMessages> errorMessages = new ArrayList<VdcBllMessages>();
    private LdapUser user;

    public LdapUser getUser() {
        return user;
    }

    public UserAuthenticationResult(VdcBllMessages... messages) {
        errorMessages.addAll(Arrays.asList(messages));
    }

    public UserAuthenticationResult(LdapUser user,VdcBllMessages... messages) {
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
