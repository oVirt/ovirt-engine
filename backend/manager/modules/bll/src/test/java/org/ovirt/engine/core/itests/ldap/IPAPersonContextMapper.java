package org.ovirt.engine.core.itests.ldap;

import static org.ovirt.engine.core.itests.ldap.IPAPersonAttributes.givenname;
import static org.ovirt.engine.core.itests.ldap.IPAPersonAttributes.ipaUniqueId;
import static org.ovirt.engine.core.itests.ldap.IPAPersonAttributes.krbPrincipalname;
import static org.ovirt.engine.core.itests.ldap.IPAPersonAttributes.mail;
import static org.ovirt.engine.core.itests.ldap.IPAPersonAttributes.memberof;
import static org.ovirt.engine.core.itests.ldap.IPAPersonAttributes.sn;
import static org.ovirt.engine.core.itests.ldap.IPAPersonAttributes.title;
import static org.ovirt.engine.core.itests.ldap.IPAPersonAttributes.department;

import java.util.ArrayList;
import java.util.List;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;

import org.springframework.ldap.core.ContextMapper;
import org.springframework.ldap.core.DirContextAdapter;

import org.ovirt.engine.core.common.businessentities.AdUser;
import org.ovirt.engine.core.compat.Guid;

public class IPAPersonContextMapper implements ContextMapper {

    public final static String[] USERS_ATTRIBUTE_FILTER = { ipaUniqueId.name(), krbPrincipalname.name(),
        givenname.name(), department.name(), title.name(), mail.name(), memberof.name(),
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

        AdUser user;
        user = new AdUser();

        // user's Guid
        String objectGuid;
        try {
            objectGuid = (String)attributes.get(ipaUniqueId.name()).get(0);
            user.setUserId(Guid.createGuidFromString(objectGuid));

            // Getting other string properties
            Attribute att = attributes.get(krbPrincipalname.name());
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
            e.printStackTrace();
            return null;
        }

        return user;
    }

}
