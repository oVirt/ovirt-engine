package org.ovirt.engine.ui.userportal.section.main.view.tab.basic;

import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.idhandler.HasElementId;
import org.ovirt.engine.ui.common.view.AbstractView;
import org.ovirt.engine.ui.userportal.section.main.presenter.tab.basic.MainTabBasicListPresenterWidget;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class MainTabBasicListView extends AbstractView implements MainTabBasicListPresenterWidget.ViewDef, HasElementId {

    interface ViewUiBinder extends UiBinder<Widget, MainTabBasicListView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    interface ViewIdHandler extends ElementIdHandler<MainTabBasicListView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    @UiField
    FlowPanel vmPanel;

    private String elementId = DOM.createUniqueId();

    @Inject
    public MainTabBasicListView() {
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        ViewIdHandler.idHandler.generateAndSetIds(this);
    }

    @Override
    public void addToSlot(Object slot, IsWidget content) {
        if (slot == MainTabBasicListPresenterWidget.TYPE_VmListContent) {
            vmPanel.add(content);
        } else {
            super.addToSlot(slot, content);
        }
    }

    @Override
    public void setInSlot(Object slot, IsWidget content) {
        if (slot == MainTabBasicListPresenterWidget.TYPE_VmListContent) {
            vmPanel.clear();
            if (content != null) {
                vmPanel.add(content);
            }
        } else {
            super.setInSlot(slot, content);
        }
    }

    @Override
    public String getElementId() {
        return elementId;
    }

    @Override
    public void setElementId(String elementId) {
        this.elementId = elementId;
    }

}
