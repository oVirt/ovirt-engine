package org.ovirt.engine.core.bll.quota;

import org.ovirt.engine.core.common.businessentities.Quota;
import org.ovirt.engine.core.compat.Guid;

public class QuotaSanityParameter extends QuotaConsumptionParameter {

    public QuotaSanityParameter(Guid quotaId, Quota quota) {
        super(quotaId, quota, null);
    }

    @Override
    public ParameterType getParameterType() {
        return ParameterType.SANITY;
    }

    @Override
    public QuotaSanityParameter clone() throws CloneNotSupportedException {
        return new QuotaSanityParameter(getQuotaGuid(), getQuota());
    }
}
