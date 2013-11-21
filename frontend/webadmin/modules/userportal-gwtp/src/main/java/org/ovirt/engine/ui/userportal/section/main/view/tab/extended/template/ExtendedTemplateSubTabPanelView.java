package org.ovirt.engine.ui.userportal.section.main.view.tab.extended.template;

import org.ovirt.engine.ui.common.widget.tab.AbstractTabPanel;
import org.ovirt.engine.ui.userportal.section.main.presenter.tab.extended.template.ExtendedTemplateSubTabPanelPresenter;
import org.ovirt.engine.ui.userportal.section.main.view.AbstractSubTabPanelView;
import org.ovirt.engine.ui.userportal.widget.tab.SimpleTabPanel;

public class ExtendedTemplateSubTabPanelView extends AbstractSubTabPanelView implements
    ExtendedTemplateSubTabPanelPresenter.ViewDef {

    private final SimpleTabPanel tabPanel = new SimpleTabPanel();

    public ExtendedTemplateSubTabPanelView() {
        initWidget(getTabPanel());
    }

    @Override
    protected Object getContentSlot() {
        return ExtendedTemplateSubTabPanelPresenter.TYPE_SetTabContent;
    }

    @Override
    protected AbstractTabPanel getTabPanel() {
        return tabPanel;
    }

}
