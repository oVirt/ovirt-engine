package org.ovirt.engine.ui.webadmin.section.main.presenter.popup;

import org.ovirt.engine.core.common.businessentities.Erratum;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.uicommonweb.models.VmErrataListModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmListModel;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

public class VmErrataListWithDetailsPopupPresenterWidget
    extends ErrataListWithDetailsPopupPresenterWidget<SearchableDetailModelProvider<Erratum,
        VmListModel<Void>, VmErrataListModel>> {

    public interface ViewDef extends ErrataListWithDetailsPopupPresenterWidget.ViewDef {};

    @Inject
    public VmErrataListWithDetailsPopupPresenterWidget(EventBus eventBus,
            VmErrataListWithDetailsPopupPresenterWidget.ViewDef view,
            SearchableDetailModelProvider<Erratum, VmListModel<Void>, VmErrataListModel> modelProvider) {
        super(eventBus, view, modelProvider);
    }
}
