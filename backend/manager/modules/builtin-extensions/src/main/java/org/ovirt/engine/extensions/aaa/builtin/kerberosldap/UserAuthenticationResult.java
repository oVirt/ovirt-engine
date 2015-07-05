package org.ovirt.engine.extensions.aaa.builtin.kerberosldap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.ovirt.engine.core.common.errors.EngineMessage;

public class UserAuthenticationResult {
    private List<EngineMessage> errorMessages = new ArrayList<EngineMessage>();
    private LdapUser user;

    public LdapUser getUser() {
        return user;
    }

    public UserAuthenticationResult(EngineMessage... messages) {
        errorMessages.addAll(Arrays.asList(messages));
    }

    public UserAuthenticationResult(LdapUser user, EngineMessage... messages) {
        this(messages);
        this.user = user;
    }


    public List<EngineMessage> getErrorMessages() {
        return errorMessages;
    }

    public boolean isSuccessful() {
        return errorMessages.size() == 0;
    }
}
