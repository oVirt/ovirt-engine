package org.ovirt.engine.ui.webadmin.uicommon.model;

import org.ovirt.engine.ui.common.presenter.popup.DefaultConfirmationPopupPresenterWidget;
import org.ovirt.engine.ui.common.uicommon.model.TabModelProvider;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicommonweb.models.hosts.FenceAgentModel;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.host.HostFenceAgentPopupPresenterWidget;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;
import com.google.inject.Provider;

public class FenceAgentModelProvider extends TabModelProvider<FenceAgentModel> {
    final Provider<HostFenceAgentPopupPresenterWidget> fenceAgentPopupProvider;

    @Inject
    public FenceAgentModelProvider(EventBus eventBus,
            Provider<DefaultConfirmationPopupPresenterWidget> defaultConfirmPopupProvider,
            Provider<HostFenceAgentPopupPresenterWidget> fenceAgentPopupProvider) {
        super(eventBus, defaultConfirmPopupProvider);
        this.fenceAgentPopupProvider = fenceAgentPopupProvider;
    }

    public void initializeModel(FenceAgentModel model) {
        if (!model.isInitialized()) {
            model.setInitialized();
            initializeModelHandlers(model);
        }
    }

    @Override
    public HostFenceAgentPopupPresenterWidget getModelPopup(FenceAgentModel source,
            UICommand lastExecutedCommand, Model windowModel) {
        return fenceAgentPopupProvider.get();
    }
}
