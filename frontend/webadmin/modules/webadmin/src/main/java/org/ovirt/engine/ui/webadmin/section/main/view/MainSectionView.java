package org.ovirt.engine.ui.webadmin.section.main.view;

import javax.inject.Inject;

import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.view.AbstractView;
import org.ovirt.engine.ui.webadmin.section.main.presenter.MainSectionPresenter;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.SimplePanel;

public class MainSectionView extends AbstractView implements MainSectionPresenter.ViewDef {

    interface ViewUiBinder extends UiBinder<FlowPanel, MainSectionView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    interface ViewIdHandler extends ElementIdHandler<MainSectionView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    @UiField
    SimplePanel headerPanel;

    @UiField
    SimplePanel menuPanel;

    @UiField
    SimplePanel notificationsPanel;

    @UiField
    FlowPanel mainContentPanel;

    @Inject
    public MainSectionView() {
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        ViewIdHandler.idHandler.generateAndSetIds(this);
    }

    @Override
    public void onAttach() {
        super.onAttach();
        Scheduler.get().scheduleDeferred(() -> {
            // At this point I know both nav and content divs are attached, so I can call JSNI
            activateMenus();
        });
    }

    @Override
    public void setInSlot(Object slot, IsWidget content) {
        if (slot == MainSectionPresenter.TYPE_SetHeader) {
            setPanelContent(headerPanel, content);
        } else if (slot == MainSectionPresenter.TYPE_SetMainContent) {
            setPanelContent(mainContentPanel, content);
        } else if (slot == MainSectionPresenter.TYPE_SetMenu) {
            setPanelContent(menuPanel, content);
        } else if (slot == MainSectionPresenter.TYPE_SetNotifications) {
            setPanelContent(notificationsPanel, content);
        } else {
            super.setInSlot(slot, content);
        }
    }

    private native void activateMenus() /*-{
        $wnd.jQuery().setupVerticalNavigation(true);
    }-*/;

}
