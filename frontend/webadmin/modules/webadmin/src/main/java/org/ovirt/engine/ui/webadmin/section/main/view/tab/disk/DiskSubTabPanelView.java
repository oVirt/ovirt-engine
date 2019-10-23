package org.ovirt.engine.ui.webadmin.section.main.view.tab.disk;

import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.presenter.ActionPanelPresenterWidget;
import org.ovirt.engine.ui.common.presenter.DisksBreadCrumbsPresenterWidget;
import org.ovirt.engine.ui.common.widget.tab.AbstractTabPanel;
import org.ovirt.engine.ui.common.widget.tab.DetailTabLayout;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.DiskActionPanelPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.disk.DiskSubTabPanelPresenter;
import org.ovirt.engine.ui.webadmin.section.main.view.AbstractSubTabPanelView;
import org.ovirt.engine.ui.webadmin.widget.tab.SimpleTabPanel;

import com.google.gwt.core.client.GWT;
import com.google.inject.Inject;

public class DiskSubTabPanelView extends AbstractSubTabPanelView implements DiskSubTabPanelPresenter.ViewDef {
    interface ViewIdHandler extends ElementIdHandler<DiskSubTabPanelView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    private final DiskActionPanelPresenterWidget actionPanel;
    private final SimpleTabPanel tabPanel;

    @Inject
    public DiskSubTabPanelView(DisksBreadCrumbsPresenterWidget breadCrumbs,
            DiskActionPanelPresenterWidget actionPanel, DetailTabLayout detailTabLayout) {
        this.actionPanel = actionPanel;
        this.tabPanel = new SimpleTabPanel(breadCrumbs, actionPanel, detailTabLayout);
        initWidget(getTabPanel());
        actionPanel.removeButton(actionPanel.getNewButtonDefinition());
    }

    @Override
    protected void generateIds() {
        ViewIdHandler.idHandler.generateAndSetIds(this);
    }

    @Override
    protected Object getContentSlot() {
        return DiskSubTabPanelPresenter.TYPE_SetTabContent;
    }

    @Override
    protected AbstractTabPanel getTabPanel() {
        return tabPanel;
    }

    @Override
    public ActionPanelPresenterWidget<?, ?, ?> getActionPanelPresenterWidget() {
        return actionPanel;
    }

}
