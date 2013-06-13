package org.ovirt.engine.ui.webadmin.section.main.view.popup.scheduling.panels;

import org.ovirt.engine.core.common.scheduling.PolicyUnit;
import org.ovirt.engine.ui.common.widget.MenuBar;
import org.ovirt.engine.ui.common.widget.PopupPanel;
import org.ovirt.engine.ui.uicommonweb.models.configure.scheduling.NewClusterPolicyModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.ContextMenuEvent;
import com.google.gwt.event.dom.client.ContextMenuHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;

public class PolicyUnitPanel extends FocusPanel {
    private static final ApplicationConstants constants = GWT.create(ApplicationConstants.class);
    static final PopupPanel menuPopup = new PopupPanel(true);
    protected final PolicyUnit policyUnit;
    protected boolean used;
    protected boolean locked;
    protected final NewClusterPolicyModel model;
    protected int position;

    public PolicyUnitPanel(PolicyUnit policyUnit,
            NewClusterPolicyModel model,
            boolean used,
            boolean locked) {
        this.policyUnit = policyUnit;
        this.model = model;
        this.used = used;
        this.locked = locked;
        if (!locked) {
            addDomHandler(new ContextMenuHandler() {

                @Override
                public void onContextMenu(ContextMenuEvent event) {
                    PolicyUnitPanel sourcePanel = (PolicyUnitPanel) event.getSource();
                    NativeEvent nativeEvent = event.getNativeEvent();
                    showContextMenu(sourcePanel, nativeEvent.getClientX(), nativeEvent.getClientY());
                    event.stopPropagation();
                    event.preventDefault();
                }

            }, ContextMenuEvent.getType());
        }
    }

    public void initWidget() {
        HorizontalPanel panel = new HorizontalPanel();
        Label nameLabel = new Label(policyUnit.getName());
        nameLabel.setWidth("180px");//$NON-NLS-1$
        panel.add(nameLabel);
        if (used && position != 0) {
            String labelText = null;
            if (position == -1) {
                labelText = constants.firstFilter();
            } else if (position == 1) {
                labelText = constants.lastFilter();
            }
            Label label = new Label(labelText);
            label.setWidth("50px");//$NON-NLS-1$
            panel.add(label);
        }
        setWidget(panel);
    }

    protected void showContextMenu(PolicyUnitPanel sourcePanel, int clientX, int clientY) {
        MenuBar menuBar = createMenu();
        fillMenuBar(menuBar);
        addSubMenu(menuBar);
        menuPopup.setWidget(menuBar);
        menuPopup.setPopupPosition(clientX, clientY);
        menuPopup.show();
    }

    protected void fillMenuBar(MenuBar menuBar) {
        if (used) {
            menuBar.addItem("Remove Filter", new Command() { //$NON-NLS-1$

                        @Override
                        public void execute() {
                            model.removeFilter(policyUnit);
                            menuPopup.hide();
                        }
                    });
        } else {
            menuBar.addItem("Add Filter", new Command() { //$NON-NLS-1$

                        @Override
                        public void execute() {
                            model.addFilter(policyUnit, used, 0);
                            menuPopup.hide();
                        }
                    });
        }
    }

    protected void addSubMenu(MenuBar menuBar) {
        MenuBar subMenu = new MenuBar(true);
        if (position >= 0) {
            subMenu.addItem("First", new Command() { //$NON-NLS-1$

                        @Override
                        public void execute() {
                            model.addFilter(policyUnit, used, -1);
                            menuPopup.hide();
                        }
                    });
        }
        if (position <= 0) {
            subMenu.addItem("Last", new Command() { //$NON-NLS-1$

                        @Override
                        public void execute() {
                            model.addFilter(policyUnit, used, 1);
                            menuPopup.hide();
                        }
                    });
        }
        menuBar.addItem("Position", subMenu); //$NON-NLS-1$
    }

    protected MenuBar createMenu() {
        MenuBar menuBar = new MenuBar(true);
        menuBar.addItem("Action Items", (Command) null); //$NON-NLS-1$
        menuBar.addSeparator();
        return menuBar;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

}
