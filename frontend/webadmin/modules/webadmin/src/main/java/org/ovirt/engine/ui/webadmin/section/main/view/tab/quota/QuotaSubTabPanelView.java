package org.ovirt.engine.ui.webadmin.section.main.view.tab.quota;

import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.presenter.ActionPanelPresenterWidget;
import org.ovirt.engine.ui.common.presenter.QuotaBreadCrumbsPresenterWidget;
import org.ovirt.engine.ui.common.widget.tab.AbstractTabPanel;
import org.ovirt.engine.ui.common.widget.tab.DetailTabLayout;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.QuotaActionPanelPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.quota.QuotaSubTabPanelPresenter;
import org.ovirt.engine.ui.webadmin.section.main.view.AbstractSubTabPanelView;
import org.ovirt.engine.ui.webadmin.widget.tab.SimpleTabPanel;

import com.google.gwt.core.client.GWT;
import com.google.inject.Inject;

public class QuotaSubTabPanelView extends AbstractSubTabPanelView implements QuotaSubTabPanelPresenter.ViewDef {

    interface ViewIdHandler extends ElementIdHandler<QuotaSubTabPanelView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    private final QuotaActionPanelPresenterWidget<Void> actionPanel;
    private final SimpleTabPanel tabPanel;

    @Inject
    public QuotaSubTabPanelView(QuotaBreadCrumbsPresenterWidget breadCrumbs,
            QuotaActionPanelPresenterWidget<Void> actionPanel, DetailTabLayout detailTabLayout) {
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
        return QuotaSubTabPanelPresenter.TYPE_SetTabContent;
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
