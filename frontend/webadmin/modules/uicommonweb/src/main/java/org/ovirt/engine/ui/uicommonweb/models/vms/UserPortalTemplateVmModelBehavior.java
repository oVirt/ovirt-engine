package org.ovirt.engine.ui.uicommonweb.models.vms;

import java.util.Arrays;

import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;

public class UserPortalTemplateVmModelBehavior extends TemplateVmModelBehavior {

    public UserPortalTemplateVmModelBehavior(VmTemplate template) {
        super(template);
    }

    /**
     * Fills the default host according to the selected host set in webadmin. Since this value can be set only in
     * webadmin and can be set also to host, which is not visible to the user in userportal, this fakes the VDS value in
     * a way, that the rest of the code can use it normally and send it back to the server as-is (like Null Object
     * Pattern).
     */
    @Override
    protected void doChangeDefautlHost(Guid hostGuid) {
        if (hostGuid != null) {
            VDS vds = new VDS();
            vds.setId(hostGuid);
            getModel().getDefaultHost().setItems(Arrays.asList(vds));
        }

        super.doChangeDefautlHost(hostGuid);
    }

    @Override
    protected void getHostListByCluster(VDSGroup cluster, AsyncQuery query) {
        Frontend.RunQuery(
                VdcQueryType.GetHostsByClusterId,
                new IdQueryParameters(cluster.getId()),
                query
                );
    }
}
