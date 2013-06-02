package org.ovirt.engine.core.bll.adbroker;

import static org.ovirt.engine.core.bll.adbroker.ITDSUserAttributes.department;
import static org.ovirt.engine.core.bll.adbroker.ITDSUserAttributes.givenname;
import static org.ovirt.engine.core.bll.adbroker.ITDSUserAttributes.mail;
import static org.ovirt.engine.core.bll.adbroker.ITDSUserAttributes.sn;
import static org.ovirt.engine.core.bll.adbroker.ITDSUserAttributes.title;
import static org.ovirt.engine.core.bll.adbroker.ITDSUserAttributes.uid;
import static org.ovirt.engine.core.bll.adbroker.ITDSUserAttributes.uniqueIdentifier;

import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;

import org.ovirt.engine.core.common.businessentities.LdapUser;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;
import org.springframework.ldap.core.ContextMapper;
import org.springframework.ldap.core.DirContextAdapter;

public class ITDSUserContextMapper implements ContextMapper {

    private static Log log = LogFactory.getLog(LdapBrokerImpl.class);

    protected final static String[] USERS_ATTRIBUTE_FILTER = { uniqueIdentifier.name(), uid.name(),
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
        String objectGuid;
        try {
            objectGuid = (String)attributes.get(uniqueIdentifier.name()).get(0);
            user.setUserId(Guid.createGuidFromString(objectGuid));

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
            log.error("Failed populating user",e);
            return null;
        }

        return user;
    }
}
