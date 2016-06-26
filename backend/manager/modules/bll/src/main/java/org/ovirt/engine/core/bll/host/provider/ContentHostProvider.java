package org.ovirt.engine.core.bll.host.provider;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.Erratum;

public interface ContentHostProvider {
    List<Erratum> getErrataForHost(String hostName);

    Erratum getErratumForHost(String hostName, String erratumId);

    boolean isContentHostExist(String hostName);
}
