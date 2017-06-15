package org.ovirt.engine.ui.webadmin.section.main.view.popup.scheduling.panels;

import org.ovirt.engine.core.common.scheduling.PolicyUnit;
import org.ovirt.engine.ui.common.widget.MenuBar;
import org.ovirt.engine.ui.common.widget.PopupPanel;
import org.ovirt.engine.ui.uicommonweb.models.configure.scheduling.NewClusterPolicyModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.scheduling.ClusterPolicyPopupView.WidgetStyle;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.FontWeight;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ContextMenuEvent;
import com.google.gwt.event.dom.client.DragDropEventBase;
import com.google.gwt.event.dom.client.DragStartEvent;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

public class PolicyUnitPanel extends FocusPanel {
    public static final String FILTER = "Filter"; //$NON-NLS-1$
    static final PopupPanel menuPopup = new PopupPanel(true);
    protected final PolicyUnit policyUnit;
    protected boolean used;
    protected boolean locked;
    protected final NewClusterPolicyModel model;
    protected int position;
    WidgetStyle style;

    private static String lastDragData = ""; //$NON-NLS-1$

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    public PolicyUnitPanel(PolicyUnit policyUnit,
            NewClusterPolicyModel model,
            final boolean used,
            boolean locked,
            WidgetStyle style) {
        this.policyUnit = policyUnit;
        this.model = model;
        this.used = used;
        this.locked = locked;
        this.style = style;
        getElement().setTitle(policyUnit.getDescription());
        if (!locked && (policyUnit.isEnabled() || used)) {
            addDomHandler(event -> {
                PolicyUnitPanel sourcePanel = (PolicyUnitPanel) event.getSource();
                NativeEvent nativeEvent = event.getNativeEvent();
                showContextMenu(sourcePanel, nativeEvent.getClientX(), nativeEvent.getClientY());
                event.stopPropagation();
                event.preventDefault();
            }, ContextMenuEvent.getType());
            // enable d&d
            getElement().setDraggable(Element.DRAGGABLE_TRUE);
            // drag start
            addBitlessDomHandler(event -> {
                PolicyUnitPanel sourcePanel = (PolicyUnitPanel) event.getSource();
                lastDragData = getType() + " " + sourcePanel.policyUnit.getId() + " " + Boolean.toString(used); //$NON-NLS-1$ //$NON-NLS-2$
                event.setData("Text", lastDragData); //$NON-NLS-1$
                // show a ghost of the widget under cursor.
                NativeEvent nativeEvent = event.getNativeEvent();
                int x = nativeEvent.getClientX() - sourcePanel.getWidget().getAbsoluteLeft();
                int y = nativeEvent.getClientY() - sourcePanel.getWidget().getAbsoluteTop();
                event.getDataTransfer().setDragImage(sourcePanel.getWidget().getElement(), x, y);
            }, DragStartEvent.getType());
        }
    }

    public void initWidget() {
        FlowPanel namePanel = getNamePanel(policyUnit);
        if (!used) {
            namePanel.setStyleName(style.unusedPolicyUnitStyle());
        } else {
            namePanel.setStyleName(style.usedFilterPolicyUnitStyle());
            if (position != 0) {
                Label label = new Label();
                label.setStyleName(style.positionLabelStyle());
                namePanel.add(label);
                String labelText = null;
                if (position <= -1) {
                    labelText = constants.firstFilter();
                } else {
                    labelText = constants.lastFilter();
                }
                label.setText(labelText);
                Widget text = namePanel.getWidget(0);
                text.getElement().getStyle().setFloat(Style.Float.LEFT);
                text.getElement().getStyle().setPaddingRight(5, Unit.PX);
            }
        }
        if (!policyUnit.isEnabled()) {
            namePanel.getElement().getStyle().setOpacity(0.5);
        }
        setWidget(namePanel);
    }

    protected FlowPanel getNamePanel(PolicyUnit policyUnit) {
        FlowPanel panel = new FlowPanel();
        Label label = new Label(policyUnit.getName());
        if (!policyUnit.isInternal()) {
            Label extLabel = new Label(constants.externalPolicyUnitLabel());
            extLabel.getElement().getStyle().setFontWeight(FontWeight.BOLD);
            extLabel.getElement().getStyle().setPaddingLeft(5, Unit.PX);
            extLabel.getElement().getStyle().setPaddingRight(5, Unit.PX);
            extLabel.getElement().getStyle().setFloat(Style.Float.LEFT);
            panel.add(extLabel);
        }
        panel.add(label);

        if (!policyUnit.isEnabled()) {
            Label disabledLabel = new Label(constants.disabledPolicyUnit());
            disabledLabel.getElement().getStyle().setFontWeight(FontWeight.BOLD);
            disabledLabel.getElement().getStyle().setPaddingLeft(5, Unit.PX);
            panel.add(disabledLabel);
        }
        return panel;
    }

    protected void showContextMenu(PolicyUnitPanel sourcePanel, int clientX, int clientY) {
        MenuBar menuBar = createMenu();
        fillMenuBar(menuBar);
        if (policyUnit.isEnabled()) {
            addSubMenu(menuBar);
        }
        menuPopup.setWidget(menuBar);
        menuPopup.setPopupPosition(clientX, clientY);
        menuPopup.show();
    }

    protected void fillMenuBar(MenuBar menuBar) {
        if (used) {
            menuBar.addItem(constants.removeFilter(), () -> {
                model.removeFilter(policyUnit);
                menuPopup.hide();
            });
        } else {
            menuBar.addItem(constants.addFilter(), () -> {
                model.addFilter(policyUnit, used, 0);
                menuPopup.hide();
            });
        }
    }

    protected void addSubMenu(MenuBar menuBar) {
        MenuBar subMenu = new MenuBar(true);
        if (position != 0) {
            subMenu.addItem(constants.noPositionFilter(), () -> {
                model.addFilter(policyUnit, used, 0);
                menuPopup.hide();
            });
        }
        if (position >= 0) {
            subMenu.addItem(constants.firstFilter(), () -> {
                model.addFilter(policyUnit, used, -1);
                menuPopup.hide();
            });
        }
        if (position <= 0) {
            subMenu.addItem(constants.lastFilter(), () -> {
                model.addFilter(policyUnit, used, 1);
                menuPopup.hide();
            });
        }
        menuBar.addItem(constants.position(), subMenu);
    }

    protected MenuBar createMenu() {
        MenuBar menuBar = new MenuBar(true);
        menuBar.addItem(constants.actionItems(), (Command) null);
        menuBar.addSeparator();
        return menuBar;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    protected String getType() {
        return FILTER;
    }

    public static String getDragDropEventData(DragDropEventBase<?> event, boolean isDrop) {
        if (isDrop) {
            return event.getData("Text"); //$NON-NLS-1$
        } else {
            // On most of the browsers drag, dragenter, dragleave, dragover and dragend don't have access to event's
            // data
            return lastDragData;
        }
    }
}
