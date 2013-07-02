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
import org.ovirt.engine.ui.uicommonweb.models.vms.VncConsoleModel;

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
    EntityModelRadioButtonEditor spiceAutoImplRadioButton;

    @UiField(provided = true)
    @WithElementId
    EntityModelRadioButtonEditor spiceNativeImplRadioButton;

    @UiField(provided = true)
    @WithElementId
    EntityModelRadioButtonEditor spicePluginImplRadioButton;

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
    EntityModelValueCheckBoxEditor<ConsoleModel> enableSpiceProxy;

    @UiField(provided = true)
    @WithElementId
    EntityModelValueCheckBoxEditor<ConsoleModel> useLocalDrives;

    @UiField(provided = true)
    @WithElementId
    EntityModelValueCheckBoxEditor<ConsoleModel> disableSmartcard;

    @UiField
    FlowPanel disableSmartcardPanel;

    @UiField(provided = true)
    @WithElementId
    EntityModelRadioButtonEditor vncNativeImplRadioButton;

    @UiField(provided = true)
    @WithElementId
    EntityModelRadioButtonEditor noVncImplRadioButton;

    @UiField(provided = true)
    @WithElementId
    EntityModelRadioButtonEditor rdpAutoImplRadioButton;

    @UiField(provided = true)
    @WithElementId
    EntityModelRadioButtonEditor rdpNativeImplRadioButton;

    @UiField(provided = true)
    @WithElementId
    EntityModelRadioButtonEditor rdpPluginImplRadioButton;

    @UiField
    FlowPanel spicePanel;

    @UiField
    FlowPanel vncPanel;

    @UiField
    FlowPanel rdpPanel;

    @UiField
    FlowPanel wanOptionsPanel;

    @UiField(provided = true)
    @WithElementId
    EntityModelValueCheckBoxEditor<ConsoleModel> wanEnabled;

    private final CommonApplicationConstants constants;

    private final CommonApplicationMessages messages;

    private ConsolePopupModel model;

    @Inject
    public ConsolePopupView(EventBus eventBus,
            CommonApplicationResources resources,
            CommonApplicationConstants constants,
            CommonApplicationMessages messages) {
        super(eventBus, resources);
        this.constants = constants;
        this.messages = messages;

        spiceRadioButton = new EntityModelRadioButtonEditor("1"); //$NON-NLS-1$
        spiceRadioButton.setLabel(constants.spice());

        spiceAutoImplRadioButton = new EntityModelRadioButtonEditor("2"); //$NON-NLS-1$
        spiceAutoImplRadioButton.setLabel(constants.auto());
        spiceNativeImplRadioButton = new EntityModelRadioButtonEditor("2");// $NON-NLS-1$
        spiceNativeImplRadioButton.setLabel(constants.nativeClient());
        spicePluginImplRadioButton = new EntityModelRadioButtonEditor("2"); //$NON-NLS-1$
        spicePluginImplRadioButton.setLabel(constants.browserPlugin());

        vncNativeImplRadioButton = new EntityModelRadioButtonEditor("3"); //$NON-NLS-1$
        vncNativeImplRadioButton.setLabel(constants.nativeClient());
        noVncImplRadioButton = new EntityModelRadioButtonEditor("3"); //$NON-NLS-1$
        noVncImplRadioButton.setLabel(constants.noVnc());

        rdpAutoImplRadioButton = new EntityModelRadioButtonEditor("4"); //$NON-NLS-1$
        rdpAutoImplRadioButton.setLabel(constants.auto());
        rdpNativeImplRadioButton = new EntityModelRadioButtonEditor("4");// $NON-NLS-1$
        rdpNativeImplRadioButton.setLabel(constants.nativeClient());
        rdpPluginImplRadioButton = new EntityModelRadioButtonEditor("4"); //$NON-NLS-1$
        rdpPluginImplRadioButton.setLabel(constants.browserPlugin());

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

        enableSpiceProxy = new EntityModelValueCheckBoxEditor<ConsoleModel>(Align.RIGHT, new SpiceRenderer() {

            @Override
            protected void updateModel(ISpice spice, boolean value) {
                spice.setSpiceProxyEnabled(value);
            }

            @Override
            protected boolean extractBoolean(ISpice spice) {
                return spice.isSpiceProxyEnabled();
            }
        });
        enableSpiceProxy.setLabel(constants.enableSpiceProxy());

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
        vncPanel.setVisible(false);
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
                enableSpiceProxy,
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
        if (spiceRadioButton.asRadioButton().getValue()) {
            setSelectedProtocol(ConsoleProtocol.SPICE);
            setSelectedSpiceImpl();
        } else if (remoteDesktopRadioButton.asRadioButton().getValue()) {
            setSelectedProtocol(ConsoleProtocol.RDP);
            setSelectedRdpImpl();
        } else if (vncRadioButton.asRadioButton().getValue()) {
            setSelectedProtocol(ConsoleProtocol.VNC);
            setSelectedVncImpl();
        }

        flushCheckBoxes(
                ctrlAltDel,
                enableUsbAutoshare,
                openInFullScreen,
                enableSpiceProxy,
                useLocalDrives,
                wanEnabled,
                disableSmartcard);
    }

    private void setSelectedSpiceImpl() {
        SpiceConsoleModel spiceModel = null;

        if (model.getModel().getDefaultConsoleModel() instanceof SpiceConsoleModel) {
            spiceModel = (SpiceConsoleModel) model.getModel().getDefaultConsoleModel();
        }

        if (spiceModel == null) {
            return;
        }

        if (spiceAutoImplRadioButton.asRadioButton().getValue()) {
            spiceModel.setSpiceImplementation(SpiceConsoleModel.ClientConsoleMode.Auto);
        } else if (spiceNativeImplRadioButton.asRadioButton().getValue()) {
            spiceModel.setSpiceImplementation(SpiceConsoleModel.ClientConsoleMode.Native);
        } else if (spicePluginImplRadioButton.asRadioButton().getValue()) {
            spiceModel.setSpiceImplementation(SpiceConsoleModel.ClientConsoleMode.Plugin);
        }
    }

    private void setSelectedVncImpl() {
        Object defConsoleModel = model.getModel().getDefaultConsoleModel();
        VncConsoleModel vncConsoleModel = defConsoleModel instanceof VncConsoleModel ?
                (VncConsoleModel) defConsoleModel : null;

        if (vncConsoleModel == null) {
            return;
        }

        if (noVncImplRadioButton.asRadioButton().getValue()) {
            vncConsoleModel.setVncImplementation(VncConsoleModel.ClientConsoleMode.NoVnc);
        } else {
            vncConsoleModel.setVncImplementation(VncConsoleModel.ClientConsoleMode.Native);
        }
    }

    private void setSelectedRdpImpl() {
        if (model.getModel().getAdditionalConsoleModel() == null) {
            return;
        }

        RdpConsoleModel rdpModel = (RdpConsoleModel) model.getModel().getAdditionalConsoleModel();

        if (rdpAutoImplRadioButton.asRadioButton().getValue()) {
            rdpModel.setRdpImplementation(RdpConsoleModel.ClientConsoleMode.Auto);
        } else if (rdpNativeImplRadioButton.asRadioButton().getValue()) {
            rdpModel.setRdpImplementation(RdpConsoleModel.ClientConsoleMode.Native);
        } else if (rdpPluginImplRadioButton.asRadioButton().getValue()) {
            rdpModel.setRdpImplementation(RdpConsoleModel.ClientConsoleMode.Plugin);
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
        spiceRadioButton.setEnabled(visible);
        spiceRadioButton.setTitle(visible ? "" : constants.spiceNotAvailable());
    }

    @Override
    public void setRdpAvailable(boolean visible) {
        remoteDesktopRadioButton.setEnabled(visible);
        remoteDesktopRadioButton.setTitle(visible ? "" : constants.rdpNotAvailable());
    }

    @Override
    public void setVncAvailable(boolean visible) {
        vncRadioButton.setEnabled(visible);
        vncRadioButton.setTitle(visible ? "" : constants.vncNotAvailable());
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
    public void showRdpPanel(boolean visible) {
        rdpPanel.setVisible(visible);
        if (visible) {
            spicePanel.setVisible(false);
            vncPanel.setVisible(false);
        }
    }

    @Override
    public void showSpicePanel(boolean visible) {
        spicePanel.setVisible(visible);
        if (visible) {
            vncPanel.setVisible(false);
            rdpPanel.setVisible(false);
        }
    }

    @Override
    public void showVncPanel(boolean visible) {
        vncPanel.setVisible(visible);
        if (visible) {
            spicePanel.setVisible(false);
            rdpPanel.setVisible(false);
        }
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

    @Override
    public void setNoVncEnabled(boolean enabled, String reason) {
        noVncImplRadioButton.setEnabled(enabled);
        noVncImplRadioButton.setTitle(enabled ? "" : reason);
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

    @Override
    public void setSpiceProxyEnabled(boolean enabled, String reason) {
        enableSpiceProxy.setEnabled(enabled);
        if (!enabled) {
            enableSpiceProxy.setTitle(reason);
        }
    }

    @Override
    public void setSpicePluginImplEnabled(boolean enabled, String reason) {
        spicePluginImplRadioButton.setEnabled(enabled);
        if (!enabled) {
            spicePluginImplRadioButton.setTitle(reason);
        }
    }

    @Override
    public void setRdpPluginImplEnabled(boolean enabled, String reason) {
        rdpPluginImplRadioButton.setEnabled(enabled);
        if (!enabled) {
            rdpPluginImplRadioButton.setTitle(reason);
        }
    }

    @Override
    public void selectSpiceImplementation(SpiceConsoleModel.ClientConsoleMode consoleMode) {
        switch (consoleMode) {
        case Native:
            spiceAutoImplRadioButton.asRadioButton().setValue(false);
            spicePluginImplRadioButton.asRadioButton().setValue(false);
            spiceNativeImplRadioButton.asRadioButton().setValue(true);
            break;
        case Plugin:
            spiceAutoImplRadioButton.asRadioButton().setValue(false);
            spicePluginImplRadioButton.asRadioButton().setValue(true);
            spiceNativeImplRadioButton.asRadioButton().setValue(false);
            break;
        default:
            spiceAutoImplRadioButton.asRadioButton().setValue(true);
            spicePluginImplRadioButton.asRadioButton().setValue(false);
            spiceNativeImplRadioButton.asRadioButton().setValue(false);
            break;
        }
    }

    @Override
    public void selectVncImplementation(VncConsoleModel.ClientConsoleMode clientConsoleMode) {
        switch (clientConsoleMode) {
            case NoVnc:
                noVncImplRadioButton.asRadioButton().setValue(true);
                vncNativeImplRadioButton.asRadioButton().setValue(false);
                break;
            default:
                noVncImplRadioButton.asRadioButton().setValue(false);
                vncNativeImplRadioButton.asRadioButton().setValue(true);
                break;
        }
    }

    @Override
    public void selectRdpImplementation(RdpConsoleModel.ClientConsoleMode consoleMode) {
        switch (consoleMode) {
        case Native:
            rdpAutoImplRadioButton.asRadioButton().setValue(false);
            rdpPluginImplRadioButton.asRadioButton().setValue(false);
            rdpNativeImplRadioButton.asRadioButton().setValue(true);
            break;
        case Plugin:
            rdpAutoImplRadioButton.asRadioButton().setValue(false);
            rdpPluginImplRadioButton.asRadioButton().setValue(true);
            rdpNativeImplRadioButton.asRadioButton().setValue(false);
            break;
        default:
            rdpAutoImplRadioButton.asRadioButton().setValue(true);
            rdpPluginImplRadioButton.asRadioButton().setValue(false);
            rdpNativeImplRadioButton.asRadioButton().setValue(false);
            break;
        }
    }

    @Override
    public HasValueChangeHandlers<Boolean> getSpiceAutoImplRadioButton() {
        return spiceAutoImplRadioButton.asRadioButton();
    }

    @Override
    public HasValueChangeHandlers<Boolean> getSpiceNativeImplRadioButton() {
        return spiceNativeImplRadioButton.asRadioButton();
    }

    @Override
    public HasValueChangeHandlers<Boolean> getSpicePluginImplRadioButton() {
        return spicePluginImplRadioButton.asRadioButton();
    }

    @Override
    public HasValueChangeHandlers<Boolean> getRdpAutoImplRadioButton() {
        return rdpAutoImplRadioButton.asRadioButton();
    }

    @Override
    public HasValueChangeHandlers<Boolean> getRdpNativeImplRadioButton() {
        return rdpNativeImplRadioButton.asRadioButton();
    }

    @Override
    public HasValueChangeHandlers<Boolean> getRdpPluginImplRadioButton() {
        return rdpPluginImplRadioButton.asRadioButton();
    }

    public HasValueChangeHandlers<Boolean> getNoVncImplRadioButton() {
        return noVncImplRadioButton.asRadioButton();
    }

    public HasValueChangeHandlers<Boolean> getVncNativeImplRadioButton() {
        return vncNativeImplRadioButton.asRadioButton();
    }
}
