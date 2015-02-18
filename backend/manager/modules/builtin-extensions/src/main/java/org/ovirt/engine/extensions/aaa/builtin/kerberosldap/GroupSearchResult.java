package org.ovirt.engine.extensions.aaa.builtin.kerberosldap;

import java.util.Collections;
import java.util.List;

/**
 * Contains search result information for directory groups.
 */
public class GroupSearchResult {
    private List<String> memberOf = (List<String>) Collections.EMPTY_LIST;
    private String distinguishedName;
    private String id;

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

    public GroupSearchResult(String id, List<String> memberOf,
            String distinguishedName) {
        this.id = id;
        this.memberOf = memberOf;
        this.distinguishedName = distinguishedName;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

}
