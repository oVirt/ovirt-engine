package org.ovirt.engine.core.bll.adbroker;

import org.ovirt.engine.core.compat.Guid;

public abstract class LdapQueryExecutionFormatterBase implements LdapQueryFormatter<LdapQueryExecution> {

    @Override
    public abstract LdapQueryExecution format(LdapQueryMetadata queryMetadata);

    protected Object[] getEncodedParameters(Object[] parameters, LdapGuidEncoder LdapGuidEncoder) {
        if (parameters == null) {
            return null;
        }

        Object[] retVal = parameters.clone();

        int index = 0;

        for (Object parameter : parameters) {
            if (parameter instanceof Guid) {
                retVal[index] = LdapGuidEncoder.encodeGuid((Guid) parameter);
            }
            index++;
        }

        return retVal;
    }
}
