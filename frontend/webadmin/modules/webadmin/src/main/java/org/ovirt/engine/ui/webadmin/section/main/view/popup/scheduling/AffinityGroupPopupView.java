package org.ovirt.engine.ui.webadmin.section.main.view.popup.scheduling;

import org.ovirt.engine.core.common.scheduling.EntityAffinityRule;
import org.ovirt.engine.ui.common.CommonApplicationTemplates;
import org.ovirt.engine.ui.common.editor.UiCommonEditorDriver;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.idhandler.WithElementId;
import org.ovirt.engine.ui.common.view.popup.AbstractModelBoundPopupView;
import org.ovirt.engine.ui.common.widget.Align;
import org.ovirt.engine.ui.common.widget.EntityModelWidgetWithInfo;
import org.ovirt.engine.ui.common.widget.dialog.InfoIcon;
import org.ovirt.engine.ui.common.widget.dialog.SimpleDialogPanel;
import org.ovirt.engine.ui.common.widget.editor.ListModelListBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.generic.EntityModelCheckBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.generic.StringEntityModelTextBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.generic.StringEntityModelTextBoxOnlyEditor;
import org.ovirt.engine.ui.common.widget.form.key_value.KeyWidget;
import org.ovirt.engine.ui.common.widget.label.EnableableFormLabel;
import org.ovirt.engine.ui.common.widget.renderer.EnumRenderer;
import org.ovirt.engine.ui.uicommonweb.models.configure.scheduling.affinity_groups.EntitySelectionModel;
import org.ovirt.engine.ui.uicommonweb.models.configure.scheduling.affinity_groups.model.AffinityGroupModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.scheduling.AffinityGroupPopupPresenterWidget;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.inject.Inject;

public class AffinityGroupPopupView extends AbstractModelBoundPopupView<AffinityGroupModel> implements AffinityGroupPopupPresenterWidget.ViewDef {
    interface Driver extends UiCommonEditorDriver<AffinityGroupModel, AffinityGroupPopupView> {
    }

    private final Driver driver = GWT.create(Driver.class);

    interface ViewUiBinder extends UiBinder<SimpleDialogPanel, AffinityGroupPopupView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    interface ViewIdHandler extends ElementIdHandler<AffinityGroupPopupView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    @UiField
    @Path(value = "name.entity")
    @WithElementId("name")
    StringEntityModelTextBoxEditor nameEditor;

    @UiField
    @Path(value = "description.entity")
    @WithElementId("description")
    StringEntityModelTextBoxEditor descriptionEditor;

    @Path(value = "priority.entity")
    @WithElementId("priority")
    StringEntityModelTextBoxOnlyEditor priorityEditor;

    @UiField(provided = true)
    @Ignore
    EntityModelWidgetWithInfo priorityEditorWithInfo;

    @Path(value = "vmAffinityRule.selectedItem")
    @UiField(provided=true)
    ListModelListBoxEditor<EntityAffinityRule> vmAffinityRuleEditor;

    @UiField(provided=true)
    InfoIcon vmAffinityRuleEditorInfoIcon;

    @Path(value = "vmAffinityEnforcing.entity")
    @WithElementId("vmAffinityEnforcing")
    @UiField(provided=true)
    EntityModelCheckBoxEditor vmAffinityEnforcingEditor;

    @UiField(provided=true)
    InfoIcon vmAffinityEnforcingEditorInfoIcon;

    @UiField
    @Ignore
    EnableableFormLabel vmAffinityEnforcingEditorLabel;

    @Path(value = "hostAffinityRule.selectedItem")
    @UiField(provided=true)
    ListModelListBoxEditor<EntityAffinityRule> hostAffinityRuleEditor;

    @UiField(provided=true)
    InfoIcon hostAffinityRuleEditorInfoIcon;

    @Path(value = "hostAffinityEnforcing.entity")
    @WithElementId("hostAffinityEnforcing")
    @UiField(provided=true)
    EntityModelCheckBoxEditor hostAffinityEnforcingEditor;

    @UiField(provided=true)
    InfoIcon hostAffinityEnforcingEditorInfoIcon;

    @UiField
    @Ignore
    EnableableFormLabel hostAffinityEnforcingEditorLabel;

    @UiField
    @Ignore
    protected KeyWidget<EntitySelectionModel> addRemoveVmWidget;

    @UiField
    @Ignore
    protected KeyWidget<EntitySelectionModel> addRemoveHostWidget;

    private static final CommonApplicationTemplates templates = AssetProvider.getTemplates();
    private static final ApplicationConstants constants = AssetProvider.getConstants();

    @Inject
    public AffinityGroupPopupView(EventBus eventBus) {
        super(eventBus);
        initEditors();
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        ViewIdHandler.idHandler.generateAndSetIds(this);
        driver.initialize(this);
    }

    private void initEditors() {
        priorityEditor = new StringEntityModelTextBoxOnlyEditor();
        EnableableFormLabel priorityLabel = new EnableableFormLabel();
        priorityLabel.setText(constants.affinityGroupPriorityLabel());

        priorityEditorWithInfo = new EntityModelWidgetWithInfo(priorityLabel, priorityEditor);
        priorityEditorWithInfo.setExplanation(templates.italicText(constants.affinityGroupPriorityInfo()));

        vmAffinityRuleEditor = new ListModelListBoxEditor<>(new EnumRenderer<EntityAffinityRule>());
        vmAffinityRuleEditor.hideLabel();
        vmAffinityRuleEditorInfoIcon = new InfoIcon(templates.italicText(constants.affinityGroupVmPolarityInfo()));

        vmAffinityEnforcingEditor = new EntityModelCheckBoxEditor(Align.RIGHT);
        vmAffinityEnforcingEditor.hideLabel();
        vmAffinityEnforcingEditorInfoIcon = new InfoIcon(templates.italicText(constants.affinityGroupEnforceInfo()));

        hostAffinityRuleEditor = new ListModelListBoxEditor<>(new EnumRenderer<EntityAffinityRule>());
        hostAffinityRuleEditor.hideLabel();
        hostAffinityRuleEditorInfoIcon = new InfoIcon(templates.italicText(constants.affinityGroupHostPolarityInfo()));

        hostAffinityEnforcingEditor = new EntityModelCheckBoxEditor(Align.RIGHT);
        hostAffinityEnforcingEditor.hideLabel();
        hostAffinityEnforcingEditorInfoIcon = new InfoIcon(templates.italicText(constants.affinityGroupEnforceInfo()));
    }

    public void edit(final AffinityGroupModel model) {
        driver.edit(model);
        addRemoveVmWidget.edit(model.getVmsOrLabelsSelectionModel());
        addRemoveHostWidget.edit(model.getHostsOrLabelsSelectionModel());
    }

    @Override
    public AffinityGroupModel flush() {
        return driver.flush();
    }

    @Override
    public void cleanup() {
        driver.cleanup();
    }
}
