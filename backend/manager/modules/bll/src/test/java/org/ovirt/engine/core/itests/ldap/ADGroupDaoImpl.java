package org.ovirt.engine.core.itests.ldap;

import java.util.ArrayList;
import java.util.List;

import javax.naming.Name;

import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.DistinguishedName;
import org.springframework.ldap.core.LdapTemplate;

import org.ovirt.engine.core.bll.adbroker.ADGroupContextMapper;

public class ADGroupDaoImpl implements GroupDao {

    private LdapTemplate ldapTemplate;

    @Override
    public void create(Group group) {
        Name dn = buildDn(group);
        DirContextAdapter context = new DirContextAdapter(dn);
        mapToContext(group, context);
        getLdapTemplate().bind(dn, context, null);
    }

    protected Name buildDn(Group group) {
        return buildDn(group.getName());
    }

    protected Name buildDn(String groupName) {
        DistinguishedName dn = new DistinguishedName();
        dn.add("cn", groupName);
        return dn;
    }

    protected void mapToContext(Group group, DirContextAdapter context) {
        context.setAttributeValues("objectclass", new String[] { "top", "group" });
        context.setAttributeValue("cn", group.getName());
        context.setAttributeValue("sAMAccountName", group.getName());
        context.setAttributeValues("member", getGroupMembers(group).toArray());
    }

    @Override
    public void update(Group group) {
        Name dn = buildDn(group);
        DirContextAdapter context = (DirContextAdapter) getLdapTemplate().lookup(dn);
        mapToContext(group, context);
        getLdapTemplate().modifyAttributes(dn, context.getModificationItems());
    }

    @Override
    public void delete(Group group) {
        ldapTemplate.unbind(buildDn(group));
    }

    public LdapTemplate getLdapTemplate() {
        return ldapTemplate;
    }

    public void setLdapTemplate(LdapTemplate ldapTemplate) {
        this.ldapTemplate = ldapTemplate;
    }

    protected List<String> getGroupMembers(Group group) {
        List<String> members = new ArrayList();
        for (String currMember : group.getUserMembers()) {
            if (currMember.isEmpty()) {
                continue;
            }
            members.add("cn=" + currMember + ",cn=users," + group.getDc());
        }

        for (String currMember : group.getGroupMembers()) {
            if (currMember.isEmpty()) {
                continue;
            }
            members.add("cn=" + currMember + "," + group.getDc());
        }

        return members;
    }

    @Override
    public void create(Group... groups) {
        for (Group g : groups) {
            create(g);
        }
    }

    @Override
    public void delete(Group... groups) {
        for (Group g : groups) {
            delete(g);
        }
    }

    @Override
    public List runFilter(String filter) {
        return runFilter("", filter);
    }

    @Override
    public List runFilter(String baseDN, String filter) {
        return ldapTemplate.search(baseDN, filter, new ADGroupContextMapper());
    }
}
