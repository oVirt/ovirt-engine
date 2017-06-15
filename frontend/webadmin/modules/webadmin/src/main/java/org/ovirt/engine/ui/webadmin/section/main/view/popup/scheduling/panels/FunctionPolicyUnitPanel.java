package org.ovirt.engine.ui.webadmin.section.main.view.popup.scheduling.panels;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.constants.IconType;
import org.ovirt.engine.core.common.scheduling.PolicyUnit;
import org.ovirt.engine.ui.common.widget.MenuBar;
import org.ovirt.engine.ui.uicommonweb.models.configure.scheduling.NewClusterPolicyModel;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.scheduling.ClusterPolicyPopupView.WidgetStyle;

import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;

public class FunctionPolicyUnitPanel extends PolicyUnitPanel {
    public static final String FUNCTION = "Function"; //$NON-NLS-1$
    private Integer factor;

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
        FlowPanel namePanel = getNamePanel(policyUnit);
        if (!used) {
            namePanel.setStyleName(style.unusedPolicyUnitStyle());
        } else {
            final Label weightLabel = createWeightLabel();

            final Button downButton = createDownButton(weightLabel);
            if (factor == 1) {
                downButton.setEnabled(false);
            }
            Button upButton = createUpButton(weightLabel, downButton);

            if (!locked || policyUnit.isEnabled()) {
                namePanel.insert(upButton, 0);
            }
            namePanel.insert(weightLabel, 0);
            if (!locked || policyUnit.isEnabled()) {
                namePanel.insert(downButton, 0);
            }

            namePanel.setStyleName(style.usedFilterPolicyUnitStyle());
        }
        if (!policyUnit.isEnabled()) {
            namePanel.getElement().getStyle().setOpacity(0.5);
        }
        setWidget(namePanel);
    }

    private Button createUpButton(final Label weightLabel, final Button downButton) {
        Button upButton = new Button("", IconType.PLUS, event -> {
            factor++;
            model.updateFactor(policyUnit, factor);
            weightLabel.setText(String.valueOf(factor));
            if (factor > 1) {
                downButton.setEnabled(true);
            }
        });
        upButton.getElement().getStyle().setFloat(Style.Float.LEFT);
        upButton.getElement().getStyle().setPosition(Position.RELATIVE);
        upButton.getElement().getStyle().setTop(1, Unit.PX);
        upButton.getElement().getStyle().setMarginRight(5, Unit.PX);
        return upButton;
    }

    private Button createDownButton(final Label weightLabel) {
        final Button downButton = new Button("");
        downButton.getElement().getStyle().setFloat(Style.Float.LEFT);
        downButton.getElement().getStyle().setPosition(Position.RELATIVE);
        downButton.getElement().getStyle().setTop(1, Unit.PX);
        downButton.setIcon(IconType.MINUS);
        downButton.addClickHandler(event -> {
            factor--;
            model.updateFactor(policyUnit, factor);
            weightLabel.setText(String.valueOf(factor));
            if (factor == 1) {
                downButton.setEnabled(false);
            }
        });
        return downButton;
    }

    private Label createWeightLabel() {
        final Label weightLabel = new Label(String.valueOf(factor));
        weightLabel.getElement().getStyle().setPaddingLeft(2, Unit.PX);
        weightLabel.getElement().getStyle().setPaddingRight(2, Unit.PX);
        weightLabel.getElement().getStyle().setFloat(Style.Float.LEFT);
        return weightLabel;
    }

    @Override
    protected void fillMenuBar(MenuBar menuBar) {
        if (used) {
            menuBar.addItem("Remove Function", () -> { //$NON-NLS-1$
                model.removeFunction(policyUnit);
                menuPopup.hide();
            });
        } else {
            menuBar.addItem("Add Function", () -> { //$NON-NLS-1$
                model.addFunction(policyUnit);
                menuPopup.hide();
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
