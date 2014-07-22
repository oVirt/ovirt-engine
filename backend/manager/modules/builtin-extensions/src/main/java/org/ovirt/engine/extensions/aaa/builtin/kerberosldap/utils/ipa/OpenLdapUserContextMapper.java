package org.ovirt.engine.extensions.aaa.builtin.kerberosldap.utils.ipa;

import static org.ovirt.engine.extensions.aaa.builtin.kerberosldap.utils.kerberos.InstallerConstants.ERROR_PREFIX;

import javax.naming.NamingException;
import javax.naming.directory.Attributes;

import org.springframework.ldap.core.ContextMapper;
import org.springframework.ldap.core.DirContextAdapter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class OpenLdapUserContextMapper implements ContextMapper {

    private static final Log log = LogFactory.getLog(OpenLdapUserContextMapper.class);

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
            return attributes.get("uid").get(0);
        } catch (NamingException e) {
            log.error(ERROR_PREFIX + "Failed getting user GUID");
            return null;
        }
    }

}
