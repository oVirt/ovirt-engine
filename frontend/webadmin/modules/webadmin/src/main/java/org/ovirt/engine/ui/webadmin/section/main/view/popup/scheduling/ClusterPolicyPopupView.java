package org.ovirt.engine.ui.webadmin.section.main.view.popup.scheduling;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.common.scheduling.PolicyUnit;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.idhandler.WithElementId;
import org.ovirt.engine.ui.common.view.popup.AbstractModelBoundPopupView;
import org.ovirt.engine.ui.common.widget.dialog.SimpleDialogPanel;
import org.ovirt.engine.ui.common.widget.editor.EntityModelTextBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.ListModelListBoxOnlyEditor;
import org.ovirt.engine.ui.common.widget.form.key_value.KeyValueWidget;
import org.ovirt.engine.ui.common.widget.renderer.NullSafeRenderer;
import org.ovirt.engine.ui.uicommonweb.models.configure.scheduling.NewClusterPolicyModel;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.IEventListener;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.scheduling.ClusterPolicyPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.scheduling.panels.FunctionPolicyUnitPanel;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.scheduling.panels.PolicyUnitListPanel;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.scheduling.panels.PolicyUnitPanel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.inject.Inject;

public class ClusterPolicyPopupView extends AbstractModelBoundPopupView<NewClusterPolicyModel> implements ClusterPolicyPopupPresenterWidget.ViewDef {

    interface Driver extends SimpleBeanEditorDriver<NewClusterPolicyModel, ClusterPolicyPopupView> {
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
    EntityModelTextBoxEditor nameEditor;

    @UiField
    @Path(value = "description.entity")
    @WithElementId("description")
    EntityModelTextBoxEditor descriptionEditor;

    @UiField(provided = true)
    @Path(value = "loadBalanceList.selectedItem")
    @WithElementId("loadBalanceList")
    public ListModelListBoxOnlyEditor<Object> loadBalanceListEditor;

    @UiField
    @Ignore
    protected KeyValueWidget customPropertiesSheetEditor;

    @UiField(provided = true)
    PolicyUnitListPanel usedFilterPanel;

    @UiField(provided = true)
    PolicyUnitListPanel unusedFilterPanel;

    @UiField(provided = true)
    PolicyUnitListPanel usedFunctionPanel;

    @UiField(provided = true)
    PolicyUnitListPanel unusedFunctionPanel;

    @Inject
    public ClusterPolicyPopupView(EventBus eventBus, ApplicationResources resources, ApplicationConstants constants) {
        super(eventBus, resources);
        initListBoxEditors();
        initPanels();
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        ViewIdHandler.idHandler.generateAndSetIds(this);
        driver.initialize(this);
        localize(constants);
    }

    private void initPanels() {
        usedFilterPanel = new PolicyUnitListPanel();
        unusedFilterPanel = new PolicyUnitListPanel();
        usedFunctionPanel = new PolicyUnitListPanel();
        unusedFunctionPanel = new PolicyUnitListPanel();
    }

    private void initListBoxEditors() {
        loadBalanceListEditor = new ListModelListBoxOnlyEditor<Object>(new NullSafeRenderer<Object>() {
            @Override
            public String renderNullSafe(Object object) {
                return ((PolicyUnit) object).getName();
            }
        });
    }

    private void localize(ApplicationConstants constants) {
        nameEditor.setLabel(constants.clusterPolicyNameLabel());
        descriptionEditor.setLabel(constants.clusterPolicyDescriptionLabel());
    }

    public void edit(final NewClusterPolicyModel model) {
        driver.edit(model);
        updateFilters(model);
        model.getFiltersChangedEvent().addListener(new IEventListener() {

            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                updateFilters(model);

            }
        });
        updateFunctions(model);
        model.getFunctionsChangedEvent().addListener(new IEventListener() {

            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                updateFunctions(model);

            }
        });
        if (model.getClusterPolicy().isLocked()) {
            customPropertiesSheetEditor.setEnabled(false);
        }
        customPropertiesSheetEditor.edit(model.getCustomPropertySheet());
        model.getCustomPropertySheet().getKeyValueLines().getItemsChangedEvent().addListener(new IEventListener() {

            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                customPropertiesSheetEditor.edit(model.getCustomPropertySheet());
            }
        });
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
                            pair.getSecond());
            usedFunctionPanel.add(functionPolicyUnitPanel);
            functionPolicyUnitPanel.initWidget();
        }
        unusedFunctionPanel.clear();
        for (PolicyUnit policyUnit : model.getUnusedFunctions()) {
            functionPolicyUnitPanel = new FunctionPolicyUnitPanel(policyUnit, model, false, false, null);
            unusedFunctionPanel.add(functionPolicyUnitPanel);
            functionPolicyUnitPanel.initWidget();
        }
    }

    private void updateFilters(NewClusterPolicyModel model) {
        usedFilterPanel.clear();
        PolicyUnitPanel first = null;
        PolicyUnitPanel last = null;
        List<PolicyUnitPanel> list = new ArrayList<PolicyUnitPanel>();
        PolicyUnitPanel tempPolicyUnitPanel;
        for (PolicyUnit policyUnit : model.getUsedFilters()) {
            tempPolicyUnitPanel = new PolicyUnitPanel(policyUnit, model, true, model.getClusterPolicy().isLocked());
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
                    new PolicyUnitPanel(policyUnit, model, false, model.getClusterPolicy().isLocked());
            unusedFilterPanel.add(policyUnitPanel);
            policyUnitPanel.initWidget();
        }
    }

    @Override
    public NewClusterPolicyModel flush() {
        return driver.flush();
    }

}
