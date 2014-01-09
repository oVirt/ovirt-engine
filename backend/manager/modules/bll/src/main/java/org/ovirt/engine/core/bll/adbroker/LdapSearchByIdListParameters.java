package org.ovirt.engine.core.bll.adbroker;

import java.util.List;

import org.ovirt.engine.core.common.utils.ExternalId;

public class LdapSearchByIdListParameters extends LdapBrokerBaseParameters {
    private List<ExternalId> ids;

    public LdapSearchByIdListParameters(String domain, List<ExternalId> ids) {
        super(domain);
        this.ids = ids;
    }

    public List<ExternalId> getIds() {
        return ids;
    }

    public void setIds(List<ExternalId> ids) {
        this.ids = ids;
    }
}
