package org.ovirt.engine.ui.common.uicommon;

import org.ovirt.engine.core.common.queries.ConsoleOptionsParams;
import org.ovirt.engine.core.common.queries.QueryReturnValue;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.uicommonweb.models.vms.ConsoleModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.IVncNative;

public class VncNativeImpl extends AbstractVnc implements IVncNative {

    @Override
    public void invokeClient() {
        // todo avoid code duplication with spice
        AsyncQuery<QueryReturnValue> callback = new AsyncQuery<>(returnValue ->
                ConsoleModel.makeConsoleConfigRequest("console.vv", //$NON-NLS-1$
                "application/x-virt-viewer; charset=UTF-8", //$NON-NLS-1$
                returnValue.getReturnValue()));
        Frontend.getInstance().runQuery(
                QueryType.GetConsoleDescriptorFile,
                new ConsoleOptionsParams(getOptions()), callback);
    }
}
