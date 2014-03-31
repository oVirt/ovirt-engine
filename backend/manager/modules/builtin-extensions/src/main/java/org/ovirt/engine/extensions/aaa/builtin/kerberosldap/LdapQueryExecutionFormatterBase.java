package org.ovirt.engine.extensions.aaa.builtin.kerberosldap;

import org.ovirt.engine.core.compat.Guid;

public abstract class LdapQueryExecutionFormatterBase implements LdapQueryFormatter<LdapQueryExecution> {

    @Override
    public abstract LdapQueryExecution format(LdapQueryMetadata queryMetadata);

    protected abstract String getDisplayFilter(LdapQueryMetadata queryMetadata);

    protected Object[] getEncodedParameters(Object[] parameters, LdapIdEncoder idEncoder) {
        if (parameters == null) {
            return null;
        }

        Object[] retVal = parameters.clone();

        int index = 0;

        for (Object parameter : parameters) {
            if (parameter instanceof Guid) {
                retVal[index] = idEncoder.encodedId((Guid) parameter);
            }
            index++;
        }

        return retVal;
    }
}
