package org.ovirt.engine.ui.userportal.uicommon.model.vm;

import org.ovirt.engine.ui.common.presenter.popup.DefaultConfirmationPopupPresenterWidget;
import org.ovirt.engine.ui.uicommonweb.models.userportal.UserPortalListModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmMonitorModel;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.IEventListener;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;
import org.ovirt.engine.ui.userportal.uicommon.model.UserPortalDetailModelProvider;
import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;
import com.google.inject.Provider;

public class VmMonitorModelProvider extends UserPortalDetailModelProvider<UserPortalListModel, VmMonitorModel> {

    @Inject
    public VmMonitorModelProvider(EventBus eventBus,
            Provider<DefaultConfirmationPopupPresenterWidget> defaultConfirmPopupProvider) {
        super(eventBus, defaultConfirmPopupProvider);
    }

    @Override
    protected void initializeModelHandlers(VmMonitorModel model) {
        super.initializeModelHandlers(model);

        model.getPropertyChangedEvent().addListener(new IEventListener<PropertyChangedEventArgs>() {
            @Override
            public void eventRaised(Event<? extends PropertyChangedEventArgs> ev, Object sender, PropertyChangedEventArgs args) {
                String propName = args.propertyName;

                if ("CpuUsage".equals(propName) || "MemoryUsage".equals(propName) || "NetworkUsage".equals(propName)) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                    VmMonitorValueChangeEvent.fire(getEventBus());
                }
            }
        });
    }

}
