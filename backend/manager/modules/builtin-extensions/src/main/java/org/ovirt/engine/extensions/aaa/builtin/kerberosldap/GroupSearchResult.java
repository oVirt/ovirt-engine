package org.ovirt.engine.extensions.aaa.builtin.kerberosldap;

import java.util.Collections;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.LdapGroup;
import org.ovirt.engine.core.common.utils.ExternalId;

/**
 * Contains search result information for directory groups.
 */
public class GroupSearchResult {
    private List<String> memberOf = (List<String>) Collections.EMPTY_LIST;
    private String distinguishedName;
    private ExternalId id;

    public GroupSearchResult(LdapGroup group) {
        id = group.getid();
        distinguishedName = group.getDistinguishedName();
        memberOf = group.getMemberOf();
    }

    public List<String> getMemberOf() {
        return memberOf;
    }

    public void setMemberOf(List<String> memberOf) {
        this.memberOf = memberOf;
    }

    public String getDistinguishedName() {
        return distinguishedName;
    }

    public void setDistinguishedName(String distinguishedName) {
        this.distinguishedName = distinguishedName;
    }

    public GroupSearchResult(ExternalId id, List<String> memberOf,
            String distinguishedName) {
        this.id = id;
        this.memberOf = memberOf;
        this.distinguishedName = distinguishedName;
    }

    public void setId(ExternalId id) {
        this.id = id;
    }

    public ExternalId getId() {
        return id;
    }

}
