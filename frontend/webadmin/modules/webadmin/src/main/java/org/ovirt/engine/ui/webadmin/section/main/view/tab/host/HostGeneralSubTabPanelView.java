package org.ovirt.engine.ui.webadmin.section.main.view.tab.host;

import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.widget.tab.AbstractTabPanel;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.host.HostGeneralSubTabPanelPresenter;
import org.ovirt.engine.ui.webadmin.section.main.view.AbstractSubTabPanelView;
import org.ovirt.engine.ui.webadmin.widget.tab.HyperlinkTabPanel;
import com.google.gwt.core.client.GWT;

public class HostGeneralSubTabPanelView extends AbstractSubTabPanelView
    implements HostGeneralSubTabPanelPresenter.ViewDef {

    interface ViewIdHandler extends ElementIdHandler<HostGeneralSubTabPanelView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    private final HyperlinkTabPanel tabPanel = new HyperlinkTabPanel();

    public HostGeneralSubTabPanelView() {
        initWidget(getTabPanel());
    }

    @Override
    protected void generateIds() {
        ViewIdHandler.idHandler.generateAndSetIds(this);
    }

    @Override
    protected AbstractTabPanel getTabPanel() {
        return tabPanel;
    }

    @Override
    protected Object getContentSlot() {
        return HostGeneralSubTabPanelPresenter.TYPE_SetTabContent;
    }

}
