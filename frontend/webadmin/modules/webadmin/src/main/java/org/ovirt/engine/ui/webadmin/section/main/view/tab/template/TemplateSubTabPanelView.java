package org.ovirt.engine.ui.webadmin.section.main.view.tab.template;

import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.presenter.ActionPanelPresenterWidget;
import org.ovirt.engine.ui.common.presenter.TemplateBreadCrumbsPresenterWidget;
import org.ovirt.engine.ui.common.widget.tab.AbstractTabPanel;
import org.ovirt.engine.ui.common.widget.tab.DetailTabLayout;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.TemplateActionPanelPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.template.TemplateSubTabPanelPresenter;
import org.ovirt.engine.ui.webadmin.section.main.view.AbstractSubTabPanelView;
import org.ovirt.engine.ui.webadmin.widget.tab.SimpleTabPanel;

import com.google.gwt.core.client.GWT;
import com.google.inject.Inject;

public class TemplateSubTabPanelView extends AbstractSubTabPanelView implements TemplateSubTabPanelPresenter.ViewDef {

    interface ViewIdHandler extends ElementIdHandler<TemplateSubTabPanelView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    private final TemplateActionPanelPresenterWidget actionPanel;
    private final SimpleTabPanel tabPanel;

    @Inject
    public TemplateSubTabPanelView(TemplateBreadCrumbsPresenterWidget breadCrumbs,
            TemplateActionPanelPresenterWidget actionPanel, DetailTabLayout detailTabLayout) {
        this.actionPanel = actionPanel;
        this.tabPanel = new SimpleTabPanel(breadCrumbs, actionPanel, detailTabLayout);
        initWidget(getTabPanel());
    }

    @Override
    protected void generateIds() {
        ViewIdHandler.idHandler.generateAndSetIds(this);
    }

    @Override
    protected Object getContentSlot() {
        return TemplateSubTabPanelPresenter.TYPE_SetTabContent;
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
