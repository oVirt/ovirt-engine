package org.ovirt.engine.core.bll.adbroker;

import org.ovirt.engine.core.common.businessentities.*;

import org.ovirt.engine.core.compat.LogCompat;
import org.ovirt.engine.core.compat.LogFactoryCompat;
import org.ovirt.engine.core.utils.jwin32.*;

import com.sun.jna.ptr.*;
import com.sun.jna.WString;

public class LUGetAdUserByUserNameCommand extends LUBrokerCommandBase {
    private static LogCompat log = LogFactoryCompat.getLog(LUGetAdUserByUserNameCommand.class);

    private String getUserName() {
        return ((LdapSearchByUserNameParameters) getParameters()).getUserName();
    }

    public LUGetAdUserByUserNameCommand(LdapSearchByUserNameParameters parameters) {
        super(parameters);
    }

    @Override
    protected void ExecuteQuery() {
        log.debug("ExecuteQuery, Entry");

        IntByReference lToken = null;
        PointerByReference p = new PointerByReference();
        int nStatus;
        IntByReference nEntriesRead;
        IntByReference nTotalEntries;
        IntByReference nResumeHandle;

        if (jwin32.netapi32.NetUserGetInfo(new WString(this.getDomain()), new WString(this.getUserName()), 20, p
                ) == jwin32.NERR_Success) {

            USER_INFO_20 ui = new USER_INFO_20(p.getValue());
            log.debug("ui=" + ui.usri20_name);
            AdUser user = populateUser(ui);
            jwin32.netapi32.NetApiBufferFree(p.getValue());

            // Getting the groups
            // Seems local users don't get groups..
            // TODO: check if this just strange or unused.

            setReturnValue(user);
            setSucceeded((getReturnValue() != null));
        } else {
            log.error("ExecuteQuery, NetUserGetInfo failed (" + jwin32.kernel32.GetLastError() + ")");
        }
    }
}
