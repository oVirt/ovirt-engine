package org.ovirt.engine.core.bll;

import java.text.MessageFormat;

import org.ovirt.engine.core.bll.storage.StorageHandlingCommandBase;
import org.ovirt.engine.core.bll.utils.VersionSupport;
import org.ovirt.engine.core.common.businessentities.IVdcQueryable;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.interfaces.SearchType;
import org.ovirt.engine.core.common.queries.GetAvailableClusterVersionsParameters;
import org.ovirt.engine.core.common.queries.SearchParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

public class GetAvailableClusterVersionsQuery<P extends GetAvailableClusterVersionsParameters>
        extends QueriesCommandBase<P> {
    public GetAvailableClusterVersionsQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        if (getParameters().getVdsGroupId() != null) {
            java.util.ArrayList<Version> result = new java.util.ArrayList<Version>();
            VDSGroup cluster = DbFacade.getInstance().getVdsGroupDAO().get(getParameters().getVdsGroupId());
            if (cluster != null) {
                SearchParameters p = new SearchParameters(MessageFormat.format(
                        StorageHandlingCommandBase.UpVdssInCluster, cluster.getname()), SearchType.VDS);
                p.setMaxCount(Integer.MAX_VALUE);

                Iterable<IVdcQueryable> clusterUpVdss = (Iterable<IVdcQueryable>) Backend.getInstance()
                        .runInternalQuery(VdcQueryType.Search, p).getReturnValue();

                for (Version supportedVer : Config
                        .<java.util.HashSet<Version>> GetValue(ConfigValues.SupportedClusterLevels)) {
                    // if version lower than current skip because cannot
                    // decrease version
                    if (supportedVer.compareTo(cluster.getcompatibility_version()) < 0) {
                        continue;
                    }

                    boolean versionOk = true;
                    // check all vdss support this ver
                    for (IVdcQueryable vds : clusterUpVdss) {
                        if (!VersionSupport.checkClusterVersionSupported(cluster.getcompatibility_version(),
                                (VDS) vds)) {
                            versionOk = false;
                            break;
                        }
                    }
                    if (versionOk) {
                        result.add(supportedVer);
                    }
                }
            }
            getQueryReturnValue().setReturnValue(result);
        } else {
            getQueryReturnValue().setReturnValue(
                    new java.util.ArrayList<Version>(Config
                            .<java.util.HashSet<Version>> GetValue(ConfigValues.SupportedClusterLevels)));
        }
    }
}
