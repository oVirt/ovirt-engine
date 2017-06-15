package org.ovirt.engine.ui.common.view;

import org.ovirt.engine.ui.common.presenter.CollapsiblePanelPresenterWidget;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.ToggleButton;
import com.google.gwt.user.client.ui.Widget;

public class CollapsiblePanelView extends AbstractView implements CollapsiblePanelPresenterWidget.ViewDef {

    public interface ViewUiBinder extends UiBinder<Widget, CollapsiblePanelView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    private static final String INDEX = "index"; //$NON-NLS-1$

    @UiField
    FlowPanel headerPanel;

    @UiField
    ToggleButton collapseButton;

    @UiField
    FlowPanel contentPanel;

    int dragIndex;

    /**
     * Constructor.
     */
    public CollapsiblePanelView() {
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
    }

    @Override
    public void addContentWidget(IsWidget widget) {
        contentPanel.add(widget);
    }

    @Override
    public void clearContent() {
        contentPanel.clear();
    }

    @Override
    public void setTitleWidget(IsWidget widget) {
        headerPanel.insert(widget, headerPanel.getWidgetCount() - 1);
    }

    @UiHandler("collapseButton")
    void handleCollapseClick(ClickEvent event) {
        contentPanel.setVisible(!contentPanel.isVisible());
    }

    @Override
    public void collapsePanel() {
        if (contentPanel.isVisible()) {
            collapseButton.setValue(true);
            contentPanel.setVisible(false);
        }
    }
}
