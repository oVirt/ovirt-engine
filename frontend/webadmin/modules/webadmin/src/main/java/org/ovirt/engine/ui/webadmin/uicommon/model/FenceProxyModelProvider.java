package org.ovirt.engine.ui.webadmin.uicommon.model;

import org.ovirt.engine.ui.common.presenter.popup.DefaultConfirmationPopupPresenterWidget;
import org.ovirt.engine.ui.common.uicommon.model.TabModelProvider;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicommonweb.models.hosts.FenceProxyModel;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.host.HostFenceProxyPopupPresenterWidget;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;
import com.google.inject.Provider;

public class FenceProxyModelProvider extends TabModelProvider<FenceProxyModel> {
    final Provider<HostFenceProxyPopupPresenterWidget> fenceProxyPopupPresenterProvider;

    @Inject
    public FenceProxyModelProvider(EventBus eventBus,
            Provider<DefaultConfirmationPopupPresenterWidget> defaultConfirmPopupProvider,
            Provider<HostFenceProxyPopupPresenterWidget> fenceProxyPopupPresenterProvider) {
        super(eventBus, defaultConfirmPopupProvider);
        this.fenceProxyPopupPresenterProvider = fenceProxyPopupPresenterProvider;
    }

    public void initializeModel(FenceProxyModel model) {
        if (!model.isInitialized()) {
            model.setInitialized();
            initializeModelHandlers(model);
        }
    }

    public HostFenceProxyPopupPresenterWidget getModelPopup(FenceProxyModel source,
            UICommand lastExecutedCommand, Model windowModel) {
        return fenceProxyPopupPresenterProvider.get();
    }
}
