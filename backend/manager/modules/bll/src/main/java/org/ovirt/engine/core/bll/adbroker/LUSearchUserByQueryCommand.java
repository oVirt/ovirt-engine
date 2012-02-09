package org.ovirt.engine.core.bll.adbroker;

import java.util.regex.Pattern;
import java.util.regex.Matcher;
import org.ovirt.engine.core.compat.*;
import org.ovirt.engine.core.common.businessentities.*;
import org.ovirt.engine.core.utils.jwin32.*;
import org.ovirt.engine.core.utils.log.LogCompat;
import org.ovirt.engine.core.utils.log.LogFactoryCompat;

public class LUSearchUserByQueryCommand extends LUSearchGroupsByQueryCommand {
    private static LogCompat log = LogFactoryCompat.getLog(LUSearchUserByQueryCommand.class);

    public LUSearchUserByQueryCommand(LdapSearchByQueryParameters parameters) {
        super(parameters);
    }

    @Override
    protected void ExecuteQuery() {
        log.debug("ExecuteQuery Entry, Query=" + getQuery());

        java.util.ArrayList<AdUser> users = new java.util.ArrayList<AdUser>();

        Pattern p = queryToPattern(getQuery(), "givenname");

        for (AdUser user : getAdUsers()) {
            Matcher mName = p.matcher(user.getUserName());
            Matcher mFull = p.matcher(user.getName());

            if (p == null || mName.matches() || mFull.matches()) {
                log.debug("sid=" + user.getUserId());
                users.add(user);
            }
        }
        log.debug("ExecuteQuery Return, users.size = " + users.size());
        setReturnValue(users);
    }
}
