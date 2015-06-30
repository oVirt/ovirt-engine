package org.ovirt.engine.core.bll.host;

import java.util.List;

import org.ovirt.engine.core.bll.Backend;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.common.utils.RpmVersionUtils;
import org.ovirt.engine.core.compat.RpmVersion;

public class OvirtNodeUpgradeManager implements UpdateAvailable {

    @Override
    public boolean isUpdateAvailable(VDS host) {
        VdcQueryReturnValue returnValue =
                Backend.getInstance().runInternalQuery(VdcQueryType.GetoVirtISOs,
                        new IdQueryParameters(host.getId()));
        List<RpmVersion> isos = returnValue.getReturnValue();
        return RpmVersionUtils.isUpdateAvailable(isos, host.getHostOs());
    }
}
