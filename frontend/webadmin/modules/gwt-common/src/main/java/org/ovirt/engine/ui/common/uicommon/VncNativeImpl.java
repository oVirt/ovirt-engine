package org.ovirt.engine.ui.common.uicommon;

import org.ovirt.engine.core.common.queries.ConsoleOptionsParams;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.models.vms.ConsoleModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.IVncNative;

public class VncNativeImpl extends AbstractVnc implements IVncNative {

    @Override
    public void invokeClient() {
        AsyncQuery callback = new AsyncQuery(this, new INewAsyncCallback() {
            @Override
            public void onSuccess(Object model, Object returnValue) { // todo avoid code duplication with spice
                StringBuilder configBuilder = new StringBuilder((String) ((VdcQueryReturnValue) returnValue).getReturnValue());
                writeOVirtSection(configBuilder, getOptions());
                ConsoleModel.makeConsoleConfigRequest("console.vv", //$NON-NLS-1$
                        "application/x-virt-viewer; charset=UTF-8", //$NON-NLS-1$
                        configBuilder.toString());
            }
        });
        Frontend.getInstance().runQuery(
                VdcQueryType.GetConsoleDescriptorFile,
                new ConsoleOptionsParams(getOptions()), callback);
    }
}
