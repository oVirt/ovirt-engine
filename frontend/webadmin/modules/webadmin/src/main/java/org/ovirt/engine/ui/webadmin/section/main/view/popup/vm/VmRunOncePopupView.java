package org.ovirt.engine.ui.webadmin.section.main.view.popup.vm;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Column;
import org.gwtbootstrap3.client.ui.Container;
import org.ovirt.engine.core.common.businessentities.BootSequence;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.storage.RepoImage;
import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.CommonApplicationResources;
import org.ovirt.engine.ui.common.CommonApplicationTemplates;
import org.ovirt.engine.ui.common.editor.UiCommonEditorDriver;
import org.ovirt.engine.ui.common.gin.AssetProvider;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.idhandler.WithElementId;
import org.ovirt.engine.ui.common.view.popup.AbstractModelBoundPopupView;
import org.ovirt.engine.ui.common.widget.Align;
import org.ovirt.engine.ui.common.widget.ComboBox;
import org.ovirt.engine.ui.common.widget.VisibilityRenderer;
import org.ovirt.engine.ui.common.widget.dialog.InfoIcon;
import org.ovirt.engine.ui.common.widget.dialog.SimpleDialogPanel;
import org.ovirt.engine.ui.common.widget.dialog.tab.DialogTab;
import org.ovirt.engine.ui.common.widget.editor.ListModelListBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.ListModelTypeAheadChangeableListBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.VncKeyMapRenderer;
import org.ovirt.engine.ui.common.widget.editor.generic.EntityModelCheckBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.generic.EntityModelRadioButtonEditor;
import org.ovirt.engine.ui.common.widget.editor.generic.StringEntityModelPasswordBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.generic.StringEntityModelTextBoxEditor;
import org.ovirt.engine.ui.common.widget.form.key_value.KeyValueWidget;
import org.ovirt.engine.ui.common.widget.renderer.NameRenderer;
import org.ovirt.engine.ui.common.widget.renderer.NullSafeRenderer;
import org.ovirt.engine.ui.common.widget.uicommon.popup.vm.RunOnceVmInitWidget;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.BootSequenceModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.RunOnceModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.key_value.KeyValueModel;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.vm.VmRunOncePopupPresenterWidget;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.ButtonBase;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.inject.Inject;

public class VmRunOncePopupView extends AbstractModelBoundPopupView<RunOnceModel> implements VmRunOncePopupPresenterWidget.ViewDef {

    interface Driver extends UiCommonEditorDriver<RunOnceModel, VmRunOncePopupView> {
    }

    interface ViewIdHandler extends ElementIdHandler<VmRunOncePopupView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    interface ViewUiBinder extends UiBinder<SimpleDialogPanel, VmRunOncePopupView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    private static final CommonApplicationTemplates templates = AssetProvider.getTemplates();
    private static final CommonApplicationResources resources = AssetProvider.getResources();
    private static final CommonApplicationConstants constants = AssetProvider.getConstants();

    private final Driver driver = GWT.create(Driver.class);

    private RunOnceModel runOnceModel;

    private BootSequenceModel bootSequenceModel;

    @UiField
    @WithElementId
    DialogTab generalBootOptionsPanel;

    @UiField
    Container generalBootOptionsContainer;

    @UiField
    @WithElementId
    DialogTab linuxBootOptionsPanel;

    @UiField
    Container linuxBootOptionsContainer;

    @UiField
    @WithElementId
    DialogTab initialRunPanel;

    @UiField
    Container initialRunContainer;

    @UiField
    @WithElementId
    FlowPanel runOnceSpecificSysprepOptions;

    @UiField(provided = true)
    @Path(value = "isCloudInitEnabled.entity")
    @WithElementId
    EntityModelCheckBoxEditor cloudInitEnabledEditor;

    @UiField
    @Ignore
    @WithElementId(value = "vmInit", processType = false)
    RunOnceVmInitWidget vmInitWidget;

    @UiField
    @WithElementId
    DialogTab systemPanel;

    @UiField
    Container systemContainer;

    @UiField
    @WithElementId
    DialogTab hostPanel;

    @UiField
    Container hostContainer;

    @UiField
    @WithElementId
    DialogTab displayProtocolPanel;

    @UiField
    Container displayProtocolContainer;

    @UiField
    @WithElementId
    DialogTab customPropertiesPanel;

    @UiField
    Container customPropertiesContainer;

    @UiField
    @Ignore
    KeyValueWidget<KeyValueModel> customPropertiesSheetEditor;

    @UiField
    @Path(value = "floppyImage.selectedItem")
    @WithElementId("floppyImage")
    ListModelListBoxEditor<String> floppyImageEditor;

    @UiField(provided = true)
    @Path(value = "isoImage.selectedItem")
    @WithElementId("isoImage")
    ListModelListBoxEditor<RepoImage> isoImageEditor;

    @UiField(provided = true)
    @Path(value = "attachFloppy.entity")
    @WithElementId("attachFloppy")
    EntityModelCheckBoxEditor attachFloppyEditor;

    @UiField(provided = true)
    @Path(value = "attachIso.entity")
    @WithElementId("attachIso")
    EntityModelCheckBoxEditor attachIsoEditor;

    @UiField(provided = true)
    @Path(value = "attachWgt.entity")
    @WithElementId("attachWgt")
    EntityModelCheckBoxEditor attachWgtEditor;

    @UiField(provided = true)
    @Path(value = "attachSysprep.entity")
    @WithElementId("attachSysprep")
    EntityModelCheckBoxEditor attachSysprepEditor;

    @UiField(provided = true)
    @Path("bootMenuEnabled.entity")
    @WithElementId("bootMenuEnabled")
    EntityModelCheckBoxEditor bootMenuEnabledEditor;

    @UiField(provided = true)
    @Path(value = "runAsStateless.entity")
    @WithElementId("runAsStateless")
    EntityModelCheckBoxEditor runAsStatelessEditor;

    @UiField(provided = true)
    @Path(value = "volatileRun.entity")
    @WithElementId("volatileRun")
    EntityModelCheckBoxEditor volatileRunEditor;

    @UiField(provided = true)
    @Ignore
    InfoIcon volatileRunInfoIcon;

    @UiField(provided = true)
    @Path(value = "runAndPause.entity")
    @WithElementId("runAndPause")
    EntityModelCheckBoxEditor runAndPauseEditor;

    @UiField(provided = true)
    @Path(value = "initrdImage.selectedItem")
    @WithElementId("initrdImage")
    ListModelTypeAheadChangeableListBoxEditor initrdImageEditor;

    @UiField
    @Path(value = "kernelParameters.entity")
    @WithElementId("kernelParameters")
    StringEntityModelTextBoxEditor kernelParamsEditor;

    @UiField(provided = true)
    @WithElementId("sysPrepDomainNameComboBox")
    ComboBox<String> sysPrepDomainNameComboBox;

    @Path(value = "sysPrepDomainName.selectedItem")
    @WithElementId("sysPrepDomainNameListBox")
    ListModelListBoxEditor<String> sysPrepDomainNameListBoxEditor;

    @Path(value = "sysPrepSelectedDomainName.entity")
    @WithElementId("sysPrepDomainNameTextBox")
    StringEntityModelTextBoxEditor sysPrepDomainNameTextBoxEditor;

    @UiField(provided = true)
    @Path(value = "useAlternateCredentials.entity")
    @WithElementId("useAlternateCredentials")
    EntityModelCheckBoxEditor useAlternateCredentialsEditor;

    @UiField
    @Path(value = "sysPrepUserName.entity")
    @WithElementId("sysPrepUserName")
    StringEntityModelTextBoxEditor sysPrepUserNameEditor;

    @UiField
    @Path(value = "sysPrepPassword.entity")
    @WithElementId("sysPrepPassword")
    StringEntityModelPasswordBoxEditor sysPrepPasswordEditor;

    @UiField
    @Path(value = "sysPrepPasswordVerification.entity")
    @WithElementId("sysPrepPasswordVerification")
    StringEntityModelPasswordBoxEditor sysPrepPasswordVerificationEditor;

    @UiField(provided = true)
    @Path(value = "runOnceHeadlessModeIsSelected.entity")
    @WithElementId("runOnceHeadlessMode")
    EntityModelRadioButtonEditor runOnceHeadlessModeEditor;

    @WithElementId
    @Ignore
    @UiField(provided = true)
    public InfoIcon runOnceHeadlessModeEnabledInfoIcon;

    @UiField(provided = true)
    @Path(value = "displayConsole_Vnc_IsSelected.entity")
    @WithElementId("displayConsoleVnc")
    EntityModelRadioButtonEditor displayConsoleVncEditor;

    @UiField(provided = true)
    @Path(value = "vncKeyboardLayout.selectedItem")
    @WithElementId("vncKeyboardLayout")
    public ListModelListBoxEditor<String> vncKeyboardLayoutEditor;

    @UiField(provided = true)
    @Path(value = "displayConsole_Spice_IsSelected.entity")
    @WithElementId("displayConsoleSpice")
    EntityModelRadioButtonEditor displayConsoleSpiceEditor;

    @UiField(provided = true)
    @Path(value = "spiceFileTransferEnabled.entity")
    @WithElementId("spiceFileTransferEnabled")
    public EntityModelCheckBoxEditor spiceFileTransferEnabledEditor;

    @UiField(provided = true)
    @Path(value = "spiceCopyPasteEnabled.entity")
    @WithElementId("spiceCopyPasteEnabled")
    public EntityModelCheckBoxEditor spiceCopyPasteEnabledEditor;

    @UiField
    @WithElementId
    Button bootSequenceUpButton;

    @UiField
    @WithElementId
    Button bootSequenceDownButton;

    @UiField
    Column bootSequenceColumn;

    @UiField
    @Ignore
    Label bootSequenceLabel;

    @WithElementId("bootSequence")
    ListBox bootSequenceBox;

    @UiField(provided = true)
    @Path(value = "isAutoAssign.entity")
    @WithElementId("isAutoAssign")
    public EntityModelRadioButtonEditor isAutoAssignEditor;

    @UiField(provided = true)
    @Ignore
    @WithElementId("specificHost")
    public RadioButton specificHost;

    @UiField(provided = true)
    @Path(value = "defaultHost.selectedItem")
    @WithElementId("defaultHost")
    public ListModelListBoxEditor<VDS> defaultHostEditor;

    @UiField(provided = true)
    @Path(value = "emulatedMachine.selectedItem")
    @WithElementId("emulatedMachine")
    public ListModelTypeAheadChangeableListBoxEditor emulatedMachine;

    @UiField(provided = true)
    @Path(value = "customCpu.selectedItem")
    @WithElementId("customCpu")
    public ListModelTypeAheadChangeableListBoxEditor customCpu;

    @UiField(provided = true)
    @Path(value = "kernelImage.selectedItem")
    @WithElementId("kernelImage")
    ListModelTypeAheadChangeableListBoxEditor kernelImageEditor;

    @UiField
    @Ignore
    ButtonBase isoImagesRefreshButton;

    @UiField
    @Ignore
    ButtonBase linuxBootOptionsRefreshButton;

    @Inject
    public VmRunOncePopupView(EventBus eventBus) {
        super(eventBus);
        initCheckBoxEditors();
        initRadioButtonEditors();
        initListBoxEditors();
        initComboBox();
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        initBootSequenceBox();
        ViewIdHandler.idHandler.generateAndSetIds(this);
        driver.initialize(this);
        fixStylesForPatternfly();
    }

    void initCheckBoxEditors() {
        attachFloppyEditor = new EntityModelCheckBoxEditor(Align.RIGHT);
        attachIsoEditor = new EntityModelCheckBoxEditor(Align.RIGHT);
        attachWgtEditor = new EntityModelCheckBoxEditor(Align.RIGHT);
        attachSysprepEditor = new EntityModelCheckBoxEditor(Align.RIGHT);
        bootMenuEnabledEditor = new EntityModelCheckBoxEditor(Align.RIGHT);
        runAsStatelessEditor = new EntityModelCheckBoxEditor(Align.RIGHT);
        volatileRunEditor = new EntityModelCheckBoxEditor(Align.RIGHT);
        runAndPauseEditor = new EntityModelCheckBoxEditor(Align.RIGHT);
        useAlternateCredentialsEditor = new EntityModelCheckBoxEditor(Align.RIGHT);
        spiceFileTransferEnabledEditor = new EntityModelCheckBoxEditor(Align.RIGHT);
        spiceCopyPasteEnabledEditor = new EntityModelCheckBoxEditor(Align.RIGHT);
        cloudInitEnabledEditor = new EntityModelCheckBoxEditor(Align.RIGHT);
    }

    void initRadioButtonEditors() {
        runOnceHeadlessModeEditor = new EntityModelRadioButtonEditor("1"); //$NON-NLS-1$
        displayConsoleVncEditor = new EntityModelRadioButtonEditor("1"); //$NON-NLS-1$
        displayConsoleSpiceEditor = new EntityModelRadioButtonEditor("1"); //$NON-NLS-1$

        runOnceHeadlessModeEnabledInfoIcon =
                new InfoIcon(SafeHtmlUtils.fromTrustedString(constants.runOnceHeadlessModeExplanation()));
        volatileRunInfoIcon = new InfoIcon(templates.italicText(constants.volatileRunInfo()));

        // host tab
        specificHost = new RadioButton("runVmOnHostGroup"); //$NON-NLS-1$
        isAutoAssignEditor = new EntityModelRadioButtonEditor("runVmOnHostGroup"); //$NON-NLS-1$
    }

    void initComboBox() {
        sysPrepDomainNameListBoxEditor = new ListModelListBoxEditor<>();
        sysPrepDomainNameTextBoxEditor = new StringEntityModelTextBoxEditor();

        sysPrepDomainNameListBoxEditor.asListBox().addValueChangeHandler(event -> runOnceModel.sysPrepListBoxChanged());

        sysPrepDomainNameComboBox = new ComboBox<>(sysPrepDomainNameListBoxEditor, sysPrepDomainNameTextBoxEditor);

        defaultHostEditor = new ListModelListBoxEditor<>(new NameRenderer<>());

        emulatedMachine = new ListModelTypeAheadChangeableListBoxEditor(
                new ListModelTypeAheadChangeableListBoxEditor.NullSafeSuggestBoxRenderer() {

                    @Override
                    public String getDisplayStringNullSafe(String data) {
                        if (data == null || data.trim().isEmpty()) {
                            data = getDefaultEmulatedMachineLabel();
                        }
                        return typeAheadNameTemplateNullSafe(data);
                    }
                },
                false);

        customCpu = new ListModelTypeAheadChangeableListBoxEditor(
                new ListModelTypeAheadChangeableListBoxEditor.NullSafeSuggestBoxRenderer() {

                    @Override
                    public String getDisplayStringNullSafe(String data) {
                        return typeAheadNameTemplateNullSafe(data);
                    }
                },
                false);
    }

    void initListBoxEditors() {
        vncKeyboardLayoutEditor = new ListModelListBoxEditor<>(new VncKeyMapRenderer());

        kernelImageEditor = new ListModelTypeAheadChangeableListBoxEditor(
                new ListModelTypeAheadChangeableListBoxEditor.NullSafeSuggestBoxRenderer() {

                    @Override
                    public String getDisplayStringNullSafe(String data) {
                        return typeAheadNameTemplateNullSafe(data);
                    }
                },
                false,
                new VisibilityRenderer.SimpleVisibilityRenderer(),
                constants.empty());

        initrdImageEditor = new ListModelTypeAheadChangeableListBoxEditor(
                new ListModelTypeAheadChangeableListBoxEditor.NullSafeSuggestBoxRenderer() {

                    @Override
                    public String getDisplayStringNullSafe(String data) {
                        return typeAheadNameTemplateNullSafe(data);
                    }
                },
                false,
                new VisibilityRenderer.SimpleVisibilityRenderer(),
                constants.empty());

        isoImageEditor = new ListModelListBoxEditor<>(new NullSafeRenderer<RepoImage>() {
            @Override
            protected String renderNullSafe(RepoImage object) {
                return object.getName();
            }
        });
    }

    private String typeAheadNameTemplateNullSafe(String name) {
        if (name != null && !name.trim().isEmpty()) {
            return templates.typeAheadName(name).asString();
        } else {
            return templates.typeAheadEmptyContent().asString();
        }
    }

    @Override
    public RunOnceModel flush() {
        vmInitWidget.flush();
        return driver.flush();
    }

    @Override
    public void edit(final RunOnceModel model) {
        driver.edit(model);
        runOnceModel = model;
        linuxBootOptionsPanel.setVisible(false);
        initialRunPanel.setVisible(false);

        //Update linux options due to getting linux OS being loaded in async
        model.getIsLinuxOptionsAvailable().getEntityChangedEvent().addListener((ev, sender, args) -> {
            boolean newValue = Boolean.TRUE.equals(model.getIsLinuxOptionsAvailable().getEntity());
            linuxBootOptionsPanel.setVisible(newValue);
        });

        bootSequenceModel = model.getBootSequence();
        updateBootSequenceListBox();

        customPropertiesSheetEditor.edit(model.getCustomPropertySheet());

        updateSysprepOptionsVisibility(model);

        boolean cloudSelected = Boolean.TRUE.equals(model.getIsCloudInitEnabled().getEntity());
        boolean cloudPossible = Boolean.TRUE.equals(model.getIsCloudInitPossible().getEntity());

        if (model.getIsIgnition()) {
            cloudInitEnabledEditor.setLabel(constants.ignition() + " " + model.getIgnitionVersion()); //$NON-NLS-1$
            vmInitWidget.setIgnitionContentVisible(cloudPossible && cloudSelected);
        } else {
            cloudInitEnabledEditor.setLabel(constants.runOncePopupCloudInitLabel());
            vmInitWidget.setCloudInitContentVisible(cloudPossible && cloudSelected);
        }

        emulatedMachine.setNullReplacementString(getDefaultEmulatedMachineLabel());
        model.getClusterEmulatedMachine().getEntityChangedEvent().addListener((ev, sender, args) ->
                emulatedMachine.setNullReplacementString(getDefaultEmulatedMachineLabel()));

        model.getIsAutoAssign().getPropertyChangedEvent()
                .addListener((ev, send, args) -> {
                    boolean isAutoAssign = model.getIsAutoAssign().getEntity();
                    defaultHostEditor.setEnabled(!isAutoAssign);
                    specificHost.setValue(!isAutoAssign);
                });

        specificHost.addValueChangeHandler(event -> {
            defaultHostEditor.setEnabled(specificHost.getValue());
            ValueChangeEvent.fire(isAutoAssignEditor.asRadioButton(), false);
        });

        model.getIsAutoAssign().getEntityChangedEvent().addListener((ev, sender, args) -> {
            if (!isAutoAssignEditor.asRadioButton().getValue()) {
                specificHost.setValue(true, true);
            }
        });

        model.getPropertyChangedEvent().addListener((ev, sender, args) -> {
            String propName = args.propertyName;
            if ("IsHostTabVisible".equals(propName)) { //$NON-NLS-1$
                hostPanel.setVisible(model.getIsHostTabVisible());
            } else if ("IsCustomPropertiesSheetVisible".equals(propName)) { //$NON-NLS-1$
                customPropertiesPanel.setVisible(model.getIsCustomPropertiesSheetVisible());
            }
        });

        model.getIsSysprepEnabled().getEntityChangedEvent().addListener((ev, sender, args) -> {
            updateSysprepOptionsVisibility(model);
            if (Boolean.TRUE.equals(model.getIsSysprepEnabled().getEntity())) {
                model.autoSetVmHostname();
            }
        });

        model.getIsCloudInitEnabled().getEntityChangedEvent().addListener((ev, sender, args) -> {
            updateCloudInitVisibility(model);
            if (Boolean.TRUE.equals(model.getIsCloudInitEnabled().getEntity())) {
                model.autoSetVmHostname();
            }
        });

        model.getIsCloudInitPossible().getEntityChangedEvent().addListener((ev, sender, args) -> {
            updateCloudInitVisibility(model);
            updateInitialRunTabVisibility(model);
        });

        model.getIsSysprepPossible().getEntityChangedEvent().addListener((ev, sender, args) -> {
            updateSysprepOptionsVisibility(model);
            updateInitialRunTabVisibility(model);
        });

        vmInitWidget.edit(model.getVmInitModel());
    }

    @Override
    public void cleanup() {
        vmInitWidget.cleanup();
        driver.cleanup();
    }

    private void updateSysprepOptionsVisibility(RunOnceModel model) {
        boolean selected = Boolean.TRUE.equals(model.getIsSysprepEnabled().getEntity());
        boolean possible = Boolean.TRUE.equals(model.getIsSysprepPossible().getEntity());
        vmInitWidget.setSyspepContentVisible(possible && selected);
        runOnceSpecificSysprepOptions.setVisible(possible && selected);
    }

    private void updateCloudInitVisibility(RunOnceModel model) {
        boolean selected = Boolean.TRUE.equals(model.getIsCloudInitEnabled().getEntity());
        boolean possible = Boolean.TRUE.equals(model.getIsCloudInitPossible().getEntity());

        model.updateOSs();
        if (model.getIsIgnition()) {
            cloudInitEnabledEditor.setLabel(constants.ignition() + " " + model.getIgnitionVersion()); //$NON-NLS-1$
            vmInitWidget.setIgnitionContentVisible(selected && possible);
        } else {
            cloudInitEnabledEditor.setLabel(constants.runOncePopupCloudInitLabel());
            vmInitWidget.setCloudInitContentVisible(selected && possible);
        }
    }

    private void updateInitialRunTabVisibility(RunOnceModel model) {
        boolean cloudInitPossible = Boolean.TRUE.equals(model.getIsCloudInitPossible().getEntity());
        boolean sysprepPossible = Boolean.TRUE.equals(model.getIsSysprepPossible().getEntity());
        initialRunPanel.setVisible(sysprepPossible || cloudInitPossible);
    }

    private void fixStylesForPatternfly() {
        kernelImageEditor.hideLabel();
        defaultHostEditor.hideLabel();
        isoImageEditor.hideLabel();
        floppyImageEditor.hideLabel();
    }

    private void initBootSequenceBox() {
        bootSequenceBox = new ListBox(false);
        bootSequenceBox.setHeight("60px"); //$NON-NLS-1$
        bootSequenceBox.setWidth("100%"); //$NON-NLS-1$
        bootSequenceColumn.add(bootSequenceBox);
    }

    private void updateBootSequenceListBox() {
        updateBootSequenceItems();

        // Items change handling
        bootSequenceModel.getItems().getCollectionChangedEvent().addListener((ev, sender, args) -> {
            updateBootSequenceItems();

            // Update selected item
            bootSequenceBox.setSelectedIndex(bootSequenceModel.getSelectedItemIndex());
        });

        // Update predefined boot sequence options availability
        updateList(bootSequenceModel.getCdromOption());
        updateList(bootSequenceModel.getHardDiskOption());
        updateList(bootSequenceModel.getNetworkOption());

        bootSequenceBox.addChangeHandler(event -> {
            int selectIndex = bootSequenceBox.getSelectedIndex();
            bootSequenceModel.setSelectedItem(bootSequenceModel.getItems().get(selectIndex));
            bootSequenceUpButton.setEnabled(bootSequenceModel.getMoveItemUpCommand().getIsExecutionAllowed());
            bootSequenceDownButton.setEnabled(bootSequenceModel.getMoveItemDownCommand().getIsExecutionAllowed());
        });
    }

    private void updateList(EntityModel<BootSequence> entityModel) {
        entityModel.getPropertyChangedEvent().addListener((ev, sender, args) -> {
            boolean isEnabled = entityModel.getIsChangable();
            String itemName = entityModel.getTitle();
            updateItemAvailability(itemName, isEnabled);
        });
    }

    private void updateBootSequenceItems() {
        bootSequenceBox.clear();
        bootSequenceBox.setVisibleItemCount(bootSequenceModel.getItems().size());

        for (EntityModel<BootSequence> item : bootSequenceModel.getItems()) {
            bootSequenceBox.addItem(item.getTitle());
            updateItemAvailability(item.getTitle(), item.getIsChangable());
        }
    }

    private void updateItemAvailability(String itemName, boolean isEnabled) {
        for (int i = 0; i < bootSequenceBox.getItemCount(); i++) {
            if (bootSequenceBox.getItemText(i).equals(itemName)) {
                NodeList<Element> options = bootSequenceBox.getElement().getElementsByTagName("option"); //$NON-NLS-1$
                if (!isEnabled) {
                    options.getItem(i).setAttribute("disabled", "disabled"); //$NON-NLS-1$ //$NON-NLS-2$
                } else {
                    options.getItem(i).removeAttribute("disabled"); //$NON-NLS-1$
                }
            }
        }
    }

    private String getDefaultEmulatedMachineLabel() {
        String emulatedMachine = runOnceModel.getClusterEmulatedMachine().getEntity();
        String newClusterEmulatedMachine = constants.clusterDefaultOption();
        if (emulatedMachine != null) {
            newClusterEmulatedMachine += "(" + emulatedMachine + ")"; //$NON-NLS-1$ //$NON-NLS-2$
        }
        return newClusterEmulatedMachine;
    }

    //UI Handlers
    @UiHandler("bootSequenceUpButton")
    void handleBootSequenceUpButtonClick(ClickEvent ev) {
        if (bootSequenceModel != null) {
            bootSequenceModel.executeCommand(bootSequenceModel.getMoveItemUpCommand());
        }
    }

    @UiHandler("bootSequenceDownButton")
    void handleBootSequenceDownButtonClick(ClickEvent ev) {
        if (bootSequenceModel != null) {
            bootSequenceModel.executeCommand(bootSequenceModel.getMoveItemDownCommand());
        }
    }

    @UiHandler("isoImagesRefreshButton")
    void handleIsoImagesRefreshButtonClick(ClickEvent ev) {
        runOnceModel.updateIsoList(true);
    }

    @UiHandler("linuxBootOptionsRefreshButton")
    void handleLinuxBootOptionsRefreshButtonClick(ClickEvent ev) {
        runOnceModel.updateUnknownTypeImagesList(true);
    }

}
