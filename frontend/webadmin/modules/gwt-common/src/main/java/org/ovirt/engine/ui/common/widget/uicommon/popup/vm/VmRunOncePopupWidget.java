package org.ovirt.engine.ui.common.widget.uicommon.popup.vm;

import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.CommonApplicationMessages;
import org.ovirt.engine.ui.common.CommonApplicationResources;
import org.ovirt.engine.ui.common.CommonApplicationTemplates;
import org.ovirt.engine.ui.common.gin.AssetProvider;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.idhandler.WithElementId;
import org.ovirt.engine.ui.common.widget.Align;
import org.ovirt.engine.ui.common.widget.ComboBox;
import org.ovirt.engine.ui.common.widget.VisibilityRenderer;
import org.ovirt.engine.ui.common.widget.editor.ListModelListBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.ListModelTypeAheadChangeableListBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.VncKeyMapRenderer;
import org.ovirt.engine.ui.common.widget.editor.generic.EntityModelCheckBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.generic.EntityModelRadioButtonEditor;
import org.ovirt.engine.ui.common.widget.editor.generic.StringEntityModelPasswordBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.generic.StringEntityModelTextBoxEditor;
import org.ovirt.engine.ui.common.widget.form.key_value.KeyValueWidget;
import org.ovirt.engine.ui.common.widget.renderer.NameRenderer;
import org.ovirt.engine.ui.common.widget.uicommon.popup.AbstractModelBoundPopupWidget;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.BootSequenceModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.RunOnceModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.key_value.KeyValueModel;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.IEventListener;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiFactory;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.ButtonBase;
import com.google.gwt.user.client.ui.DisclosurePanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

public class VmRunOncePopupWidget extends AbstractModelBoundPopupWidget<RunOnceModel> {

    interface Driver extends SimpleBeanEditorDriver<RunOnceModel, VmRunOncePopupWidget> {
    }

    interface ViewUiBinder extends UiBinder<ScrollPanel, VmRunOncePopupWidget> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    interface ViewIdHandler extends ElementIdHandler<VmRunOncePopupWidget> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    interface Style extends CssResource {
        String attachImageCheckBoxLabel();

        String attachImageSelectBoxLabel();

        String attachImageSelectbox();

    }

    @UiField
    Style style;

    @UiField
    @WithElementId
    DisclosurePanel generalBootOptionsPanel;

    @UiField
    @WithElementId
    DisclosurePanel linuxBootOptionsPanel;

    @UiField
    @WithElementId
    DisclosurePanel initialRunPanel;

    @UiField
    @WithElementId
    VerticalPanel runOnceSpecificSysprepOptions;

    @UiField
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
    @WithElementId
    DisclosurePanel hostPanel;

    @UiField
    @WithElementId
    DisclosurePanel displayProtocolPanel;

    @UiField
    @WithElementId
    DisclosurePanel customPropertiesPanel;

    @UiField
    @Path(value = "floppyImage.selectedItem")
    @WithElementId("floppyImage")
    ListModelListBoxEditor<String> floppyImageEditor;

    @UiField
    @Ignore
    KeyValueWidget<KeyValueModel> customPropertiesSheetEditor;

    @UiField
    @Path(value = "isoImage.selectedItem")
    @WithElementId("isoImage")
    ListModelListBoxEditor<String> isoImageEditor;

    @UiField(provided = true)
    @Path(value = "attachFloppy.entity")
    @WithElementId("attachFloppy")
    EntityModelCheckBoxEditor attachFloppyEditor;

    @UiField(provided = true)
    @Path(value = "attachIso.entity")
    @WithElementId("attachIso")
    EntityModelCheckBoxEditor attachIsoEditor;

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

    @UiField
    @Ignore
    Label sysprepToEnableLabel;

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
    ButtonBase bootSequenceUpButton;

    @UiField
    @WithElementId
    ButtonBase bootSequenceDownButton;

    @UiField
    AbsolutePanel bootSequencePanel;

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

    private RunOnceModel runOnceModel;

    private BootSequenceModel bootSequenceModel;

    private final Driver driver = GWT.create(Driver.class);

    private static final CommonApplicationTemplates templates = AssetProvider.getTemplates();
    private static final CommonApplicationResources resources = AssetProvider.getResources();
    private static final CommonApplicationConstants constants = AssetProvider.getConstants();
    private static final CommonApplicationMessages messages = AssetProvider.getMessages();

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

        localize();
        addStyles();
        ViewIdHandler.idHandler.generateAndSetIds(this);
        driver.initialize(this);
    }

    void localize() {
        // Boot Options
        runAsStatelessEditor.setLabel(constants.runOncePopupRunAsStatelessLabel());
        runAndPauseEditor.setLabel(constants.runOncePopupRunAndPauseLabel());
        attachFloppyEditor.setLabel(constants.runOncePopupAttachFloppyLabel());
        attachIsoEditor.setLabel(constants.runOncePopupAttachIsoLabel());
        bootSequenceLabel.setText(constants.runOncePopupBootSequenceLabel());

        // Linux Boot Options
        kernelImageEditor.setLabel(constants.runOncePopupKernelPathLabel());
        initrdImageEditor.setLabel(constants.runOncePopupInitrdPathLabel());
        kernelParamsEditor.setLabel(constants.runOncePopupKernelParamsLabel());

        // Cloud Init
        cloudInitEnabledEditor.setLabel(constants.runOncePopupCloudInitLabel());

        // WindowsSysprep
        sysprepToEnableLabel.setText(constants.runOnceSysPrepToEnableLabel());
        sysPrepDomainNameListBoxEditor.setLabel(constants.runOncePopupSysPrepDomainNameLabel());
        useAlternateCredentialsEditor.setLabel(constants.runOnceUseAlternateCredentialsLabel());
        sysPrepUserNameEditor.setLabel(constants.runOncePopupSysPrepUserNameLabel());
        sysPrepPasswordEditor.setLabel(constants.runOncePopupSysPrepPasswordLabel());
        sysPrepPasswordVerificationEditor.setLabel(constants.runOncePopupSysPrepPasswordVerificationLabel());

        // Display Protocol
        displayConsoleVncEditor.setLabel(constants.runOncePopupDisplayConsoleVncLabel());
        vncKeyboardLayoutEditor.setLabel(constants.vncKeyboardLayoutVmPopup());

        displayConsoleSpiceEditor.setLabel(constants.runOncePopupDisplayConsoleSpiceLabel());
        spiceFileTransferEnabledEditor.setLabel(constants.spiceFileTransferEnabled());
        spiceCopyPasteEnabledEditor.setLabel(constants.spiceCopyPasteEnabled());

        // System Tab
        emulatedMachine.setLabel(constants.emulatedMachineLabel());
        customCpu.setLabel(constants.cpuModelLabel());

        // Host Tab
        isAutoAssignEditor.setLabel(constants.anyHostInClusterVmPopup());
    }

    void initCheckBoxEditors() {
        attachFloppyEditor = new EntityModelCheckBoxEditor(Align.RIGHT);
        attachIsoEditor = new EntityModelCheckBoxEditor(Align.RIGHT);
        bootMenuEnabledEditor = new EntityModelCheckBoxEditor(Align.RIGHT);
        runAsStatelessEditor = new EntityModelCheckBoxEditor(Align.RIGHT);
        runAndPauseEditor = new EntityModelCheckBoxEditor(Align.RIGHT);
        useAlternateCredentialsEditor = new EntityModelCheckBoxEditor(Align.RIGHT);
        spiceFileTransferEnabledEditor = new EntityModelCheckBoxEditor(Align.RIGHT);
        spiceCopyPasteEnabledEditor = new EntityModelCheckBoxEditor(Align.RIGHT);
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
    }

    void initRadioButtonEditors() {
        displayConsoleVncEditor = new EntityModelRadioButtonEditor("1"); //$NON-NLS-1$
        displayConsoleSpiceEditor = new EntityModelRadioButtonEditor("1"); //$NON-NLS-1$

        // host tab
        specificHost = new RadioButton("runVmOnHostGroup"); //$NON-NLS-1$
        isAutoAssignEditor = new EntityModelRadioButtonEditor("runVmOnHostGroup"); //$NON-NLS-1$
    }

    void initComboBox() {
        sysPrepDomainNameListBoxEditor = new ListModelListBoxEditor<>();
        sysPrepDomainNameTextBoxEditor = new StringEntityModelTextBoxEditor();

        sysPrepDomainNameListBoxEditor.asListBox().addDomHandler(new FocusHandler() {
            @Override
            public void onFocus(FocusEvent event) {
                sysPrepDomainNameListBoxEditor.asListBox().setSelectedIndex(-1);
            }
        }, FocusEvent.getType());

        sysPrepDomainNameListBoxEditor.asListBox().addDomHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) {
                runOnceModel.sysPrepListBoxChanged();
            }
        }, ChangeEvent.getType());

        sysPrepDomainNameComboBox = new ComboBox<>(sysPrepDomainNameListBoxEditor, sysPrepDomainNameTextBoxEditor);

        defaultHostEditor = new ListModelListBoxEditor<>(new NameRenderer<VDS>());

        emulatedMachine = new ListModelTypeAheadChangeableListBoxEditor(
                new ListModelTypeAheadChangeableListBoxEditor.NullSafeSuggestBoxRenderer() {

                    @Override
                    public String getDisplayStringNullSafe(String data) {
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
        bootSequenceBox.setWidth("475px"); //$NON-NLS-1$
        bootSequenceBox.setHeight("60px"); //$NON-NLS-1$

        VerticalPanel boxPanel = new VerticalPanel();
        boxPanel.setWidth("100%"); //$NON-NLS-1$
        boxPanel.add(bootSequenceBox);
        bootSequencePanel.add(boxPanel);

        localizeBootSequenceButtons();
    }

    void addStyles() {
        linuxBootOptionsPanel.setVisible(false);
        initialRunPanel.setVisible(false);
        systemPanel.setVisible(true);
        hostPanel.setVisible(true);
        attachFloppyEditor.addContentWidgetContainerStyleName(style.attachImageCheckBoxLabel());
        attachIsoEditor.addContentWidgetContainerStyleName(style.attachImageCheckBoxLabel());
        floppyImageEditor.addLabelStyleName(style.attachImageSelectBoxLabel());
        isoImageEditor.addLabelStyleName(style.attachImageSelectBoxLabel());
        floppyImageEditor.addContentWidgetContainerStyleName(style.attachImageSelectbox());
        isoImageEditor.addContentWidgetContainerStyleName(style.attachImageSelectbox());
    }

    @Override
    public void edit(final RunOnceModel object) {
        driver.edit(object);
        customPropertiesSheetEditor.edit(object.getCustomPropertySheet());
        runOnceModel = object;

        // Update Linux options panel
        final EntityModel<Boolean> isLinuxOptionsAvailable = object.getIsLinuxOptionsAvailable();
        object.getIsLinuxOptionsAvailable().getEntityChangedEvent().addListener(new IEventListener<EventArgs>() {
            @Override
            public void eventRaised(Event<? extends EventArgs> ev, Object sender, EventArgs args) {
                boolean isLinux = isLinuxOptionsAvailable.getEntity();
                linuxBootOptionsPanel.setVisible(isLinux);
            }
        });

        object.getIsSysprepEnabled().getEntityChangedEvent().addListener(new IEventListener<EventArgs>() {
            @Override
            public void eventRaised(Event<? extends EventArgs> ev, Object sender, EventArgs args) {
                updateSysprepVisibility(object);
            }
        });

        object.getIsCloudInitPossible().getEntityChangedEvent().addListener(new IEventListener<EventArgs>() {
            @Override
            public void eventRaised(Event<? extends EventArgs> ev, Object sender, EventArgs args) {
                updateCloudInitVisibility(object);
                updateInitialRunTabVisibility(object);
            }
        });

        object.getIsCloudInitEnabled().getEntityChangedEvent().addListener(new IEventListener<EventArgs>() {
            @Override
            public void eventRaised(Event<? extends EventArgs> ev, Object sender, EventArgs args) {
                updateCloudInitVisibility(object);
            }
        });

        object.getIsSysprepPossible().getEntityChangedEvent().addListener(new IEventListener<EventArgs>() {
            @Override
            public void eventRaised(Event<? extends EventArgs> ev, Object sender, EventArgs args) {
                updateSysprepVisibility(object);
                updateInitialRunTabVisibility(object);
            }
        });

        // Update Host combo
        object.getIsAutoAssign().getPropertyChangedEvent()
                .addListener(new IEventListener<PropertyChangedEventArgs>() {
                    @Override
                    public void eventRaised(Event<? extends PropertyChangedEventArgs> ev, Object sender, PropertyChangedEventArgs args) {
                        boolean isAutoAssign = object.getIsAutoAssign().getEntity();
                        defaultHostEditor.setEnabled(!isAutoAssign);
                        // only this is not bind tloudInitSubo the model, so needs to
                        // listen to the change explicitly
                        specificHost.setValue(!isAutoAssign);
                    }
        });

        specificHost.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                defaultHostEditor.setEnabled(specificHost.getValue());
                ValueChangeEvent.fire(isAutoAssignEditor.asRadioButton(), false);
            }
        });

        isAutoAssignEditor.addDomHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                defaultHostEditor.setEnabled(false);
            }
        }, ClickEvent.getType());

        object.getIsAutoAssign().getEntityChangedEvent().addListener(new IEventListener<EventArgs>() {
            @Override
            public void eventRaised(Event<? extends EventArgs> ev, Object sender, EventArgs args) {
                if (!isAutoAssignEditor.asRadioButton().getValue()) {
                    specificHost.setValue(true, true);
                }
            }
        });

        object.getPropertyChangedEvent().addListener(new IEventListener<PropertyChangedEventArgs>() {
            @Override
            public void eventRaised(Event<? extends PropertyChangedEventArgs> ev, Object sender, PropertyChangedEventArgs args) {
                String propName = args.propertyName;
                if ("IsHostTabVisible".equals(propName)) { //$NON-NLS-1$
                    hostPanel.setVisible(object.getIsHostTabVisible());
                } else if ("IsCustomPropertiesSheetVisible".equals(propName)) { //$NON-NLS-1$
                    customPropertiesPanel.setVisible(object.getIsCustomPropertiesSheetVisible());
                }
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

        sysprepToEnableLabel.setVisible(!selected && possible);
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

        vmInitWidget.setCloudInitContentVisible(selected && possible);
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
        bootSequenceModel.getItems().getCollectionChangedEvent().addListener(new IEventListener<EventArgs>() {
            @Override
            public void eventRaised(Event<? extends EventArgs> ev, Object sender, EventArgs args) {
                updateBootSequenceItems();

                // Update selected item
                bootSequenceBox.setSelectedIndex(bootSequenceModel.getSelectedItemIndex());
            }
        });

        // Attach CD change handling
        bootSequenceModel.getCdromOption().getPropertyChangedEvent().addListener(new IEventListener<PropertyChangedEventArgs>() {
            @Override
            public void eventRaised(Event<? extends PropertyChangedEventArgs> ev, Object sender, PropertyChangedEventArgs args) {
                boolean isEnabled = bootSequenceModel.getCdromOption().getIsChangable();
                String itemName = bootSequenceModel.getCdromOption().getTitle();
                updateItemAvailability(itemName, isEnabled);
            }
        });

        // NIC change handling
        bootSequenceModel.getNetworkOption().getPropertyChangedEvent().addListener(new IEventListener<PropertyChangedEventArgs>() {
            @Override
            public void eventRaised(Event<? extends PropertyChangedEventArgs> ev, Object sender, PropertyChangedEventArgs args) {
                boolean isEnabled = bootSequenceModel.getNetworkOption().getIsChangable();
                String itemName = bootSequenceModel.getNetworkOption().getTitle();
                updateItemAvailability(itemName, isEnabled);
            }
        });

        // Hard disk change handling
        bootSequenceModel.getHardDiskOption().getPropertyChangedEvent().addListener(new IEventListener<PropertyChangedEventArgs>() {
            @Override
            public void eventRaised(Event<? extends PropertyChangedEventArgs> ev, Object sender, PropertyChangedEventArgs args) {
                boolean isEnabled = bootSequenceModel.getHardDiskOption().getIsChangable();
                String itemName = bootSequenceModel.getHardDiskOption().getTitle();
                updateItemAvailability(itemName, isEnabled);
            }
        });

        // Change boot option handling
        bootSequenceBox.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) {
                int selectedIndex = bootSequenceBox.getSelectedIndex();
                bootSequenceModel.setSelectedItem(bootSequenceModel.getItems().get(selectedIndex));

                bootSequenceUpButton.setEnabled(bootSequenceModel.getMoveItemUpCommand().getIsExecutionAllowed());
                bootSequenceDownButton.setEnabled(bootSequenceModel.getMoveItemDownCommand().getIsExecutionAllowed());

                // the setEnabled resets the label for some reason, so need to set it back
                localizeBootSequenceButtons();
            }

        });
    }

    protected void localizeBootSequenceButtons() {
        bootSequenceUpButton.setText(constants.bootSequenceUpButtonLabel());
        bootSequenceDownButton.setText(constants.bootSequenceDownButtonLabel());
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

    private String typeAheadNameTemplateNullSafe(String name) {
        if (name != null && !name.trim().isEmpty()) {
            return templates.typeAheadName(name).asString();
        } else {
            return templates.typeAheadEmptyContent().asString();
        }
    }

}
