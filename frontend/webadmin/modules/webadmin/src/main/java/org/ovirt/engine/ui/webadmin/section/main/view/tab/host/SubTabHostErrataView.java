package org.ovirt.engine.ui.webadmin.section.main.view.tab.host;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.ui.common.uicommon.model.DetailTabModelProvider;
import org.ovirt.engine.ui.uicommonweb.models.HostErrataCountModel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.HostListModel;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.host.SubTabHostErrataPresenter;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.AbstractSubTabErrataCountView;

/**
 * View for the sub tab that shows errata counts for a VM selected in the main tab.
 */
public class SubTabHostErrataView extends AbstractSubTabErrataCountView<VDS, HostListModel<Void>, HostErrataCountModel>
    implements SubTabHostErrataPresenter.ViewDef {

    @Inject
    public SubTabHostErrataView(
            DetailTabModelProvider<HostListModel<Void>, HostErrataCountModel> modelProvider) {
        super(modelProvider);
    }

}
