package org.ovirt.engine.ui.webadmin.section.main.presenter.popup.host;

import org.ovirt.engine.ui.common.presenter.AbstractTabbedModelBoundPopupPresenterWidget;
import org.ovirt.engine.ui.uicommonweb.models.hosts.ConfigureLocalStorageModel;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

public class ConfigureLocalStoragePopupPresenterWidget extends
    AbstractTabbedModelBoundPopupPresenterWidget<ConfigureLocalStorageModel, ConfigureLocalStoragePopupPresenterWidget.ViewDef> {

    public interface ViewDef extends AbstractTabbedModelBoundPopupPresenterWidget.ViewDef<ConfigureLocalStorageModel> {

    }

    @Inject
    public ConfigureLocalStoragePopupPresenterWidget(EventBus eventBus, ViewDef view) {
        super(eventBus, view);
    }
}
