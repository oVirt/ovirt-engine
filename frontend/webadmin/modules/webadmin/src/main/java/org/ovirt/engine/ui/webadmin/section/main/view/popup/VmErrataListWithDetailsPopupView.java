package org.ovirt.engine.ui.webadmin.section.main.view.popup;

import org.ovirt.engine.ui.uicommonweb.models.VmErrataListModel;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.VmErrataListWithDetailsPopupPresenterWidget;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

public class VmErrataListWithDetailsPopupView extends ErrataListWithDetailsPopupView
    implements VmErrataListWithDetailsPopupPresenterWidget.ViewDef {

    @Inject
    public VmErrataListWithDetailsPopupView(EventBus eventBus, VmErrataListModel listModel) {
        super(eventBus, listModel);
    }

}
