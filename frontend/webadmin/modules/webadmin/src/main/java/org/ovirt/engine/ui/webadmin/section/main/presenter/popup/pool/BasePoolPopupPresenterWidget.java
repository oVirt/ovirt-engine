package org.ovirt.engine.ui.webadmin.section.main.presenter.popup.pool;

import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.ui.common.system.ClientStorage;
import org.ovirt.engine.ui.common.widget.popup.AbstractVmBasedPopupPresenterWidget;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.vms.UnitVmModel;
import org.ovirt.engine.ui.webadmin.ApplicationMessages;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;

import com.google.gwt.event.shared.EventBus;

public abstract class BasePoolPopupPresenterWidget<V extends AbstractVmBasedPopupPresenterWidget.ViewDef> extends AbstractVmBasedPopupPresenterWidget<V> {

    private static final ApplicationMessages messages = AssetProvider.getMessages();

    public BasePoolPopupPresenterWidget(EventBus eventBus, V view, ClientStorage clientStorage) {
        super(eventBus, view, clientStorage);
    }

    @Override
    public void init(final UnitVmModel model) {
        super.init(model);

        model.getDataCenterWithClustersList().getSelectedItemChangedEvent().addListener((ev, sender, args) -> {
            if (model.getSelectedCluster() != null) {
                setSpiceProxyOverrideExplanation(model.getSelectedCluster());
            }

        });
    }

    private void setSpiceProxyOverrideExplanation(Cluster selectedCluster) {
        String spiceProxyInConfig =
                (String) AsyncDataProvider.getInstance().getConfigValuePreConverted(ConfigValues.SpiceProxyDefault);
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
