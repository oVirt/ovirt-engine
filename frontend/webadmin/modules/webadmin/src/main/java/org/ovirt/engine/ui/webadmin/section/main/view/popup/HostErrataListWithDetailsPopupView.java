package org.ovirt.engine.ui.webadmin.section.main.view.popup;

import org.ovirt.engine.ui.uicommonweb.models.HostErrataListModel;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.HostErrataListWithDetailsPopupPresenterWidget;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

public class HostErrataListWithDetailsPopupView extends ErrataListWithDetailsPopupView
    implements HostErrataListWithDetailsPopupPresenterWidget.ViewDef {

    @Inject
    public HostErrataListWithDetailsPopupView(EventBus eventBus, HostErrataListModel listModel) {
        super(eventBus, listModel);
    }
}
