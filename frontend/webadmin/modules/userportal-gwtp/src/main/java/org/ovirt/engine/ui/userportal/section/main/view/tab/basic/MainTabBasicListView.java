package org.ovirt.engine.ui.userportal.section.main.view.tab.basic;

import org.ovirt.engine.ui.common.view.AbstractView;
import org.ovirt.engine.ui.userportal.section.main.presenter.tab.basic.MainTabBasicListPresenterWidget;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class MainTabBasicListView extends AbstractView implements MainTabBasicListPresenterWidget.ViewDef {

    @UiField
    FlowPanel vmPanel;

    interface ViewUiBinder extends UiBinder<Widget, MainTabBasicListView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    @Inject
    public MainTabBasicListView() {
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
    }

    @Override
    public void addToSlot(Object slot, Widget content) {
        if (slot == MainTabBasicListPresenterWidget.TYPE_VmListContent) {
            vmPanel.add(content);
        } else {
            super.addToSlot(slot, content);
        }
    }

    @Override
    public void clear() {
        vmPanel.clear();
    }
}
