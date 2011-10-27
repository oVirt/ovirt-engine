package org.ovirt.engine.core.itests.ldap;

import java.util.List;

import javax.naming.Name;

import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.DistinguishedName;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.filter.EqualsFilter;

import org.ovirt.engine.core.bll.adbroker.EmptyAttributeMapper;

public class IPAPersonDaoImpl implements PersonDao  {

    private LdapTemplate ldapTemplate;

    @Override
    public void create(Person person) {
       Name dn = buildDn(person);
       DirContextAdapter context = new DirContextAdapter(dn);
       mapToContext(person, context);
       getLdapTemplate().bind(dn, context, null);
    }

    @Override
    public void update(Person person) {
       Name dn = buildDn(person);
       DirContextAdapter context = (DirContextAdapter)getLdapTemplate().lookup(dn);
       mapToContext(person, context);
       getLdapTemplate().modifyAttributes(dn, context.getModificationItems());
    }

    protected void mapToContext(Person user, DirContextAdapter context) {
        context.setAttributeValues("objectclass", new String[] { "top", "person", "organizationalperson",
                "inetorgperson", "inetuser", "posixaccount", "krbprincipalaux", "krbticketpolicyaux", "ipaobject",
                "mepOriginEntry" });
        context.setAttributeValue("cn", user.getUsername());
        context.setAttributeValue("sn", user.getSurName());
        context.setAttributeValue("givenname", user.getGivenName());
        context.setAttributeValue("description", user.getDescription());
        context.setAttributeValue("homeDirectory", user.getHomeDirectory());
        context.setAttributeValue("gidNumber", user.getGidNumber());
        context.setAttributeValue("uidNumber", user.getUidNumber());
        context.setAttributeValue("krbPrincipalName", user.getUsername() + "@" + user.getDomain());
    }

    protected Name buildDn(Person user) {
        return buildDn(user.getUsername());
    }

    protected Name buildDn(String fullname) {
        DistinguishedName dn = new DistinguishedName();
        dn.add("cn", "accounts");
        dn.add("cn", "users");
        dn.add("uid", fullname);
        return dn;
    }

    @Override
    public void delete(Person person) {
        ldapTemplate.unbind(buildDn(person));
    }

    public Person findByPrimaryKey(String name, String company, String country) {
        Name dn = buildDn(name);
        return (Person) ldapTemplate.lookup(dn, getContextMapper());
    }

    private AttributesMapper getContextMapper() {
        return new EmptyAttributeMapper();
    }

    public List findAll() {
        EqualsFilter filter = new EqualsFilter("objectclass", "person");
        return ldapTemplate.search(DistinguishedName.EMPTY_PATH, filter.encode(), getContextMapper());
    }

    public void setLdapTemplate(LdapTemplate ldapTemplate) {
        this.ldapTemplate = ldapTemplate;
    }

    public LdapTemplate getLdapTemplate() {
        return ldapTemplate;
    }

    @Override
    public void create(Person... persons) {
        for (Person p : persons) {
            create(p);
        }
    }

    @Override
    public void delete(Person... persons) {
        for (Person p : persons) {
            delete(p);
        }
    }

    @Override
    public List runFilter(String filter) {
        return runFilter("", filter);
    }

    @Override
    public List runFilter(String baseDN, String filter) {
        return ldapTemplate.search(baseDN, filter, new IPAPersonContextMapper());
    }
 }
