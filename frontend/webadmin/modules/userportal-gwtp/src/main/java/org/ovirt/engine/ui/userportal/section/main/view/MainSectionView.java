package org.ovirt.engine.ui.userportal.section.main.view;

import org.ovirt.engine.ui.common.view.AbstractView;
import org.ovirt.engine.ui.userportal.section.main.presenter.MainSectionPresenter;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

public class MainSectionView extends AbstractView implements MainSectionPresenter.ViewDef {

    interface ViewUiBinder extends UiBinder<Widget, MainSectionView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    @UiField
    DockLayoutPanel wrapperLayoutPanel;

    @UiField
    SimplePanel headerPanel;

    @UiField
    LayoutPanel mainContentPanel;

    public MainSectionView() {
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
    }

    @Override
    public void setInSlot(Object slot, IsWidget content) {
        if (slot == MainSectionPresenter.TYPE_SetHeader) {
            setPanelContent(headerPanel, content);
        } else if (slot == MainSectionPresenter.TYPE_SetMainContent) {
            setPanelContent(mainContentPanel, content);
        } else {
            super.setInSlot(slot, content);
        }
    }

    public void setHeaderPanelHeight(int height) {
        wrapperLayoutPanel.setWidgetSize(headerPanel, height);
    }
}
