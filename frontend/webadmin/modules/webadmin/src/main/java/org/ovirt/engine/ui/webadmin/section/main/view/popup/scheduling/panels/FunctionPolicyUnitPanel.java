package org.ovirt.engine.ui.webadmin.section.main.view.popup.scheduling.panels;

import org.ovirt.engine.core.common.scheduling.PolicyUnit;
import org.ovirt.engine.ui.common.widget.MenuBar;
import org.ovirt.engine.ui.uicommonweb.models.configure.scheduling.NewClusterPolicyModel;
import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.scheduling.ClusterPolicyPopupView.WidgetStyle;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.SimplePanel;

public class FunctionPolicyUnitPanel extends PolicyUnitPanel {
    public static final String FUNCTION = "Function"; //$NON-NLS-1$
    private Integer factor;

    private static final ApplicationResources resources = AssetProvider.getResources();

    public FunctionPolicyUnitPanel(PolicyUnit policyUnit,
            NewClusterPolicyModel model,
            boolean used,
            boolean locked,
            WidgetStyle style,
            Integer factor) {
        super(policyUnit, model, used, locked, style);
        this.factor = factor;
    }

    @Override
    public void initWidget() {
        HorizontalPanel panel = new HorizontalPanel();
        Panel namePanel = getNamePanel(policyUnit);
        if (!used) {
            panel.setStyleName(style.unusedPolicyUnitStyle());
            panel.add(namePanel);
        } else {
            HorizontalPanel weightPanel = new HorizontalPanel();
            final Label weightLabel = new Label(String.valueOf(factor));
            final PushButton downButton = new PushButton(new Image(resources.decreaseIcon()));
            downButton.setWidth("12px"); //$NON-NLS-1$
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
            upButton.setWidth("12px"); //$NON-NLS-1$
            upButton.getElement().getStyle().setPadding(0, Unit.PX);
            if (!locked || policyUnit.isEnabled()) {
                weightPanel.add(downButton);
            }
            weightPanel.add(weightLabel);
            if (!locked || policyUnit.isEnabled()) {
                weightPanel.add(upButton);
            }
            weightPanel.setStyleName(style.positionLabelStyle());
            panel.add(weightPanel);
            Panel policyUnitLablePanel = new SimplePanel();
            policyUnitLablePanel.add(namePanel);
            policyUnitLablePanel.setStyleName(style.usedFilterPolicyUnitStyle());
            panel.add(policyUnitLablePanel);
        }
        if (!policyUnit.isEnabled()) {
            panel.getElement().getStyle().setOpacity(0.5);
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

    @Override
    protected String getType() {
        return FUNCTION;
    }
}
