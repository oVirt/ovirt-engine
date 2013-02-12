package org.ovirt.engine.ui.common.view.popup;

import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.CommonApplicationMessages;
import org.ovirt.engine.ui.common.CommonApplicationResources;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.idhandler.WithElementId;
import org.ovirt.engine.ui.common.presenter.popup.ConsolePopupPresenterWidget;
import org.ovirt.engine.ui.common.widget.Align;
import org.ovirt.engine.ui.common.widget.dialog.SimpleDialogPanel;
import org.ovirt.engine.ui.common.widget.editor.EntityModelRadioButtonEditor;
import org.ovirt.engine.ui.common.widget.uicommon.popup.console.EntityModelValueCheckBoxEditor;
import org.ovirt.engine.ui.common.widget.uicommon.popup.console.EntityModelValueCheckbox.ValueCheckboxRenderer;
import org.ovirt.engine.ui.uicommonweb.models.ConsolePopupModel;
import org.ovirt.engine.ui.uicommonweb.models.ConsoleProtocol;
import org.ovirt.engine.ui.uicommonweb.models.vms.ConsoleModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.ISpice;
import org.ovirt.engine.ui.uicommonweb.models.vms.RdpConsoleModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.SpiceConsoleModel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.inject.Inject;

public class ConsolePopupView extends AbstractModelBoundPopupView<ConsolePopupModel> implements ConsolePopupPresenterWidget.ViewDef {

    interface ViewUiBinder extends UiBinder<SimpleDialogPanel, ConsolePopupView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    interface ViewIdHandler extends ElementIdHandler<ConsolePopupView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    interface Style extends CssResource {

        String ctrlAltDelContentWidget();

    }

    @UiField
    Style style;

    @UiField
    Label consoleTitle;

    @UiField(provided = true)
    @WithElementId
    EntityModelRadioButtonEditor spiceRadioButton;

    @UiField(provided = true)
    @WithElementId
    EntityModelRadioButtonEditor remoteDesktopRadioButton;

    @UiField(provided = true)
    @WithElementId
    EntityModelRadioButtonEditor vncRadioButton;

    @UiField(provided = true)
    @WithElementId
    EntityModelValueCheckBoxEditor<ConsoleModel> ctrlAltDel;

    @UiField(provided = true)
    @WithElementId
    EntityModelValueCheckBoxEditor<ConsoleModel> enableUsbAutoshare;

    @UiField(provided = true)
    @WithElementId
    EntityModelValueCheckBoxEditor<ConsoleModel> openInFullScreen;

    @UiField(provided = true)
    @WithElementId
    EntityModelValueCheckBoxEditor<ConsoleModel> useLocalDrives;

    @UiField(provided = true)
    @WithElementId
    EntityModelValueCheckBoxEditor<ConsoleModel> disableSmartcard;

    @UiField
    FlowPanel disableSmartcardPanel;

    @UiField
    FlowPanel spicePanel;

    @UiField
    FlowPanel rdpPanel;

    @UiField
    FlowPanel wanOptionsPanel;

    @UiField(provided = true)
    @WithElementId
    EntityModelValueCheckBoxEditor<ConsoleModel> wanEnabled;

    private final CommonApplicationMessages messages;

    private ConsolePopupModel model;

    @Inject
    public ConsolePopupView(EventBus eventBus,
            CommonApplicationResources resources,
            CommonApplicationConstants constants,
            CommonApplicationMessages messages) {
        super(eventBus, resources);
        this.messages = messages;

        spiceRadioButton = new EntityModelRadioButtonEditor("1"); //$NON-NLS-1$
        spiceRadioButton.setLabel(constants.spice());

        disableSmartcard = new EntityModelValueCheckBoxEditor<ConsoleModel>(Align.RIGHT, new SpiceRenderer() {

            @Override
            protected void updateModel(ISpice spice, boolean value) {
                spice.setOverrideEnabledSmartcard(value);
            }

            @Override
            protected boolean extractBoolean(ISpice spice) {
                return spice.isSmartcardEnabledOverridden();
            }

        });
        disableSmartcard.setLabel(constants.disableSmartcard());

        wanEnabled = new EntityModelValueCheckBoxEditor<ConsoleModel>(Align.RIGHT, new SpiceRenderer() {

            @Override
            protected void updateModel(ISpice spice, boolean value) {
                spice.setWanOptionsEnabled(value);
            }

            @Override
            protected boolean extractBoolean(ISpice spice) {
                return spice.isWanOptionsEnabled();
            }

        });
        wanEnabled.setLabel(constants.enableWanOptions());

        ctrlAltDel = new EntityModelValueCheckBoxEditor<ConsoleModel>(Align.RIGHT, new SpiceRenderer() {
            @Override
            protected void updateModel(ISpice spice, boolean value) {
                spice.setSendCtrlAltDelete(value);
                spice.setNoTaskMgrExecution(value);
            }

            @Override
            protected boolean extractBoolean(ISpice spice) {
                return spice.getSendCtrlAltDelete();
            }

        });

        ctrlAltDel.setLabel(constants.ctrlAltDel());

        enableUsbAutoshare = new EntityModelValueCheckBoxEditor<ConsoleModel>(Align.RIGHT, new SpiceRenderer() {

            @Override
            protected void updateModel(ISpice spice, boolean value) {
                spice.setUsbAutoShare(value);
            }

            @Override
            protected boolean extractBoolean(ISpice spice) {
                return spice.getUsbAutoShare();
            }
        });
        enableUsbAutoshare.setLabel(constants.usbAutoshare());

        openInFullScreen = new EntityModelValueCheckBoxEditor<ConsoleModel>(Align.RIGHT, new SpiceRenderer() {

            @Override
            protected void updateModel(ISpice spice, boolean value) {
                spice.setFullScreen(value);
            }

            @Override
            protected boolean extractBoolean(ISpice spice) {
                return spice.isFullScreen();
            }
        });
        openInFullScreen.setLabel(constants.openInFullScreen());

        useLocalDrives =
                new EntityModelValueCheckBoxEditor<ConsoleModel>(Align.RIGHT,
                        new ValueCheckboxRenderer<ConsoleModel>() {

                            @Override
                            public boolean render(ConsoleModel value) {
                                if (value instanceof RdpConsoleModel) {
                                    return ((RdpConsoleModel) value).getrdp().getUseLocalDrives();
                                }

                                return false;
                            }

                            @Override
                            public ConsoleModel read(boolean value, ConsoleModel model) {
                                if (model instanceof RdpConsoleModel) {
                                    ((RdpConsoleModel) model).getrdp().setUseLocalDrives(value);
                                }

                                return model;
                            }

                        });
        useLocalDrives.setLabel(constants.useLocalDrives());

        remoteDesktopRadioButton = new EntityModelRadioButtonEditor("1"); //$NON-NLS-1$
        remoteDesktopRadioButton.setLabel(constants.remoteDesktop());

        vncRadioButton = new EntityModelRadioButtonEditor("1"); //$NON-NLS-1$
        vncRadioButton.setLabel(constants.vnc());

        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        ViewIdHandler.idHandler.generateAndSetIds(this);

        spicePanel.setVisible(false);
        rdpPanel.setVisible(false);

        ctrlAltDel.getContentWidgetContainer().addStyleName(style.ctrlAltDelContentWidget());
    }

    @SuppressWarnings("unchecked")
    @Override
    public void edit(ConsolePopupModel model) {
        this.model = model;

        ConsoleModel defaultConsole =
                 model.getModel().getDefaultConsoleModel();
        editCheckBoxes(defaultConsole,
                ctrlAltDel,
                enableUsbAutoshare,
                openInFullScreen,
                wanEnabled,
                disableSmartcard);

        ConsoleModel additionalConsole =
                model.getModel().getAdditionalConsoleModel();
        editCheckBoxes(additionalConsole, useLocalDrives);
    }

    @Override
    public ConsolePopupModel flush() {
        // do nothing, it will be flushed only when the presenter widget
        // decides to
        return null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void flushToPrivateModel() {
        flushCheckBoxes(
                ctrlAltDel,
                enableUsbAutoshare,
                openInFullScreen,
                useLocalDrives,
                wanEnabled,
                disableSmartcard);

        if (spiceRadioButton.asRadioButton().getValue()) {
            setSelectedProtocol(ConsoleProtocol.SPICE);
        } else if (remoteDesktopRadioButton.asRadioButton().getValue()) {
            setSelectedProtocol(ConsoleProtocol.RDP);
        } else if (vncRadioButton.asRadioButton().getValue()) {
            setSelectedProtocol(ConsoleProtocol.VNC);
        }

    }

    private void setSelectedProtocol(ConsoleProtocol selectedProtocol) {
        model.getModel().setSelectedProtocol(selectedProtocol);
    }

    private void flushCheckBoxes(EntityModelValueCheckBoxEditor<ConsoleModel>... checkBoxes) {
        for (EntityModelValueCheckBoxEditor<ConsoleModel> checkBox : checkBoxes) {
            checkBox.asEditor().getSubEditor().getValue();
        }
    }

    private void editCheckBoxes(ConsoleModel consoleModel,
            EntityModelValueCheckBoxEditor<ConsoleModel>... checkBoxes) {
        for (EntityModelValueCheckBoxEditor<ConsoleModel> checkBox : checkBoxes) {
            checkBox.asEditor().getSubEditor().setValue(consoleModel);
        }
    }

    @Override
    public void setSpiceAvailable(boolean visible) {
        spiceRadioButton.setVisible(visible);
    }

    @Override
    public void setRdpAvailable(boolean visible) {
        remoteDesktopRadioButton.setVisible(visible);
    }

    @Override
    public void setVncAvailable(boolean visible) {
        vncRadioButton.setVisible(visible);
    }

    @Override
    public HasValueChangeHandlers<Boolean> getSpiceRadioButton() {
        return spiceRadioButton.asRadioButton();
    }

    @Override
    public HasValueChangeHandlers<Boolean> getRdpRadioButton() {
        return remoteDesktopRadioButton.asRadioButton();
    }

    @Override
    public HasValueChangeHandlers<Boolean> getVncRadioButton() {
        return vncRadioButton.asRadioButton();
    }

    @Override
    public void rdpSelected(boolean selected) {
        rdpPanel.setVisible(selected);
    }

    @Override
    public void spiceSelected(boolean selected) {
        spicePanel.setVisible(selected);
    }

    @Override
    public void selectSpice(boolean selected) {
        spiceRadioButton.asRadioButton().setValue(selected);
    }

    @Override
    public void selectRdp(boolean selected) {
        remoteDesktopRadioButton.asRadioButton().setValue(selected);
    }

    @Override
    public void selectVnc(boolean selected) {
        vncRadioButton.asRadioButton().setValue(selected);
    }

    abstract class SpiceRenderer implements ValueCheckboxRenderer<ConsoleModel> {

        @Override
        public boolean render(ConsoleModel value) {
            if (value instanceof SpiceConsoleModel) {
                return extractBoolean(((SpiceConsoleModel) value).getspice());
            }

            return false;
        }

        @Override
        public ConsoleModel read(boolean value, ConsoleModel model) {
            if (model instanceof SpiceConsoleModel) {
                updateModel(((SpiceConsoleModel) model).getspice(), value);
            }

            return model;
        }

        protected abstract void updateModel(ISpice spice, boolean value);

        protected abstract boolean extractBoolean(ISpice spice);
    }

    @Override
    public void setSpiceConsoleAvailable(boolean hasSpiceConsole) {
        ctrlAltDel.setVisible(hasSpiceConsole);
        enableUsbAutoshare.setVisible(hasSpiceConsole);
        openInFullScreen.setVisible(hasSpiceConsole);
    }

    @Override
    public void setAdditionalConsoleAvailable(boolean hasAdditionalConsole) {
        useLocalDrives.setVisible(hasAdditionalConsole);
    }

    @Override
    public void selectWanOptionsEnabled(boolean selected) {
        wanEnabled.asCheckBox().setValue(selected);
    }

    @Override
    public void setWanOptionsVisible(boolean visible) {
        wanOptionsPanel.setVisible(visible);
    }

    @Override
    public void setVmName(String vmName) {
        consoleTitle.setText(messages.selectConsoleFor(vmName));
    }

    @Override
    public void setCtrlAltDelEnabled(boolean enabled, String reason) {
        ctrlAltDel.setEnabled(enabled);
        if (!enabled) {
            ctrlAltDel.setTitle(reason);
        }
    }

    @Override
    public void setDisableSmartcardVisible(boolean visible) {
        disableSmartcardPanel.setVisible(visible);
    }

}
