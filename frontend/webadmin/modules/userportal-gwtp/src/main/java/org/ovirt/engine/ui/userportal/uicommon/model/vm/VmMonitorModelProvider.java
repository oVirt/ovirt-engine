package org.ovirt.engine.ui.userportal.uicommon.model.vm;

import org.ovirt.engine.ui.uicommonweb.models.userportal.UserPortalListModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmMonitorModel;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.IEventListener;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;
import org.ovirt.engine.ui.userportal.gin.ClientGinjector;
import org.ovirt.engine.ui.userportal.uicommon.model.UserPortalDetailModelProvider;
import org.ovirt.engine.ui.userportal.uicommon.model.UserPortalModelResolver;

import com.google.inject.Inject;

public class VmMonitorModelProvider extends UserPortalDetailModelProvider<UserPortalListModel, VmMonitorModel> {

    @Inject
    public VmMonitorModelProvider(ClientGinjector ginjector,
            UserPortalListProvider parentModelProvider,
            UserPortalModelResolver resolver) {
        super(ginjector, parentModelProvider, VmMonitorModel.class, resolver);
    }

    @Override
    protected void onCommonModelChange() {
        super.onCommonModelChange();

        getModel().getPropertyChangedEvent().addListener(new IEventListener() {
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                String propName = ((PropertyChangedEventArgs) args).PropertyName;

                if ("CpuUsage".equals(propName) || "MemoryUsage".equals(propName) || "NetworkUsage".equals(propName)) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                    VmMonitorValueChangeEvent.fire(getEventBus());
                }
            }
        });
    }

}
