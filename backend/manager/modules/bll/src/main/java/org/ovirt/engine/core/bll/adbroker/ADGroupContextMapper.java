package org.ovirt.engine.core.bll.adbroker;

import static org.ovirt.engine.core.bll.adbroker.ADGroupAttributes.memberof;
import static org.ovirt.engine.core.bll.adbroker.ADGroupAttributes.objectGuid;

import java.util.ArrayList;
import java.util.List;

import javax.naming.NamingEnumeration;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;

import org.springframework.ldap.core.ContextMapper;
import org.springframework.ldap.core.DirContextAdapter;

import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;

public class ADGroupContextMapper implements ContextMapper {

    private static Log log = LogFactory.getLog(LdapBrokerImpl.class);

    protected final static String[] GROUP_ATTRIBUTE_FILTER = { memberof.name(), objectGuid.name() };

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
        if ( attributes.get(objectGuid.name()) == null ) {
            return null;
        }

        try {
            List<String> memberOf = new ArrayList<String>();
            Attribute att = attributes.get(memberof.name());
            if (att != null) {
                NamingEnumeration<?> groupsNames = att.getAll();
                while (groupsNames.hasMoreElements()) {
                    memberOf.add((String) groupsNames.nextElement());
                }
            }

            Object adObjectGuid = attributes.get(objectGuid.name()).get(0);
            byte[] guidBytes = (byte[]) adObjectGuid;
            Guid guid = new Guid(guidBytes,false);

            String distinguishedName = searchResult.getNameInNamespace();
            distinguishedName = LdapBrokerUtils.hadleNameEscaping(distinguishedName);
            GroupSearchResult groupSearchResult = new GroupSearchResult(guid,memberOf, distinguishedName);
            return groupSearchResult;
        } catch (Exception ex) {
            log.error("Failed populating group", ex);
            return null;
        }
    }
}

