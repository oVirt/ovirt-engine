package org.ovirt.engine.ui.webadmin.section.main.view.tab.errata;

import org.ovirt.engine.core.common.businessentities.Erratum;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.widget.OvirtBreadCrumbs;
import org.ovirt.engine.ui.common.widget.tab.AbstractTabPanel;
import org.ovirt.engine.ui.common.widget.tab.DetailTabLayout;
import org.ovirt.engine.ui.uicommonweb.models.EngineErrataListModel;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.errata.ErrataSubTabPanelPresenter;
import org.ovirt.engine.ui.webadmin.section.main.view.AbstractSubTabPanelView;
import org.ovirt.engine.ui.webadmin.widget.tab.SimpleTabPanel;

import com.google.gwt.core.client.GWT;
import com.google.inject.Inject;

public class ErrataSubTabPanelView extends AbstractSubTabPanelView implements ErrataSubTabPanelPresenter.ViewDef {
    interface ViewIdHandler extends ElementIdHandler<ErrataSubTabPanelView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    private final SimpleTabPanel tabPanel;

    @Inject
    public ErrataSubTabPanelView(OvirtBreadCrumbs<Erratum, EngineErrataListModel> breadCrumbs, DetailTabLayout detailTabLayout) {
        tabPanel = new SimpleTabPanel(breadCrumbs, detailTabLayout);
        initWidget(getTabPanel());
    }

    @Override
    protected void generateIds() {
        ViewIdHandler.idHandler.generateAndSetIds(this);
    }

    @Override
    protected Object getContentSlot() {
        return ErrataSubTabPanelPresenter.TYPE_SetTabContent;
    }

    @Override
    protected AbstractTabPanel getTabPanel() {
        return tabPanel;
    }

}
