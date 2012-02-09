package org.ovirt.engine.core.bll.adbroker;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.ovirt.engine.core.common.businessentities.ad_groups;
import org.ovirt.engine.core.utils.log.LogCompat;
import org.ovirt.engine.core.utils.log.LogFactoryCompat;

public class LUSearchGroupsByQueryCommand extends LUBrokerCommandBase {
    private static LogCompat log = LogFactoryCompat.getLog(LUSearchGroupsByQueryCommand.class);

    protected String getQuery() {
        // TODO:rgolan
        return "";
        //return ((LdapSearchByQueryParameters) getParameters()).getQuery();
    }

    public LUSearchGroupsByQueryCommand(LdapSearchByQueryParameters parameters) {
        super(parameters);
    }

    @Override
    protected void ExecuteQuery() {
        log.debug("ExecuteQuery Entry, Query=" + getQuery());

        java.util.ArrayList<ad_groups> groups = new java.util.ArrayList<ad_groups>();

        Pattern p = queryToPattern(getQuery(), "name");

        for (ad_groups group : getAdGroups()) {
            Matcher mName = p.matcher(group.getname());

            if (p == null || mName.matches()) {
                log.debug("sid=" + group.getid());
                groups.add(group);
            }
        }
        log.debug("ExecuteQuery Return, groups.size = " + groups.size());
        setReturnValue(groups);
    }
}
