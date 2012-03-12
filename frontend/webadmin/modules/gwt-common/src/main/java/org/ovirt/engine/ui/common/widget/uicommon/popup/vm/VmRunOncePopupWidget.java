package org.ovirt.engine.ui.common.widget.uicommon.popup.vm;

import org.ovirt.engine.core.compat.Event;
import org.ovirt.engine.core.compat.EventArgs;
import org.ovirt.engine.core.compat.IEventListener;
import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.widget.Align;
import org.ovirt.engine.ui.common.widget.ComboBox;
import org.ovirt.engine.ui.common.widget.editor.EntityModelCheckBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.EntityModelRadioButtonEditor;
import org.ovirt.engine.ui.common.widget.editor.EntityModelTextBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.ListModelListBoxEditor;
import org.ovirt.engine.ui.common.widget.uicommon.popup.AbstractModelBoundPopupWidget;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.BootSequenceModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.RunOnceModel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.ButtonBase;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.VerticalPanel;

public class VmRunOncePopupWidget extends AbstractModelBoundPopupWidget<RunOnceModel> {

    interface Driver extends SimpleBeanEditorDriver<RunOnceModel, VmRunOncePopupWidget> {
        Driver driver = GWT.create(Driver.class);
    }

    interface ViewUiBinder extends UiBinder<FlowPanel, VmRunOncePopupWidget> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    interface Style extends CssResource {
        String attachImageCheckBoxLabel();

        String attachImageSelectBoxLabel();
    }

    @UiField
    Style style;

    @UiField
    @Ignore
    Label bootOptionsLabel;

    @UiField
    @Ignore
    Label displayProtocolLabel;

    @UiField
    @Ignore
    Label linuxBootOptionsLabel;

    @UiField
    @Ignore
    Label windowsSysprepLabel;

    @UiField
    VerticalPanel linuxBootOptionsPanel;

    @UiField
    VerticalPanel windowsSysprepPanel;

    @UiField
    @Path(value = "floppyImage.selectedItem")
    ListModelListBoxEditor<Object> floppyImageEditor;

    @UiField
    @Path(value = "isoImage.selectedItem")
    ListModelListBoxEditor<Object> isoImageEditor;

    @UiField(provided = true)
    @Path(value = "attachFloppy.entity")
    EntityModelCheckBoxEditor attachFloppyEditor;

    @UiField(provided = true)
    @Path(value = "attachIso.entity")
    EntityModelCheckBoxEditor attachIsoEditor;

    @UiField(provided = true)
    @Path(value = "runAsStateless.entity")
    EntityModelCheckBoxEditor runAsStatelessEditor;

    @UiField(provided = true)
    @Path(value = "runAndPause.entity")
    EntityModelCheckBoxEditor runAndPauseEditor;

    @UiField
    @Path(value = "Kernel_path.entity")
    EntityModelTextBoxEditor kernelPathEditor;

    @UiField
    @Path(value = "Initrd_path.entity")
    EntityModelTextBoxEditor initrdPathEditor;

    @UiField
    @Path(value = "Kernel_parameters.entity")
    EntityModelTextBoxEditor kernelParamsEditor;

    @UiField(provided = true)
    ComboBox sysPrepDomainNameComboBox;

    @Path(value = "sysPrepDomainName.selectedItem")
    ListModelListBoxEditor<Object> sysPrepDomainNameListBoxEditor;

    @Path(value = "SysPrepSelectedDomainName.entity")
    EntityModelTextBoxEditor sysPrepDomainNameTextBoxEditor;

    @UiField(provided = true)
    @Path(value = "useAlternateCredentials.entity")
    EntityModelCheckBoxEditor useAlternateCredentialsEditor;

    @UiField
    @Path(value = "sysPrepUserName.entity")
    EntityModelTextBoxEditor sysPrepUserNameEditor;

    @UiField
    @Path(value = "sysPrepPassword.entity")
    EntityModelTextBoxEditor sysPrepPasswordEditor;

    @UiField
    @Path(value = "customProperties.entity")
    EntityModelTextBoxEditor customPropertiesEditor;

    @UiField(provided = true)
    @Path(value = "displayConsole_Vnc_IsSelected.entity")
    EntityModelRadioButtonEditor displayConsoleVncEditor;

    @UiField(provided = true)
    @Path(value = "displayConsole_Spice_IsSelected.entity")
    EntityModelRadioButtonEditor displayConsoleSpiceEditor;

    @UiField
    ButtonBase bootSequenceUpButton;

    @UiField
    ButtonBase bootSequenceDownButton;

    @UiField
    AbsolutePanel bootSequencePanel;

    @UiField
    @Ignore
    Label bootSequenceLabel;

    ListBox bootSequenceBox;

    private BootSequenceModel bootSequenceModel;

    public VmRunOncePopupWidget(CommonApplicationConstants constants) {
        initCheckBoxEditors();
        initRadioButtonEditors();
        initComboBox();
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        initBootSequenceBox();

        localize(constants);
        addStyles();

        Driver.driver.initialize(this);
    }

    void localize(CommonApplicationConstants constants) {
        // Boot Options
        bootOptionsLabel.setText(constants.runOncePopupBootOptionsLabel());
        runAsStatelessEditor.setLabel(constants.runOncePopupRunAsStatelessLabel());
        runAndPauseEditor.setLabel(constants.runOncePopupRunAndPauseLabel());
        attachFloppyEditor.setLabel(constants.runOncePopupAttachFloppyLabel());
        attachIsoEditor.setLabel(constants.runOncePopupAttachIsoLabel());
        customPropertiesEditor.setLabel(constants.runOncePopupCustomPropertiesLabel());
        bootSequenceLabel.setText(constants.runOncePopupBootSequenceLabel());

        // Linux Boot Options
        linuxBootOptionsLabel.setText(constants.runOncePopupLinuxBootOptionsLabel());
        kernelPathEditor.setLabel(constants.runOncePopupKernelPathLabel());
        initrdPathEditor.setLabel(constants.runOncePopupInitrdPathLabel());
        kernelParamsEditor.setLabel(constants.runOncePopupKernelParamsLabel());

        // WindowsSysprep
        windowsSysprepLabel.setText(constants.runOncePopupWindowsSysprepLabel());
        sysPrepDomainNameListBoxEditor.setLabel(constants.runOncePopupSysPrepDomainNameLabel());
        useAlternateCredentialsEditor.setLabel(constants.runOnceUseAlternateCredentialsLabel());
        sysPrepUserNameEditor.setLabel(constants.runOncePopupSysPrepUserNameLabel());
        sysPrepPasswordEditor.setLabel(constants.runOncePopupSysPrepPasswordLabel());

        // Display Protocol
        displayConsoleVncEditor.setLabel(constants.runOncePopupDisplayConsoleVncLabel());
        displayConsoleSpiceEditor.setLabel(constants.runOncePopupDisplayConsoleSpiceLabel());
        displayProtocolLabel.setText(constants.runOncePopupDisplayProtocolLabel());
    }

    void initCheckBoxEditors() {
        attachFloppyEditor = new EntityModelCheckBoxEditor(Align.RIGHT);
        attachIsoEditor = new EntityModelCheckBoxEditor(Align.RIGHT);
        runAsStatelessEditor = new EntityModelCheckBoxEditor(Align.RIGHT);
        runAndPauseEditor = new EntityModelCheckBoxEditor(Align.RIGHT);
        useAlternateCredentialsEditor = new EntityModelCheckBoxEditor(Align.RIGHT);
    }

    void initRadioButtonEditors() {
        displayConsoleVncEditor = new EntityModelRadioButtonEditor("1");
        displayConsoleSpiceEditor = new EntityModelRadioButtonEditor("1");
    }

    void initComboBox() {
        sysPrepDomainNameListBoxEditor = new ListModelListBoxEditor<Object>();
        sysPrepDomainNameTextBoxEditor = new EntityModelTextBoxEditor();

        sysPrepDomainNameComboBox = new ComboBox(sysPrepDomainNameListBoxEditor, sysPrepDomainNameTextBoxEditor);
    }

    void initBootSequenceBox() {
        bootSequenceBox = new ListBox(false);
        bootSequenceBox.setWidth("100%");
        bootSequenceBox.setHeight("60px");

        VerticalPanel boxPanel = new VerticalPanel();
        boxPanel.setWidth("100%");
        boxPanel.add(bootSequenceBox);
        bootSequencePanel.add(boxPanel);
    }

    void addStyles() {
        linuxBootOptionsPanel.setVisible(false);
        windowsSysprepPanel.setVisible(false);
        attachFloppyEditor.addContentWidgetStyleName(style.attachImageCheckBoxLabel());
        attachIsoEditor.addContentWidgetStyleName(style.attachImageCheckBoxLabel());
        floppyImageEditor.addLabelStyleName(style.attachImageSelectBoxLabel());
        isoImageEditor.addLabelStyleName(style.attachImageSelectBoxLabel());
    }

    @Override
    public void edit(RunOnceModel object) {
        Driver.driver.edit(object);

        // Update Linux options panel
        final EntityModel isLinuxOptionsAvailable = object.getIsLinuxOptionsAvailable();
        object.getIsLinuxOptionsAvailable().getEntityChangedEvent().addListener(new IEventListener() {
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                linuxBootOptionsPanel.setVisible((Boolean) isLinuxOptionsAvailable.getEntity());
            }
        });

        // Update Windows Sysprep options panel
        final EntityModel isSysprepEnabled = object.getIsSysprepEnabled();
        object.getIsSysprepEnabled().getEntityChangedEvent().addListener(new IEventListener() {
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                windowsSysprepPanel.setVisible((Boolean) isSysprepEnabled.getEntity());
            }
        });

        // Update BootSequence ListBox
        bootSequenceModel = object.getBootSequence();
        UpdateBootSequenceListBox();
    }

    @UiHandler("bootSequenceUpButton")
    void handleBootSequenceUpButtonClick(ClickEvent event) {
        if (bootSequenceModel != null) {
            bootSequenceModel.ExecuteCommand(bootSequenceModel.getMoveItemUpCommand());
        }
    }

    @UiHandler("bootSequenceDownButton")
    void handleBootSequenceDownButtonClick(ClickEvent event) {
        if (bootSequenceModel != null) {
            bootSequenceModel.ExecuteCommand(bootSequenceModel.getMoveItemDownCommand());
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

        // Change boot option handling
        bootSequenceBox.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) {
                int selectedIndex = bootSequenceBox.getSelectedIndex();
                bootSequenceModel.setSelectedItem(bootSequenceModel.getItems().get(selectedIndex));

                bootSequenceUpButton.setEnabled(bootSequenceModel.getMoveItemUpCommand().getIsExecutionAllowed());
                bootSequenceDownButton.setEnabled(bootSequenceModel.getMoveItemDownCommand().getIsExecutionAllowed());
            }
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
                NodeList<Element> options = bootSequenceBox.getElement().getElementsByTagName("option");
                if (!isEnabled) {
                    options.getItem(i).setAttribute("disabled", "disabled");
                } else {
                    options.getItem(i).removeAttribute("disabled");
                }
            }
        }
    }

    @Override
    public RunOnceModel flush() {
        return Driver.driver.flush();
    }

}
