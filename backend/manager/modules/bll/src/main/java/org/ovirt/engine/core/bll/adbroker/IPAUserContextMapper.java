package org.ovirt.engine.core.bll.adbroker;

import static org.ovirt.engine.core.bll.adbroker.IPAUserAttributes.department;
import static org.ovirt.engine.core.bll.adbroker.IPAUserAttributes.givenname;
import static org.ovirt.engine.core.bll.adbroker.IPAUserAttributes.ipaUniqueId;
import static org.ovirt.engine.core.bll.adbroker.IPAUserAttributes.krbPrincipalname;
import static org.ovirt.engine.core.bll.adbroker.IPAUserAttributes.mail;
import static org.ovirt.engine.core.bll.adbroker.IPAUserAttributes.memberof;
import static org.ovirt.engine.core.bll.adbroker.IPAUserAttributes.sn;
import static org.ovirt.engine.core.bll.adbroker.IPAUserAttributes.title;

import java.util.ArrayList;
import java.util.List;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;

import org.springframework.ldap.core.ContextMapper;
import org.springframework.ldap.core.DirContextAdapter;

import org.ovirt.engine.core.common.businessentities.LdapUser;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;

public class IPAUserContextMapper implements ContextMapper {

    private static Log log = LogFactory.getLog(LdapBrokerImpl.class);

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

        LdapUser user;
        user = new LdapUser();

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
            log.error("Failed populating user",e);
            return null;
        }

        return user;
    }

}
