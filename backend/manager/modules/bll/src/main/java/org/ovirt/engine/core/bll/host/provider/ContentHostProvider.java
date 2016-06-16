package org.ovirt.engine.core.bll.host.provider;

import org.ovirt.engine.core.common.businessentities.ErrataData;
import org.ovirt.engine.core.common.businessentities.Erratum;
import org.ovirt.engine.core.common.queries.ErrataFilter;

public interface ContentHostProvider {
    ErrataData getErrataForHost(String hostName, ErrataFilter errataFilter);

    Erratum getErratumForHost(String hostName, String erratumId);

    boolean isContentHostExist(String hostName);
}
