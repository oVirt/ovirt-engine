package org.ovirt.engine.extensions.aaa.builtin.kerberosldap;

import static org.ovirt.engine.extensions.aaa.builtin.kerberosldap.OpenLdapUserAttributes.entryuuid;
import static org.ovirt.engine.extensions.aaa.builtin.kerberosldap.OpenLdapUserAttributes.givenname;
import static org.ovirt.engine.extensions.aaa.builtin.kerberosldap.OpenLdapUserAttributes.mail;
import static org.ovirt.engine.extensions.aaa.builtin.kerberosldap.OpenLdapUserAttributes.memberof;
import static org.ovirt.engine.extensions.aaa.builtin.kerberosldap.OpenLdapUserAttributes.sn;
import static org.ovirt.engine.extensions.aaa.builtin.kerberosldap.OpenLdapUserAttributes.title;
import static org.ovirt.engine.extensions.aaa.builtin.kerberosldap.OpenLdapUserAttributes.uid;

import java.util.ArrayList;
import java.util.List;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;

import org.ovirt.engine.core.common.businessentities.aaa.LdapUser;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;
import org.springframework.ldap.core.ContextMapper;
import org.springframework.ldap.core.DirContextAdapter;

public class OpenLdapUserContextMapper implements ContextMapper {

    private static final Log log = LogFactory.getLog(LdapBrokerImpl.class);

    static final String[] USERS_ATTRIBUTE_FILTER = { entryuuid.name(),
        givenname.name(), uid.name(), title.name(), mail.name(), memberof.name(),
        sn.name() };

    @Override
    public Object mapFromContext(Object ctx) {

        if (ctx == null) {
            return null;
        }

        DirContextAdapter searchResult = (DirContextAdapter) ctx;
        Attributes attributes = searchResult.getAttributes();

        if (attributes == null) {
            return null;
        }

        LdapUser user;
        user = new LdapUser();

        // user's Guid
        try {
            String idText = (String) attributes.get(entryuuid.name()).get(0);
            Guid idObject = Guid.createGuidFromStringDefaultEmpty(idText);
            user.setUserId(idObject.toString());

            // Getting other string properties
            Attribute att = attributes.get(uid.name());
            if (att != null) {
                user.setUserName((String) att.get(0));
            } else {
                return null;
            }

            att = attributes.get(givenname.name());
            if (att != null) {
                user.setName((String) att.get(0));
            }
            att = attributes.get(sn.name());
            if (att != null) {
                user.setSurName((String) att.get(0));
            }
            att = attributes.get(title.name());
            if (att != null) {
                user.setTitle((String) att.get(0));
            }

            att = attributes.get(mail.name());
            if (att != null) {
                user.setEmail((String) att.get(0));
            }

            att = attributes.get(memberof.name());
            if (att != null) {
                NamingEnumeration<?> groupsNames = att.getAll();
                List<String> memberOf = new ArrayList<String>();
                while (groupsNames.hasMoreElements()) {
                    memberOf.add((String) groupsNames.nextElement());
                }
                user.setMemberof(memberOf);
            } else {
                // In case the attribute is null, an empty list is set
                // in the "memberOf" field in order to avoid a
                // NullPointerException
                // while traversing on the groups list in
                // LdapBrokerCommandBase.ProceedGroupsSearchResult

                user.setMemberof(new ArrayList<String>());
            }
        } catch (NamingException e) {
            log.error("Failed populating user", e);
            return null;
        }

        return user;
    }

}
