package org.ovirt.engine.core.bll.adbroker;

import java.util.Collections;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.LdapGroup;
import org.ovirt.engine.core.compat.Guid;

/**
 * Contains Search result information for ADGroups
 *
 *
 */
public class GroupSearchResult {
    private List<String> _memberOf = (List<String>) Collections.EMPTY_LIST;
    private String _distinguishedName;
    private Guid guid;

    public GroupSearchResult(LdapGroup groups) {
        guid = groups.getid();
        _distinguishedName = groups.getDistinguishedName();
        _memberOf = groups.getMemberOf();
    }

    public List<String> getMemberOf() {
        return _memberOf;
    }

    public void setMemberOf(List<String> memberOf) {
        _memberOf = memberOf;
    }

    public String getDistinguishedName() {
        return _distinguishedName;
    }

    public void setDistinguishedName(String distinguishedName) {
        _distinguishedName = distinguishedName;
    }

    public GroupSearchResult(Guid guid, List<String> memberOf,
            String distinguishedName) {
        _memberOf = memberOf;
        _distinguishedName = distinguishedName;
        this.setGuid(guid);
    }

    public void setGuid(Guid guid) {
        this.guid = guid;
    }

    public Guid getGuid() {
        return guid;
    }

}
