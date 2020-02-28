package org.ovirt.engine.core.bll.host.provider;

import org.ovirt.engine.core.bll.host.provider.foreman.ContentHostIdentifier;
import org.ovirt.engine.core.common.businessentities.ErrataData;
import org.ovirt.engine.core.common.businessentities.Erratum;
import org.ovirt.engine.core.common.queries.ErrataFilter;

public interface ContentHostProvider {
    ErrataData getErrataForHost(ContentHostIdentifier contentHostIdentifier, ErrataFilter errataFilter);

    Erratum getErratumForHost(ContentHostIdentifier contentHostIdentifier, String erratumId);

    boolean isContentHostExist(ContentHostIdentifier contentHostIdentifier);
}
