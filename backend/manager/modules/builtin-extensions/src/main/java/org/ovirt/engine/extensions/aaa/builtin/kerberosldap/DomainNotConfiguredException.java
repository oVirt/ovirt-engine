package org.ovirt.engine.extensions.aaa.builtin.kerberosldap;

public class DomainNotConfiguredException extends RuntimeException {

    private static final String ERROR_MESSAGE =
            "No domain object was obtained for domain %1$s - this domain is probably not configured in the database.";

    public DomainNotConfiguredException(String domainName) {
        super(String.format(ERROR_MESSAGE, domainName));
    }

}
