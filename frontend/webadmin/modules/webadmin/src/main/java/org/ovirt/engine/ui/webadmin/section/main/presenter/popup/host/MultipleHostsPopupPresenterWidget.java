package org.ovirt.engine.ui.webadmin.section.main.presenter.popup.host;

import org.ovirt.engine.ui.common.presenter.AbstractModelBoundPopupPresenterWidget;
import org.ovirt.engine.ui.uicommonweb.models.hosts.MultipleHostsModel;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

public class MultipleHostsPopupPresenterWidget extends AbstractModelBoundPopupPresenterWidget<MultipleHostsModel, MultipleHostsPopupPresenterWidget.ViewDef> {

    public interface ViewDef extends AbstractModelBoundPopupPresenterWidget.ViewDef<MultipleHostsModel> {

    }

    @Inject
    public MultipleHostsPopupPresenterWidget(EventBus eventBus, ViewDef view) {
        super(eventBus, view);
    }

}
