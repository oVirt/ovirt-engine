package org.ovirt.engine.ui.common.widget.uicommon.popup.vm;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Column;
import org.gwtbootstrap3.client.ui.Container;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.storage.RepoImage;
import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.CommonApplicationResources;
import org.ovirt.engine.ui.common.CommonApplicationTemplates;
import org.ovirt.engine.ui.common.editor.UiCommonEditorDriver;
import org.ovirt.engine.ui.common.gin.AssetProvider;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.idhandler.WithElementId;
import org.ovirt.engine.ui.common.widget.Align;
import org.ovirt.engine.ui.common.widget.ComboBox;
import org.ovirt.engine.ui.common.widget.EntityModelWidgetWithInfo;
import org.ovirt.engine.ui.common.widget.VisibilityRenderer;
import org.ovirt.engine.ui.common.widget.dialog.InfoIcon;
import org.ovirt.engine.ui.common.widget.editor.ListModelListBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.ListModelTypeAheadChangeableListBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.VncKeyMapRenderer;
import org.ovirt.engine.ui.common.widget.editor.generic.EntityModelCheckBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.generic.EntityModelCheckBoxOnlyEditor;
import org.ovirt.engine.ui.common.widget.editor.generic.EntityModelRadioButtonEditor;
import org.ovirt.engine.ui.common.widget.editor.generic.StringEntityModelPasswordBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.generic.StringEntityModelTextBoxEditor;
import org.ovirt.engine.ui.common.widget.form.key_value.KeyValueWidget;
import org.ovirt.engine.ui.common.widget.label.EnableableFormLabel;
import org.ovirt.engine.ui.common.widget.renderer.NameRenderer;
import org.ovirt.engine.ui.common.widget.renderer.NullSafeRenderer;
import org.ovirt.engine.ui.common.widget.uicommon.popup.AbstractModelBoundPopupWidget;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.BootSequenceModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.RunOnceModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.key_value.KeyValueModel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.dom.client.Style.Overflow;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiFactory;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.ButtonBase;
import com.google.gwt.user.client.ui.DisclosurePanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.RadioButton;

public class VmRunOncePopupWidget extends AbstractModelBoundPopupWidget<RunOnceModel> {

    interface Driver extends UiCommonEditorDriver<RunOnceModel, VmRunOncePopupWidget> {
    }
    private static final String CONTENT = "content";  //$NON-NLS-1$

    interface ViewUiBinder extends UiBinder<FlowPanel, VmRunOncePopupWidget> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    interface ViewIdHandler extends ElementIdHandler<VmRunOncePopupWidget> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    interface Style extends CssResource {
        String widgetStyle();
    }

    @UiField
    Style style;

    @UiField
    @WithElementId
    DisclosurePanel generalBootOptionsPanel;

    @UiField
    Container generalBootOptionsContainer;

    @UiField
    @WithElementId
    DisclosurePanel linuxBootOptionsPanel;

    @UiField
    Container linuxBootOptionsContainer;

    @UiField
    @WithElementId
    DisclosurePanel initialRunPanel;

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
    @WithElementId("vmInit")
    RunOnceVmInitWidget vmInitWidget;

    @UiField
    @WithElementId
    DisclosurePanel systemPanel;

    @UiField
    Container systemContainer;

    @UiField
    @WithElementId
    DisclosurePanel hostPanel;

    @UiField
    Container hostContainer;

    @UiField
    @WithElementId
    DisclosurePanel displayProtocolPanel;

    @UiField
    Container displayProtocolContainer;

    @UiField
    @WithElementId
    DisclosurePanel customPropertiesPanel;

    @UiField
    Container customPropertiesContainer;

    @UiField
    @Path(value = "floppyImage.selectedItem")
    @WithElementId("floppyImage")
    ListModelListBoxEditor<String> floppyImageEditor;

    @UiField
    @Ignore
    KeyValueWidget<KeyValueModel> customPropertiesSheetEditor;

    @UiField(provided=true)
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
    @Path(value = "runAndPause.entity")
    @WithElementId("runAndPause")
    EntityModelCheckBoxEditor runAndPauseEditor;

    @UiField(provided = true)
    @Path(value = "kernelImage.selectedItem")
    @WithElementId("kernelImage")
    ListModelTypeAheadChangeableListBoxEditor kernelImageEditor;

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

    @UiField
    @Ignore
    ButtonBase isoImagesRefreshButton;

    @UiField
    @Ignore
    ButtonBase linuxBootOptionsRefreshButton;

    @Path("volatileRun.entity")
    @WithElementId("volatileRun")
    public EntityModelCheckBoxOnlyEditor volatileRunEditor;

    @UiField(provided = true)
    @Ignore
    public EntityModelWidgetWithInfo volatileRunEditorWithInfo;

    private RunOnceModel runOnceModel;

    private BootSequenceModel bootSequenceModel;

    private final Driver driver = GWT.create(Driver.class);

    private static final CommonApplicationTemplates templates = AssetProvider.getTemplates();
    private static final CommonApplicationResources resources = AssetProvider.getResources();
    private static final CommonApplicationConstants constants = AssetProvider.getConstants();

    @UiFactory
    protected DisclosurePanel createPanel(String label) {
        return new DisclosurePanel(resources.decreaseIcon(), resources.increaseIcon(), label);
    }

    public VmRunOncePopupWidget() {
        initCheckBoxEditors();
        initRadioButtonEditors();
        initListBoxEditors();
        initComboBox();
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        initBootSequenceBox();

        addStyles();
        ViewIdHandler.idHandler.generateAndSetIds(this);
        driver.initialize(this);

        fixStylesForPatternfly();
    }

    private void fixStylesForPatternfly() {
        generalBootOptionsContainer.removeStyleName(CONTENT);
        generalBootOptionsContainer.getParent().getElement().getStyle().setOverflow(Overflow.VISIBLE);
        linuxBootOptionsContainer.removeStyleName(CONTENT);
        linuxBootOptionsContainer.getParent().getElement().getStyle().setOverflow(Overflow.VISIBLE);
        initialRunContainer.removeStyleName(CONTENT);
        initialRunContainer.getParent().getElement().getStyle().setOverflow(Overflow.VISIBLE);
        systemContainer.removeStyleName(CONTENT);
        systemContainer.getParent().getElement().getStyle().setOverflow(Overflow.VISIBLE);
        hostContainer.removeStyleName(CONTENT);
        hostContainer.getParent().getElement().getStyle().setOverflow(Overflow.VISIBLE);
        displayProtocolContainer.removeStyleName(CONTENT);
        displayProtocolContainer.getParent().getElement().getStyle().setOverflow(Overflow.VISIBLE);
        customPropertiesContainer.removeStyleName(CONTENT);
        customPropertiesContainer.getParent().getElement().getStyle().setOverflow(Overflow.VISIBLE);
        floppyImageEditor.hideLabel();
        isoImageEditor.hideLabel();
        kernelImageEditor.hideLabel();
        defaultHostEditor.hideLabel();
    }

    void initCheckBoxEditors() {
        attachFloppyEditor = new EntityModelCheckBoxEditor(Align.RIGHT);
        attachIsoEditor = new EntityModelCheckBoxEditor(Align.RIGHT);
        attachWgtEditor = new EntityModelCheckBoxEditor(Align.RIGHT);
        attachSysprepEditor = new EntityModelCheckBoxEditor(Align.RIGHT);
        bootMenuEnabledEditor = new EntityModelCheckBoxEditor(Align.RIGHT);
        runAsStatelessEditor = new EntityModelCheckBoxEditor(Align.RIGHT);
        runAndPauseEditor = new EntityModelCheckBoxEditor(Align.RIGHT);
        useAlternateCredentialsEditor = new EntityModelCheckBoxEditor(Align.RIGHT);
        spiceFileTransferEnabledEditor = new EntityModelCheckBoxEditor(Align.RIGHT);
        spiceCopyPasteEnabledEditor = new EntityModelCheckBoxEditor(Align.RIGHT);
        cloudInitEnabledEditor = new EntityModelCheckBoxEditor(Align.RIGHT);

        volatileRunEditor = new EntityModelCheckBoxOnlyEditor();
        volatileRunEditorWithInfo = new EntityModelWidgetWithInfo(new EnableableFormLabel(constants.volatileRunOnce()), volatileRunEditor);
        volatileRunEditorWithInfo.setExplanation(templates.italicText(constants.volatileRunInfo()));
    }

    void initListBoxEditors() {
        vncKeyboardLayoutEditor = new ListModelListBoxEditor<>(new VncKeyMapRenderer());
        this.kernelImageEditor = new ListModelTypeAheadChangeableListBoxEditor(
                new ListModelTypeAheadChangeableListBoxEditor.NullSafeSuggestBoxRenderer() {

                    @Override
                    public String getDisplayStringNullSafe(String data) {
                        return typeAheadNameTemplateNullSafe(data);
                    }
                },
                false,
                new VisibilityRenderer.SimpleVisibilityRenderer(),
                constants.empty());

        this.initrdImageEditor = new ListModelTypeAheadChangeableListBoxEditor(
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

    void initRadioButtonEditors() {
        runOnceHeadlessModeEditor = new EntityModelRadioButtonEditor("1"); //$NON-NLS-1$
        displayConsoleVncEditor = new EntityModelRadioButtonEditor("1"); //$NON-NLS-1$
        displayConsoleSpiceEditor = new EntityModelRadioButtonEditor("1"); //$NON-NLS-1$

        runOnceHeadlessModeEnabledInfoIcon =
                new InfoIcon(SafeHtmlUtils.fromTrustedString(constants.runOnceHeadlessModeExplanation()));

        // host tab
        specificHost = new RadioButton("runVmOnHostGroup"); //$NON-NLS-1$
        isAutoAssignEditor = new EntityModelRadioButtonEditor("runVmOnHostGroup"); //$NON-NLS-1$
    }

    void initComboBox() {
        sysPrepDomainNameListBoxEditor = new ListModelListBoxEditor<>();
        sysPrepDomainNameTextBoxEditor = new StringEntityModelTextBoxEditor();

        sysPrepDomainNameListBoxEditor.asListBox().addValueChangeHandler(event -> runOnceModel.sysPrepListBoxChanged());

        sysPrepDomainNameComboBox = new ComboBox<>(sysPrepDomainNameListBoxEditor, sysPrepDomainNameTextBoxEditor);

        defaultHostEditor = new ListModelListBoxEditor<>(new NameRenderer<VDS>());

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

    void initBootSequenceBox() {
        bootSequenceBox = new ListBox(false);
        bootSequenceBox.setHeight("60px"); //$NON-NLS-1$
        bootSequenceBox.setWidth("100%"); //$NON-NLS-1$
        bootSequenceColumn.add(bootSequenceBox);
    }

    void addStyles() {
        addStyleName(style.widgetStyle());
        linuxBootOptionsPanel.setVisible(false);
        initialRunPanel.setVisible(false);
        systemPanel.setVisible(true);
        hostPanel.setVisible(true);
    }

    @Override
    public void edit(final RunOnceModel object) {
        driver.edit(object);
        customPropertiesSheetEditor.edit(object.getCustomPropertySheet());
        runOnceModel = object;

        emulatedMachine.setNullReplacementString(getDefaultEmulatedMachineLabel());
        object.getClusterEmulatedMachine().getEntityChangedEvent().addListener((ev, sender, args) -> {
            emulatedMachine.setNullReplacementString(getDefaultEmulatedMachineLabel());
        });

        // Update Linux options panel
        final EntityModel<Boolean> isLinuxOptionsAvailable = object.getIsLinuxOptionsAvailable();
        object.getIsLinuxOptionsAvailable().getEntityChangedEvent().addListener((ev, sender, args) -> {
            boolean isLinux = isLinuxOptionsAvailable.getEntity();
            linuxBootOptionsPanel.setVisible(isLinux);
        });

        object.getIsSysprepEnabled().getEntityChangedEvent().addListener((ev, sender, args) -> {
            updateSysprepVisibility(object);
            if (Boolean.TRUE.equals(object.getIsSysprepEnabled().getEntity())) {
                runOnceModel.autoSetVmHostname();
            }
        });

        object.getIsCloudInitPossible().getEntityChangedEvent().addListener((ev, sender, args) -> {
            updateCloudInitVisibility(object);
            updateInitialRunTabVisibility(object);
        });

        object.getIsCloudInitEnabled().getEntityChangedEvent().addListener((ev, sender, args) -> {
            updateCloudInitVisibility(object);
            if (Boolean.TRUE.equals(object.getIsCloudInitEnabled().getEntity())) {
                runOnceModel.autoSetVmHostname();
            }
        });

        object.getIsSysprepPossible().getEntityChangedEvent().addListener((ev, sender, args) -> {
            updateSysprepVisibility(object);
            updateInitialRunTabVisibility(object);
        });

        // Update Host combo
        object.getIsAutoAssign().getPropertyChangedEvent()
                .addListener((ev, sender, args) -> {
                    boolean isAutoAssign = object.getIsAutoAssign().getEntity();
                    defaultHostEditor.setEnabled(!isAutoAssign);
                    // only this is not bind tloudInitSubo the model, so needs to
                    // listen to the change explicitly
                    specificHost.setValue(!isAutoAssign);
                });

        specificHost.addValueChangeHandler(event -> {
            defaultHostEditor.setEnabled(specificHost.getValue());
            ValueChangeEvent.fire(isAutoAssignEditor.asRadioButton(), false);
        });

        object.getIsAutoAssign().getEntityChangedEvent().addListener((ev, sender, args) -> {
            if (!isAutoAssignEditor.asRadioButton().getValue()) {
                specificHost.setValue(true, true);
            }
        });

        object.getPropertyChangedEvent().addListener((ev, sender, args) -> {
            String propName = args.propertyName;
            if ("IsHostTabVisible".equals(propName)) { //$NON-NLS-1$
                hostPanel.setVisible(object.getIsHostTabVisible());
            } else if ("IsCustomPropertiesSheetVisible".equals(propName)) { //$NON-NLS-1$
                customPropertiesPanel.setVisible(object.getIsCustomPropertiesSheetVisible());
            }
        });

        // Update BootSequence ListBox
        bootSequenceModel = object.getBootSequence();
        updateBootSequenceListBox();

        vmInitWidget.edit(object.getVmInitModel());
    }

    private void updateSysprepVisibility(RunOnceModel model) {
        boolean selected = model.getIsSysprepEnabled().getEntity();
        boolean possible = model.getIsSysprepPossible().getEntity();

        vmInitWidget.setSyspepContentVisible(selected && possible);
        runOnceSpecificSysprepOptions.setVisible(selected && possible);
    }

    private void updateInitialRunTabVisibility(RunOnceModel model) {
        boolean cloudInitPossible = model.getIsCloudInitPossible().getEntity() != null ?
                model.getIsCloudInitPossible().getEntity() : false;

        boolean sysprepPossible = model.getIsSysprepPossible().getEntity() != null ?
                model.getIsSysprepPossible().getEntity() : false;

        initialRunPanel.setVisible(sysprepPossible || cloudInitPossible);
    }

    private void updateCloudInitVisibility(RunOnceModel model) {
        boolean selected = model.getIsCloudInitEnabled().getEntity();
        boolean possible = model.getIsCloudInitPossible().getEntity();

        runOnceModel.updateOSs();
        if(runOnceModel.getIsIgnition()){
            cloudInitEnabledEditor.setLabel(constants.ignition() + " " + runOnceModel.getIgnitionVersion()); //$NON-NLS-1$
            vmInitWidget.setIgnitionContentVisible(selected && possible);
        } else {
            cloudInitEnabledEditor.setLabel(constants.runOncePopupCloudInitLabel());
            vmInitWidget.setCloudInitContentVisible(selected && possible);
        }
    }

    @UiHandler("isoImagesRefreshButton")
    void handleIsoImagesRefreshButtonClick(ClickEvent event) {
        runOnceModel.updateIsoList(true);
    }

    @UiHandler("linuxBootOptionsRefreshButton")
    void handleLinuxBootOptionsRefreshButtonRefreshButtonClick(ClickEvent event) {
        runOnceModel.updateUnknownTypeImagesList(true);
    }

    @UiHandler("bootSequenceUpButton")
    void handleBootSequenceUpButtonClick(ClickEvent event) {
        if (bootSequenceModel != null) {
            bootSequenceModel.executeCommand(bootSequenceModel.getMoveItemUpCommand());
        }
    }

    @UiHandler("bootSequenceDownButton")
    void handleBootSequenceDownButtonClick(ClickEvent event) {
        if (bootSequenceModel != null) {
            bootSequenceModel.executeCommand(bootSequenceModel.getMoveItemDownCommand());
        }
    }

    private void updateBootSequenceListBox() {
        // Update Items
        updateBootSequenceItems();

        // Items change handling
        bootSequenceModel.getItems().getCollectionChangedEvent().addListener((ev, sender, args) -> {
            updateBootSequenceItems();

            // Update selected item
            bootSequenceBox.setSelectedIndex(bootSequenceModel.getSelectedItemIndex());
        });

        // Attach CD change handling
        bootSequenceModel.getCdromOption().getPropertyChangedEvent().addListener((ev, sender, args) -> {
            boolean isEnabled = bootSequenceModel.getCdromOption().getIsChangable();
            String itemName = bootSequenceModel.getCdromOption().getTitle();
            updateItemAvailability(itemName, isEnabled);
        });

        // NIC change handling
        bootSequenceModel.getNetworkOption().getPropertyChangedEvent().addListener((ev, sender, args) -> {
            boolean isEnabled = bootSequenceModel.getNetworkOption().getIsChangable();
            String itemName = bootSequenceModel.getNetworkOption().getTitle();
            updateItemAvailability(itemName, isEnabled);
        });

        // Hard disk change handling
        bootSequenceModel.getHardDiskOption().getPropertyChangedEvent().addListener((ev, sender, args) -> {
            boolean isEnabled = bootSequenceModel.getHardDiskOption().getIsChangable();
            String itemName = bootSequenceModel.getHardDiskOption().getTitle();
            updateItemAvailability(itemName, isEnabled);
        });

        // Change boot option handling
        bootSequenceBox.addChangeHandler(event -> {
            int selectedIndex = bootSequenceBox.getSelectedIndex();
            bootSequenceModel.setSelectedItem(bootSequenceModel.getItems().get(selectedIndex));

            bootSequenceUpButton.setEnabled(bootSequenceModel.getMoveItemUpCommand().getIsExecutionAllowed());
            bootSequenceDownButton.setEnabled(bootSequenceModel.getMoveItemDownCommand().getIsExecutionAllowed());
        });
    }

    private void updateBootSequenceItems() {
        // Update list box
        bootSequenceBox.clear();
        bootSequenceBox.setVisibleItemCount(bootSequenceModel.getItems().size());

        // Set items
        for (EntityModel bootItem : bootSequenceModel.getItems()) {
            bootSequenceBox.addItem(bootItem.getTitle());
            updateItemAvailability(bootItem.getTitle(), bootItem.getIsChangable());
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

    @Override
    public RunOnceModel flush() {
        vmInitWidget.flush();
        return driver.flush();
    }

    @Override
    public void cleanup() {
        vmInitWidget.cleanup();
        driver.cleanup();
    }

    private String typeAheadNameTemplateNullSafe(String name) {
        if (name != null && !name.trim().isEmpty()) {
            return templates.typeAheadName(name).asString();
        } else {
            return templates.typeAheadEmptyContent().asString();
        }
    }

    private String getDefaultEmulatedMachineLabel() {
        String emulatedMachine = runOnceModel.getClusterEmulatedMachine().getEntity();
        String newClusterEmulatedMachine = constants.clusterDefaultOption();
        if (emulatedMachine != null) {
            newClusterEmulatedMachine +=  "(" + emulatedMachine + ")"; //$NON-NLS-1$ //$NON-NLS-2$
        }
        return newClusterEmulatedMachine;
    }
}
