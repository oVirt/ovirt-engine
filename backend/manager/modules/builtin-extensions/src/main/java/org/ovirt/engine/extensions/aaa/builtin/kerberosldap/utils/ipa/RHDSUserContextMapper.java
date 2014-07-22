package org.ovirt.engine.extensions.aaa.builtin.kerberosldap.utils.ipa;

import static org.ovirt.engine.extensions.aaa.builtin.kerberosldap.utils.kerberos.InstallerConstants.ERROR_PREFIX;


import javax.naming.NamingException;
import javax.naming.directory.Attributes;

import org.ovirt.engine.core.compat.Guid;
import org.springframework.ldap.core.ContextMapper;
import org.springframework.ldap.core.DirContextAdapter;

public class RHDSUserContextMapper implements ContextMapper {
    public static String getGuidFromNsUniqueId(String nsUniqueId) {
        // 12345678-12345678-12345678-12345678 -->
        // 12345678-1234-5678-1234-567812345678
        StringBuilder sb = new StringBuilder();
        sb.append(nsUniqueId.substring(0, 13))
                .append("-")
                .append(nsUniqueId.substring(13, 22))
                .append("-")
                .append(nsUniqueId.substring(22, 26))
                .append(nsUniqueId.substring(27, 35));
        return sb.toString();
    }

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
            String objectGuid = (String) attributes.get("nsUniqueId").get(0);
            return Guid.createGuidFromStringDefaultEmpty(getGuidFromNsUniqueId(objectGuid));
        } catch (NamingException e) {
            System.err.println(ERROR_PREFIX + "Failed getting user GUID");
            return null;
        }
    }

}
