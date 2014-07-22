package org.ovirt.engine.extensions.aaa.builtin.kerberosldap.utils.ipa;

import static org.ovirt.engine.extensions.aaa.builtin.kerberosldap.utils.kerberos.InstallerConstants.ERROR_PREFIX;

import javax.naming.NamingException;
import javax.naming.directory.Attributes;

import org.ovirt.engine.core.compat.Guid;
import org.springframework.ldap.core.ContextMapper;
import org.springframework.ldap.core.DirContextAdapter;

public class ADUserContextMapper implements ContextMapper {
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

        try {
            Object objectGuid = attributes.get("objectGUID").get();
            byte[] guid = (byte[]) objectGuid;
            return new Guid(guid, false).toString();
        } catch (NamingException e) {
            System.err.println(ERROR_PREFIX + "Failed getting user GUID");
            return null;
        }
    }

}
