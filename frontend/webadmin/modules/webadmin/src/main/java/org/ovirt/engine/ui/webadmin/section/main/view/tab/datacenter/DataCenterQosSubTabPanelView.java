package org.ovirt.engine.ui.webadmin.section.main.view.tab.datacenter;

import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.widget.tab.AbstractTabPanel;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.datacenter.DataCenterQosSubTabPanelPresenter;
import org.ovirt.engine.ui.webadmin.section.main.view.AbstractSubTabPanelView;
import org.ovirt.engine.ui.webadmin.widget.tab.HyperlinkTabPanel;
import com.google.gwt.core.client.GWT;

public class DataCenterQosSubTabPanelView extends AbstractSubTabPanelView implements DataCenterQosSubTabPanelPresenter.ViewDef {

    interface ViewIdHandler extends ElementIdHandler<DataCenterQosSubTabPanelView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    private final HyperlinkTabPanel tabPanel = new HyperlinkTabPanel();

    public DataCenterQosSubTabPanelView() {
        initWidget(getTabPanel());
    }

    @Override
    protected void generateIds() {
        ViewIdHandler.idHandler.generateAndSetIds(this);
    }

    @Override
    protected Object getContentSlot() {
        return DataCenterQosSubTabPanelPresenter.TYPE_SetTabContent;
    }

    @Override
    protected AbstractTabPanel getTabPanel() {
        return tabPanel;
    }
}
