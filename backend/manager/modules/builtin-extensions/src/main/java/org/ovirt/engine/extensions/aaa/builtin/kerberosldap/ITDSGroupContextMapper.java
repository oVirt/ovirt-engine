package org.ovirt.engine.extensions.aaa.builtin.kerberosldap;

import static org.ovirt.engine.extensions.aaa.builtin.kerberosldap.ITDSGroupAttributes.uniqueIdentifier;
import static org.ovirt.engine.extensions.aaa.builtin.kerberosldap.ITDSGroupAttributes.uniqueMember;

import java.util.ArrayList;
import java.util.List;

import javax.naming.NamingEnumeration;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ldap.core.ContextMapper;
import org.springframework.ldap.core.DirContextAdapter;

import org.ovirt.engine.core.compat.Guid;

public class ITDSGroupContextMapper implements ContextMapper {

    private static final Logger log = LoggerFactory.getLogger(LdapBrokerImpl.class);

    static final String[] GROUP_ATTRIBUTE_FILTER = { uniqueMember.name(), uniqueIdentifier.name() };

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

        if (attributes.get(uniqueIdentifier.name()) == null) {
            return null;
        }

        try {
            List<String> memberOf = new ArrayList<String>();
            Attribute att = attributes.get(uniqueMember.name());
            if (att != null) {
                NamingEnumeration<?> groupsNames = att.getAll();
                while (groupsNames.hasMoreElements()) {
                    memberOf.add((String) groupsNames.nextElement());
                }
            }

            String objectGuid = (String) attributes.get(uniqueIdentifier.name()).get(0);

            String distinguishedName = searchResult.getNameInNamespace();
            distinguishedName = LdapBrokerUtils.hadleNameEscaping(distinguishedName);
            GroupSearchResult groupSearchResult =
                    new GroupSearchResult(Guid.createGuidFromStringDefaultEmpty(LdapBrokerUtils.getGuidFromNsUniqueId(objectGuid))
                            .toString(),
                            memberOf,
                            distinguishedName);
            return groupSearchResult;
        } catch (Exception ex) {
            log.error("Failed populating group", ex);
            return null;
        }
    }
}
