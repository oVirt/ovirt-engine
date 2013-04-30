package org.ovirt.engine.ui.common.widget.uicommon.popup.vm;

import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.CommonApplicationResources;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.idhandler.WithElementId;
import org.ovirt.engine.ui.common.widget.Align;
import org.ovirt.engine.ui.common.widget.ComboBox;
import org.ovirt.engine.ui.common.widget.editor.EntityModelCheckBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.EntityModelRadioButtonEditor;
import org.ovirt.engine.ui.common.widget.editor.EntityModelTextBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.ListModelListBoxEditor;
import org.ovirt.engine.ui.common.widget.form.key_value.KeyValueWidget;
import org.ovirt.engine.ui.common.widget.renderer.NullSafeRenderer;
import org.ovirt.engine.ui.common.widget.uicommon.popup.AbstractModelBoundPopupWidget;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.BootSequenceModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.RunOnceModel;
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
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.VerticalPanel;

public class VmRunOncePopupWidget extends AbstractModelBoundPopupWidget<RunOnceModel> {

    interface Driver extends SimpleBeanEditorDriver<RunOnceModel, VmRunOncePopupWidget> {
    }

    interface ViewUiBinder extends UiBinder<FlowPanel, VmRunOncePopupWidget> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    interface ViewIdHandler extends ElementIdHandler<VmRunOncePopupWidget> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    interface Style extends CssResource {
        String attachImageCheckBoxLabel();

        String attachImageSelectBoxLabel();

        String attachImageSelectbox();

        String cloudInitLabel();
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
    VerticalPanel sysprepSubPanel;

    @UiField
    @WithElementId
    VerticalPanel sysprepOptions;

    @UiField
    @WithElementId
    VerticalPanel cloudInitSubPanel;

    @UiField
    @Ignore
    @WithElementId("cloudInitWidget")
    CloudInitWidget cloudInitWidget;

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
    ListModelListBoxEditor<Object> floppyImageEditor;

    @UiField
    @Ignore
    KeyValueWidget customPropertiesSheetEditor;

    @UiField
    @Path(value = "isoImage.selectedItem")
    @WithElementId("isoImage")
    ListModelListBoxEditor<Object> isoImageEditor;

    @UiField(provided = true)
    @Path(value = "attachFloppy.entity")
    @WithElementId("attachFloppy")
    EntityModelCheckBoxEditor attachFloppyEditor;

    @UiField(provided = true)
    @Path(value = "attachIso.entity")
    @WithElementId("attachIso")
    EntityModelCheckBoxEditor attachIsoEditor;

    @UiField(provided = true)
    @Path(value = "runAsStateless.entity")
    @WithElementId("runAsStateless")
    EntityModelCheckBoxEditor runAsStatelessEditor;

    @UiField(provided = true)
    @Path(value = "runAndPause.entity")
    @WithElementId("runAndPause")
    EntityModelCheckBoxEditor runAndPauseEditor;

    @UiField
    @Path(value = "Kernel_path.entity")
    @WithElementId("kernelPath")
    EntityModelTextBoxEditor kernelPathEditor;

    @UiField
    @Path(value = "Initrd_path.entity")
    @WithElementId("initrdPath")
    EntityModelTextBoxEditor initrdPathEditor;

    @UiField
    @Path(value = "Kernel_parameters.entity")
    @WithElementId("kernelParameters")
    EntityModelTextBoxEditor kernelParamsEditor;

    @UiField(provided = true)
    @WithElementId("sysPrepDomainNameComboBox")
    ComboBox sysPrepDomainNameComboBox;

    @UiField
    @Ignore
    Label sysprepToEnableLabel;

    @Path(value = "sysPrepDomainName.selectedItem")
    @WithElementId("sysPrepDomainNameListBox")
    ListModelListBoxEditor<Object> sysPrepDomainNameListBoxEditor;

    @Path(value = "SysPrepSelectedDomainName.entity")
    @WithElementId("sysPrepDomainNameTextBox")
    EntityModelTextBoxEditor sysPrepDomainNameTextBoxEditor;

    @UiField(provided = true)
    @Path(value = "useAlternateCredentials.entity")
    @WithElementId("useAlternateCredentials")
    EntityModelCheckBoxEditor useAlternateCredentialsEditor;

    @UiField
    @Path(value = "sysPrepUserName.entity")
    @WithElementId("sysPrepUserName")
    EntityModelTextBoxEditor sysPrepUserNameEditor;

    @UiField
    @Path(value = "sysPrepPassword.entity")
    @WithElementId("sysPrepPassword")
    EntityModelTextBoxEditor sysPrepPasswordEditor;

    @UiField(provided = true)
    @Path(value = "isCloudInitEnabled.entity")
    @WithElementId("isCloudInitEnabled")
    EntityModelCheckBoxEditor cloudInitEnabledEditor;

    @UiField(provided = true)
    @Path(value = "displayConsole_Vnc_IsSelected.entity")
    @WithElementId("displayConsoleVnc")
    EntityModelRadioButtonEditor displayConsoleVncEditor;

    @UiField(provided = true)
    @Path(value = "displayConsole_Spice_IsSelected.entity")
    @WithElementId("displayConsoleSpice")
    EntityModelRadioButtonEditor displayConsoleSpiceEditor;

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
    public ListModelListBoxEditor<Object> defaultHostEditor;

    @UiField
    @Ignore
    ButtonBase refreshButton;

    private RunOnceModel runOnceModel;

    private BootSequenceModel bootSequenceModel;

    private final Driver driver = GWT.create(Driver.class);

    private final CommonApplicationResources resources;
    private final CommonApplicationConstants constants;

    @UiFactory
    protected DisclosurePanel createPanel(String label)
    {
        return new DisclosurePanel(resources.decreaseIcon(), resources.increaseIcon(), label);
    }

    public VmRunOncePopupWidget(CommonApplicationConstants constants, CommonApplicationResources resources) {
        this.constants = constants;
        this.resources = resources;
        initCheckBoxEditors();
        initRadioButtonEditors();
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
        kernelPathEditor.setLabel(constants.runOncePopupKernelPathLabel());
        initrdPathEditor.setLabel(constants.runOncePopupInitrdPathLabel());
        kernelParamsEditor.setLabel(constants.runOncePopupKernelParamsLabel());

        // WindowsSysprep
        sysprepToEnableLabel.setText(constants.runOnceSysPrepToEnableLabel());
        sysPrepDomainNameListBoxEditor.setLabel(constants.runOncePopupSysPrepDomainNameLabel());
        useAlternateCredentialsEditor.setLabel(constants.runOnceUseAlternateCredentialsLabel());
        sysPrepUserNameEditor.setLabel(constants.runOncePopupSysPrepUserNameLabel());
        sysPrepPasswordEditor.setLabel(constants.runOncePopupSysPrepPasswordLabel());

        // Linux Cloud-Init
        cloudInitEnabledEditor.setLabel(constants.runOncePopupCloudInitLabel());

        // Display Protocol
        displayConsoleVncEditor.setLabel(constants.runOncePopupDisplayConsoleVncLabel());
        displayConsoleSpiceEditor.setLabel(constants.runOncePopupDisplayConsoleSpiceLabel());

        // Host Tab
        isAutoAssignEditor.setLabel(constants.anyHostInClusterVmPopup());
    }

    void initCheckBoxEditors() {
        attachFloppyEditor = new EntityModelCheckBoxEditor(Align.RIGHT);
        attachIsoEditor = new EntityModelCheckBoxEditor(Align.RIGHT);
        runAsStatelessEditor = new EntityModelCheckBoxEditor(Align.RIGHT);
        runAndPauseEditor = new EntityModelCheckBoxEditor(Align.RIGHT);
        useAlternateCredentialsEditor = new EntityModelCheckBoxEditor(Align.RIGHT);
        cloudInitEnabledEditor = new EntityModelCheckBoxEditor(Align.RIGHT);
    }

    void initRadioButtonEditors() {
        displayConsoleVncEditor = new EntityModelRadioButtonEditor("1"); //$NON-NLS-1$
        displayConsoleSpiceEditor = new EntityModelRadioButtonEditor("1"); //$NON-NLS-1$

        // host tab
        specificHost = new RadioButton("runVmOnHostGroup"); //$NON-NLS-1$
        isAutoAssignEditor = new EntityModelRadioButtonEditor("runVmOnHostGroup"); //$NON-NLS-1$
    }

    void initComboBox() {
        sysPrepDomainNameListBoxEditor = new ListModelListBoxEditor<Object>();
        sysPrepDomainNameTextBoxEditor = new EntityModelTextBoxEditor();

        sysPrepDomainNameComboBox = new ComboBox(sysPrepDomainNameListBoxEditor, sysPrepDomainNameTextBoxEditor);

        defaultHostEditor = new ListModelListBoxEditor<Object>(new NullSafeRenderer<Object>() {
            @Override
            public String renderNullSafe(Object object) {
                return ((VDS) object).getName();
            }
        });
    }

    void initBootSequenceBox() {
        bootSequenceBox = new ListBox(false);
        bootSequenceBox.setWidth("370px"); //$NON-NLS-1$
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
        sysprepSubPanel.setVisible(false);
        cloudInitSubPanel.setVisible(false);
        hostPanel.setVisible(true);
        attachFloppyEditor.addContentWidgetStyleName(style.attachImageCheckBoxLabel());
        attachIsoEditor.addContentWidgetStyleName(style.attachImageCheckBoxLabel());
        floppyImageEditor.addLabelStyleName(style.attachImageSelectBoxLabel());
        isoImageEditor.addLabelStyleName(style.attachImageSelectBoxLabel());
        floppyImageEditor.addContentWidgetStyleName(style.attachImageSelectbox());
        isoImageEditor.addContentWidgetStyleName(style.attachImageSelectbox());
        cloudInitEnabledEditor.addContentWidgetStyleName(style.cloudInitLabel());
    }

    @Override
    public void edit(final RunOnceModel object) {
        driver.edit(object);
        runOnceModel = object;

        object.getCustomPropertySheet().getKeyValueLines().getItemsChangedEvent().addListener(new IEventListener() {
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                customPropertiesSheetEditor.edit(object.getCustomPropertySheet());
            }
        });

        // Update Linux options panel
        final EntityModel isLinuxOptionsAvailable = object.getIsLinuxOptionsAvailable();
        object.getIsLinuxOptionsAvailable().getEntityChangedEvent().addListener(new IEventListener() {
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                boolean isLinux = (Boolean) isLinuxOptionsAvailable.getEntity();
                linuxBootOptionsPanel.setVisible(isLinux);
            }
        });

        object.getIsCloudInitEnabled().getEntityChangedEvent().addListener(new IEventListener() {
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                boolean selected = (Boolean) object.getIsCloudInitEnabled().getEntity();
                cloudInitWidget.setVisible(selected);
            }
        });
        object.getIsSysprepEnabled().getEntityChangedEvent().addListener(new IEventListener() {
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                boolean selected = (Boolean) object.getIsSysprepEnabled().getEntity();
                sysprepOptions.setVisible(selected);
                sysprepToEnableLabel.setVisible(!selected);
            }
        });

        object.getIsCloudInitPossible().getEntityChangedEvent().addListener(new IEventListener() {
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                cloudInitSubPanel.setVisible((Boolean) object.getIsCloudInitPossible().getEntity());
                initialRunPanel.setVisible(cloudInitSubPanel.isVisible() || sysprepSubPanel.isVisible());
            }
        });

        object.getIsSysprepPossible().getEntityChangedEvent().addListener(new IEventListener() {
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                sysprepSubPanel.setVisible((Boolean) object.getIsSysprepPossible().getEntity());
                initialRunPanel.setVisible(cloudInitSubPanel.isVisible() || sysprepSubPanel.isVisible());
            }
        });

        // Update Host combo
        object.getIsAutoAssign().getPropertyChangedEvent()
                .addListener(new IEventListener() {
                    @Override
                    public void eventRaised(Event ev, Object sender, EventArgs args) {
                        boolean isAutoAssign = (Boolean) object.getIsAutoAssign().getEntity();
                        defaultHostEditor.setEnabled(!isAutoAssign);
                        // only this is not bind to the model, so needs to
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

        object.getIsAutoAssign().getEntityChangedEvent().addListener(new IEventListener() {
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                if (!isAutoAssignEditor.asRadioButton().getValue())
                    specificHost.setValue(true, true);
            }
        });

        object.getPropertyChangedEvent().addListener(new IEventListener() {
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                String propName = ((PropertyChangedEventArgs) args).PropertyName;
                if ("IsHostTabVisible".equals(propName)) { //$NON-NLS-1$
                    hostPanel.setVisible(object.getIsHostTabVisible());
                } else if ("IsCustomPropertiesSheetVisible".equals(propName)) { //$NON-NLS-1$
                    customPropertiesPanel.setVisible(object.getIsCustomPropertiesSheetVisible());
                }
            }
        });

        // Update BootSequence ListBox
        bootSequenceModel = object.getBootSequence();
        UpdateBootSequenceListBox();

        cloudInitWidget.edit(object.getCloudInit());
    }

    @UiHandler("refreshButton")
    void handleRefreshButtonClick(ClickEvent event) {
        runOnceModel.updateIsoList(true);
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

    private void UpdateBootSequenceListBox() {
        // Update Items
        updateBootSequenceItems();

        // Items change handling
        bootSequenceModel.getItems().getCollectionChangedEvent().addListener(new IEventListener() {
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                updateBootSequenceItems();

                // Update selected item
                bootSequenceBox.setSelectedIndex(bootSequenceModel.getSelectedItemIndex());
            }
        });

        // Attach CD change handling
        bootSequenceModel.getCdromOption().getPropertyChangedEvent().addListener(new IEventListener() {
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                boolean isEnabled = bootSequenceModel.getCdromOption().getIsChangable();
                String itemName = bootSequenceModel.getCdromOption().getTitle();
                updateItemAvailability(itemName, isEnabled);
            }
        });

        // NIC change handling
        bootSequenceModel.getNetworkOption().getPropertyChangedEvent().addListener(new IEventListener() {
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                boolean isEnabled = bootSequenceModel.getNetworkOption().getIsChangable();
                String itemName = bootSequenceModel.getNetworkOption().getTitle();
                updateItemAvailability(itemName, isEnabled);
            }
        });

        // Hard disk change handling
        bootSequenceModel.getHardDiskOption().getPropertyChangedEvent().addListener(new IEventListener() {
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
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
        cloudInitWidget.flush();
        return driver.flush();
    }

}
