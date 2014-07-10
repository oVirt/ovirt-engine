package org.ovirt.engine.ui.webadmin.section.main.presenter.popup.pool;

import com.google.gwt.event.shared.EventBus;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.queries.ConfigurationValues;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.ui.common.CommonApplicationMessages;
import org.ovirt.engine.ui.common.system.ClientStorage;
import org.ovirt.engine.ui.common.widget.popup.AbstractVmBasedPopupPresenterWidget;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.vms.UnitVmModel;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.IEventListener;

public abstract class BasePoolPopupPresenterWidget<V extends AbstractVmBasedPopupPresenterWidget.ViewDef> extends AbstractVmBasedPopupPresenterWidget<V> {

    private CommonApplicationMessages messages;

    public BasePoolPopupPresenterWidget(EventBus eventBus, V view, CommonApplicationMessages messages, ClientStorage clientStorage) {
        super(eventBus, view, clientStorage);

        this.messages = messages;
    }

    @Override
    public void init(final UnitVmModel model) {
        super.init(model);

        model.getDataCenterWithClustersList().getSelectedItemChangedEvent().addListener(new IEventListener() {
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                if (model.getSelectedCluster() != null) {
                    setSpiceProxyOverrideExplanation(model.getSelectedCluster());
                }

            }
        });
    }

    private void setSpiceProxyOverrideExplanation(VDSGroup selectedCluster) {
        String spiceProxyInConfig =
                (String) AsyncDataProvider.getInstance().getConfigValuePreConverted(ConfigurationValues.SpiceProxyDefault);
        String spiceProxyOnCluster = selectedCluster.getSpiceProxy();

        if (!StringHelper.isNullOrEmpty(spiceProxyOnCluster)) {
            getView().setSpiceProxyOverrideExplanation(messages.consoleOverrideSpiceProxyMessage(messages.consoleOverrideDefinedOnCluster(),
                    spiceProxyOnCluster));
        } else if (!StringHelper.isNullOrEmpty(spiceProxyInConfig)) {
            getView().setSpiceProxyOverrideExplanation(messages.consoleOverrideSpiceProxyMessage(messages.consoleOverrideDefinedInGlobalConfig(),
                    spiceProxyInConfig));
        } else {
            getView().setSpiceProxyOverrideExplanation(messages.consoleOverrideSpiceProxyMessage(
                    messages.or(
                            messages.consoleOverrideDefinedInGlobalConfig(),
                            messages.consoleOverrideDefinedOnCluster()
                            ),

                    messages.noSpiceProxyDefined()
                    ));
        }
    }
}
