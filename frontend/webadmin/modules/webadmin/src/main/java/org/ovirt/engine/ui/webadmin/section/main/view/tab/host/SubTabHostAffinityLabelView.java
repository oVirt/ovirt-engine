package org.ovirt.engine.ui.webadmin.section.main.view.tab.host;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.Label;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.uicommonweb.models.configure.labels.list.HostAffinityLabelListModel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.HostListModel;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.host.SubTabHostAffinityLabelPresenter;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.AbstractSubTabAffinityLabelsView;

import com.google.gwt.core.client.GWT;

public class SubTabHostAffinityLabelView extends AbstractSubTabAffinityLabelsView<VDS, HostListModel<Void>, HostAffinityLabelListModel>
        implements SubTabHostAffinityLabelPresenter.ViewDef {

    interface ViewIdHandler extends ElementIdHandler<SubTabHostAffinityLabelView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    @Inject
    public SubTabHostAffinityLabelView(SearchableDetailModelProvider<Label, HostListModel<Void>, HostAffinityLabelListModel> modelProvider) {
        super(modelProvider);
    }

    @Override
    protected void generateIds() {
        ViewIdHandler.idHandler.generateAndSetIds(this);
    }
}
