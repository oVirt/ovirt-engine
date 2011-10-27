package org.ovirt.engine.core.bll.adbroker;

import org.ovirt.engine.core.utils.jwin32.jwin32;
import com.sun.jna.WString;

public class LUChangeUserPasswordCommand extends LUBrokerCommandBase {
    private String getDestinationNewPassword() {
        return (((LdapChangeUserPasswordParameters) getParameters()).getDestinationUserNewPassword());
    }

    private String getDestinationUserName() {
        return (((LdapChangeUserPasswordParameters) getParameters()).getDestinationUserName());
    }

    private String getDestinationUserPassword() {
        return (((LdapChangeUserPasswordParameters) getParameters()).getDestinationUserPassword());
    }

    public LUChangeUserPasswordCommand(LdapChangeUserPasswordParameters parameters) {
        super(parameters);
    }

    @Override
    protected void ExecuteQuery() {
        if (jwin32.netapi32.NetUserChangePassword(
                new WString(""), // new WString(this.getDomain().toString()),
                new WString(this.getDestinationUserName()),
                new WString(((LdapChangeUserPasswordParameters) getParameters()).getDestinationUserPassword()),
                new WString(((LdapChangeUserPasswordParameters) getParameters()).getDestinationUserNewPassword())
                ) == jwin32.NERR_Success) {
            setSucceeded(true);
        } else {
            setSucceeded(false);
        }
    }
}
