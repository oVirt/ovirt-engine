package org.ovirt.engine.ui.webadmin.section.main.view.popup.scheduling;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.common.scheduling.PolicyUnit;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.ui.common.editor.UiCommonEditorDriver;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.idhandler.WithElementId;
import org.ovirt.engine.ui.common.view.popup.AbstractModelBoundPopupView;
import org.ovirt.engine.ui.common.widget.dialog.InfoIcon;
import org.ovirt.engine.ui.common.widget.dialog.SimpleDialogPanel;
import org.ovirt.engine.ui.common.widget.editor.ListModelListBoxOnlyEditor;
import org.ovirt.engine.ui.common.widget.editor.generic.StringEntityModelTextBoxEditor;
import org.ovirt.engine.ui.common.widget.form.key_value.KeyValueWidget;
import org.ovirt.engine.ui.common.widget.renderer.NameRenderer;
import org.ovirt.engine.ui.uicommonweb.models.configure.scheduling.NewClusterPolicyModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.key_value.KeyValueModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.ApplicationTemplates;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.scheduling.ClusterPolicyPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.scheduling.panels.FunctionPolicyUnitPanel;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.scheduling.panels.PolicyUnitListPanel;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.scheduling.panels.PolicyUnitPanel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.inject.Inject;

public class ClusterPolicyPopupView extends AbstractModelBoundPopupView<NewClusterPolicyModel> implements ClusterPolicyPopupPresenterWidget.ViewDef {
    interface Driver extends UiCommonEditorDriver<NewClusterPolicyModel, ClusterPolicyPopupView> {
    }

    interface ViewUiBinder extends UiBinder<SimpleDialogPanel, ClusterPolicyPopupView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    interface ViewIdHandler extends ElementIdHandler<ClusterPolicyPopupView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    private final Driver driver = GWT.create(Driver.class);

    @UiField
    @Path(value = "name.entity")
    @WithElementId("name")
    StringEntityModelTextBoxEditor nameEditor;

    @UiField
    @Path(value = "description.entity")
    @WithElementId("description")
    StringEntityModelTextBoxEditor descriptionEditor;

    @UiField(provided = true)
    @Path(value = "loadBalanceList.selectedItem")
    @WithElementId("loadBalanceList")
    public ListModelListBoxOnlyEditor<PolicyUnit> loadBalanceListEditor;

    @UiField
    @Ignore
    protected KeyValueWidget<KeyValueModel> customPropertiesSheetEditor;

    @UiField(provided = true)
    PolicyUnitListPanel usedFilterPanel;

    @UiField(provided = true)
    PolicyUnitListPanel unusedFilterPanel;

    @UiField(provided = true)
    PolicyUnitListPanel usedFunctionPanel;

    @UiField(provided = true)
    PolicyUnitListPanel unusedFunctionPanel;

    @UiField
    WidgetStyle style;

    @UiField(provided = true)
    InfoIcon filterInfoIcon;

    @UiField(provided = true)
    InfoIcon functionInfoIcon;

    @UiField(provided = true)
    InfoIcon loadBalancingInfoIcon;

    @UiField(provided = true)
    InfoIcon propertiesInfoIcon;

    @UiField
    @Ignore
    Label externalLabel;

    @UiField
    FlowPanel clusterPolicyPropertiesZone;

    private static final ApplicationTemplates templates = AssetProvider.getTemplates();
    private static final ApplicationConstants constants = AssetProvider.getConstants();

    @Inject
    public ClusterPolicyPopupView(EventBus eventBus) {
        super(eventBus);
        initListBoxEditors();
        initPanels();
        initInfoIcons();
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        ViewIdHandler.idHandler.generateAndSetIds(this);
        driver.initialize(this);
    }

    private void initPanels() {
        usedFilterPanel = new PolicyUnitListPanel(PolicyUnitPanel.FILTER, true);
        unusedFilterPanel = new PolicyUnitListPanel(PolicyUnitPanel.FILTER, false);
        usedFunctionPanel = new PolicyUnitListPanel(FunctionPolicyUnitPanel.FUNCTION, true);
        unusedFunctionPanel = new PolicyUnitListPanel(FunctionPolicyUnitPanel.FUNCTION, false);
    }

    private void setPanelModel(NewClusterPolicyModel model) {
        usedFilterPanel.setModel(model);
        unusedFilterPanel.setModel(model);
        usedFunctionPanel.setModel(model);
        unusedFunctionPanel.setModel(model);
    }

    private void initListBoxEditors() {
        loadBalanceListEditor = new ListModelListBoxOnlyEditor<>(new NameRenderer<PolicyUnit>());
    }

    public boolean showClusterPolicyPropertiesZone(final NewClusterPolicyModel model) {
        if (!model.getCustomProperties().isEmpty()) {
            return true;
        }

        if (model.getUsedFilters() != null) {
            for (PolicyUnit policyUnit: model.getUsedFilters()) {
                if (policyUnit.getParameterRegExMap() != null
                        && !policyUnit.getParameterRegExMap().isEmpty()) {
                    return true;
                }
            }
        }
        if (model.getUsedFunctions() != null) {
            for (Pair<PolicyUnit, Integer> policyUnit: model.getUsedFunctions()) {
                if (policyUnit.getFirst().getParameterRegExMap() != null
                        && !policyUnit.getFirst().getParameterRegExMap().isEmpty()) {
                    return true;
                }
            }
        }
        if (model.getLoadBalanceList().getSelectedItem() != null) {
            PolicyUnit policyUnit = model.getLoadBalanceList().getSelectedItem();
            if (policyUnit.getParameterRegExMap() != null
                    && !policyUnit.getParameterRegExMap().isEmpty()) {
                return true;
            }
        }

        return false;
    }

    public void edit(final NewClusterPolicyModel model) {
        driver.edit(model);
        setPanelModel(model);
        updateFilters(model);
        model.getFiltersChangedEvent().addListener((ev, sender, args) -> {
            updateFilters(model);
            clusterPolicyPropertiesZone.setVisible(showClusterPolicyPropertiesZone(model));
        });
        updateFunctions(model);
        model.getFunctionsChangedEvent().addListener((ev, sender, args) -> {
            updateFunctions(model);
            clusterPolicyPropertiesZone.setVisible(showClusterPolicyPropertiesZone(model));
        });
        customPropertiesSheetEditor.edit(model.getCustomPropertySheet());

        clusterPolicyPropertiesZone.setVisible(showClusterPolicyPropertiesZone(model));
        updateTooltips(model);
        model.getLoadBalanceList().getSelectedItemChangedEvent().addListener((ev, sender, args) -> {
            updateTooltips(model);
            clusterPolicyPropertiesZone.setVisible(showClusterPolicyPropertiesZone(model));
        });
    }

    private void initInfoIcons() {
        filterInfoIcon =
                new InfoIcon(templates.italicWordWrapMaxWidth(constants.clusterPolicyFilterInfo()));

        functionInfoIcon =
                new InfoIcon(templates.italicWordWrapMaxWidth(constants.clusterPolicyWeightFunctionInfo()));

        loadBalancingInfoIcon =
                new InfoIcon(templates.italicWordWrapMaxWidth(constants.clusterPolicyLoadBalancingInfo()));

        propertiesInfoIcon =
                new InfoIcon(templates.italicWordWrapMaxWidth(constants.clusterPolicyPropertiesInfo()));

    }

    private void updateTooltips(NewClusterPolicyModel model) {
        PolicyUnit selectedItem = model.getLoadBalanceList().getSelectedItem();
        if (selectedItem != null) {
            loadBalanceListEditor.getElement().setTitle(selectedItem.getDescription());
            String text = ""; //$NON-NLS-1$
            if (!selectedItem.isInternal()) {
                text = constants.externalPolicyUnitLabel() + " "; //$NON-NLS-1$
            }
            if (!selectedItem.isEnabled()) {
                text += constants.disabledPolicyUnit();
            }
            externalLabel.setText(text);
        }
    }

    private void updateFunctions(NewClusterPolicyModel model) {
        FunctionPolicyUnitPanel functionPolicyUnitPanel;
        usedFunctionPanel.clear();
        for (Pair<PolicyUnit, Integer> pair : model.getUsedFunctions()) {
            functionPolicyUnitPanel =
                    new FunctionPolicyUnitPanel(pair.getFirst(),
                            model,
                            true,
                            model.getClusterPolicy().isLocked(),
                            style,
                            pair.getSecond());
            usedFunctionPanel.add(functionPolicyUnitPanel);
            functionPolicyUnitPanel.initWidget();
        }
        unusedFunctionPanel.clear();
        for (PolicyUnit policyUnit : model.getUnusedFunctions()) {
            functionPolicyUnitPanel =
                    new FunctionPolicyUnitPanel(policyUnit,
                            model,
                            false,
                            model.getClusterPolicy().isLocked(),
                            style,
                            null);
            unusedFunctionPanel.add(functionPolicyUnitPanel);
            functionPolicyUnitPanel.initWidget();
        }
    }

    private void updateFilters(NewClusterPolicyModel model) {
        usedFilterPanel.clear();
        PolicyUnitPanel first = null;
        PolicyUnitPanel last = null;
        List<PolicyUnitPanel> list = new ArrayList<>();
        PolicyUnitPanel tempPolicyUnitPanel;
        for (PolicyUnit policyUnit : model.getUsedFilters()) {
            tempPolicyUnitPanel =
                    new PolicyUnitPanel(policyUnit, model, true, model.getClusterPolicy().isLocked(), style);
            Integer position = model.getFilterPositionMap().get(policyUnit.getId());
            if (position == null || position == 0) {
                list.add(tempPolicyUnitPanel);
            } else {
                if (position < 0) {
                    first = tempPolicyUnitPanel;
                } else if (position > 0) {
                    last = tempPolicyUnitPanel;
                }
                tempPolicyUnitPanel.setPosition(position);
            }
            tempPolicyUnitPanel.initWidget();
        }
        if (first != null) {
            usedFilterPanel.add(first);
        }
        for (PolicyUnitPanel policyUnitPanel : list) {
            usedFilterPanel.add(policyUnitPanel);
        }
        if (last != null) {
            usedFilterPanel.add(last);
        }
        unusedFilterPanel.clear();
        for (PolicyUnit policyUnit : model.getUnusedFilters()) {
            PolicyUnitPanel policyUnitPanel =
                    new PolicyUnitPanel(policyUnit, model, false, model.getClusterPolicy().isLocked(), style);
            unusedFilterPanel.add(policyUnitPanel);
            policyUnitPanel.initWidget();
        }
    }

    @Override
    public NewClusterPolicyModel flush() {
        return driver.flush();
    }

    @Override
    public void cleanup() {
        driver.cleanup();
    }

    public interface WidgetStyle extends CssResource {
        String unusedPolicyUnitStyle();

        String usedFilterPolicyUnitStyle();

        String positionLabelStyle();
    }
}
