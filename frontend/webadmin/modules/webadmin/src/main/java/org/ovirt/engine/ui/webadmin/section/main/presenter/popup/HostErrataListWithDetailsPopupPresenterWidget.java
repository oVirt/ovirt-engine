package org.ovirt.engine.ui.webadmin.section.main.presenter.popup;

import org.ovirt.engine.core.common.businessentities.Erratum;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.uicommonweb.models.HostErrataListModel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.HostListModel;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

public class HostErrataListWithDetailsPopupPresenterWidget
    extends ErrataListWithDetailsPopupPresenterWidget<SearchableDetailModelProvider<Erratum,
                HostListModel<Void>, HostErrataListModel>> {

    public interface ViewDef extends ErrataListWithDetailsPopupPresenterWidget.ViewDef {};

    @Inject
    public HostErrataListWithDetailsPopupPresenterWidget(EventBus eventBus,
            HostErrataListWithDetailsPopupPresenterWidget.ViewDef view,
            SearchableDetailModelProvider<Erratum, HostListModel<Void>,
            HostErrataListModel> modelProvider) {
        super(eventBus, view, modelProvider);
    }
}
