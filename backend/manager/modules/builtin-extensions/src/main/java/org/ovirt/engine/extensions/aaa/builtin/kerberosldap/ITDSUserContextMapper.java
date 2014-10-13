package org.ovirt.engine.extensions.aaa.builtin.kerberosldap;

import static org.ovirt.engine.extensions.aaa.builtin.kerberosldap.ITDSUserAttributes.department;
import static org.ovirt.engine.extensions.aaa.builtin.kerberosldap.ITDSUserAttributes.givenname;
import static org.ovirt.engine.extensions.aaa.builtin.kerberosldap.ITDSUserAttributes.mail;
import static org.ovirt.engine.extensions.aaa.builtin.kerberosldap.ITDSUserAttributes.sn;
import static org.ovirt.engine.extensions.aaa.builtin.kerberosldap.ITDSUserAttributes.title;
import static org.ovirt.engine.extensions.aaa.builtin.kerberosldap.ITDSUserAttributes.uid;
import static org.ovirt.engine.extensions.aaa.builtin.kerberosldap.ITDSUserAttributes.uniqueIdentifier;

import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ldap.core.ContextMapper;
import org.springframework.ldap.core.DirContextAdapter;

import org.ovirt.engine.core.common.businessentities.aaa.LdapUser;
import org.ovirt.engine.core.compat.Guid;

public class ITDSUserContextMapper implements ContextMapper {

    private static final Logger log = LoggerFactory.getLogger(LdapBrokerImpl.class);

    static final String[] USERS_ATTRIBUTE_FILTER = { uniqueIdentifier.name(), uid.name(),
        givenname.name(), department.name(), title.name(), mail.name(), sn.name() };

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
            String idText = (String) attributes.get(uniqueIdentifier.name()).get(0);
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

            att = attributes.get(department.name());
            if (att != null) {
                user.setDepartment((String) att.get(0));
            }

        } catch (NamingException e) {
            log.error("Failed populating user", e);
            return null;
        }

        return user;
    }
}
