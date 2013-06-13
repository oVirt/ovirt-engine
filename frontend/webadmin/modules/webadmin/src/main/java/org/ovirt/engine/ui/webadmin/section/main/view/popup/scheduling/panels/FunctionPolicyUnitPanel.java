package org.ovirt.engine.ui.webadmin.section.main.view.popup.scheduling.panels;

import org.ovirt.engine.core.common.scheduling.PolicyUnit;
import org.ovirt.engine.ui.common.widget.MenuBar;
import org.ovirt.engine.ui.uicommonweb.models.configure.scheduling.NewClusterPolicyModel;
import org.ovirt.engine.ui.webadmin.ApplicationResources;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PushButton;

public class FunctionPolicyUnitPanel extends PolicyUnitPanel {
    private Integer factor;
    private static final ApplicationResources resources = GWT.create(ApplicationResources.class);

    public FunctionPolicyUnitPanel(PolicyUnit policyUnit,
            NewClusterPolicyModel model,
            boolean used,
            boolean locked,
            Integer factor) {
        super(policyUnit, model, used, locked);
        this.factor = factor;
    }

    @Override
    public void initWidget() {
        HorizontalPanel panel = new HorizontalPanel();
        Label nameLabel = new Label(policyUnit.getName());
        nameLabel.setWidth("180px"); //$NON-NLS-1$
        panel.add(nameLabel);
        if (used) {
            HorizontalPanel weightPanel = new HorizontalPanel();
            final Label weightLabel = new Label(String.valueOf(factor));
            final PushButton downButton = new PushButton(new Image(resources.decreaseIcon()));
            downButton.addClickHandler(new ClickHandler() {

                @Override
                public void onClick(ClickEvent event) {
                    factor--;
                    model.updateFactor(policyUnit, factor);
                    weightLabel.setText(String.valueOf(factor));
                    if (factor == 1) {
                        downButton.setEnabled(false);
                    }
                }
            });
            downButton.getElement().getStyle().setPadding(0, Unit.PX);
            if (factor == 1) {
                downButton.setEnabled(false);
            }
            PushButton upButton = new PushButton(new Image(resources.increaseIcon()),
                    new ClickHandler() {

                        @Override
                        public void onClick(ClickEvent event) {
                            factor++;
                            model.updateFactor(policyUnit, factor);
                            weightLabel.setText(String.valueOf(factor));
                            if (factor > 1) {
                                downButton.setEnabled(true);
                            }
                        }
                    });
            upButton.getElement().getStyle().setPadding(0, Unit.PX);
            if (!locked) {
                weightPanel.add(upButton);
            }
            weightPanel.add(weightLabel);
            if (!locked) {
                weightPanel.add(downButton);
            }
            weightPanel.setWidth("50px"); //$NON-NLS-1$
            panel.add(weightPanel);
        }
        setWidget(panel);
    }

    @Override
    protected void fillMenuBar(MenuBar menuBar) {
        if (used) {
            menuBar.addItem("Remove Function", new Command() { //$NON-NLS-1$

                        @Override
                        public void execute() {
                            model.removeFunction(policyUnit);
                            menuPopup.hide();
                        }
                    });
        } else {
            menuBar.addItem("Add Function", new Command() { //$NON-NLS-1$

                        @Override
                        public void execute() {
                            model.addFunction(policyUnit);
                            menuPopup.hide();
                        }
                    });
        }
    }

    @Override
    protected void addSubMenu(MenuBar menuBar) {
        // No sub menu
    }
}
