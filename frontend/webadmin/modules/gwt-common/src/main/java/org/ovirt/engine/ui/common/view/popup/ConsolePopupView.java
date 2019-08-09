package org.ovirt.engine.ui.common.view.popup;

import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.CommonApplicationMessages;
import org.ovirt.engine.ui.common.gin.AssetProvider;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.idhandler.WithElementId;
import org.ovirt.engine.ui.common.presenter.popup.ConsolePopupPresenterWidget;
import org.ovirt.engine.ui.common.widget.Align;
import org.ovirt.engine.ui.common.widget.HasEnabledWithHints;
import org.ovirt.engine.ui.common.widget.WidgetWithInfo;
import org.ovirt.engine.ui.common.widget.WidgetWithTooltippedIcon;
import org.ovirt.engine.ui.common.widget.dialog.SimpleDialogPanel;
import org.ovirt.engine.ui.common.widget.editor.generic.EntityModelRadioButtonEditor;
import org.ovirt.engine.ui.common.widget.tooltip.WidgetTooltip;
import org.ovirt.engine.ui.common.widget.uicommon.popup.console.EntityModelValueCheckBoxEditor;
import org.ovirt.engine.ui.common.widget.uicommon.popup.console.EntityModelValueCheckbox.ValueCheckboxRenderer;
import org.ovirt.engine.ui.uicommonweb.DynamicMessages;
import org.ovirt.engine.ui.uicommonweb.models.ConsolePopupModel;
import org.ovirt.engine.ui.uicommonweb.models.ConsoleProtocol;
import org.ovirt.engine.ui.uicommonweb.models.vms.ConsoleClient;
import org.ovirt.engine.ui.uicommonweb.models.vms.ConsoleModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.RdpConsoleModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.SpiceConsoleModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.VncConsoleModel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Anchor;
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
        String remapCADContentWidget();
        String consoleResourcesLink();
        String labelStyle();
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
    EntityModelValueCheckBoxEditor<ConsoleModel> remapCtrlAltDeleteSpice;

    @UiField(provided = true)
    @WithElementId
    EntityModelValueCheckBoxEditor<ConsoleModel> remapCtrlAltDeleteVnc;

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

    @Ignore
    Anchor clientConsoleResourcesUrl;

    @UiField
    FlowPanel spicePanel;

    @UiField(provided = true)
    WidgetWithTooltippedIcon vncHeadline;

    @UiField(provided = true)
    WidgetWithTooltippedIcon rdpHeadline;

    @UiField
    FlowPanel vncPanel;

    @UiField
    FlowPanel rdpPanel;

    @UiField
    FlowPanel wanOptionsPanel;

    @UiField(provided = true)
    @WithElementId
    EntityModelValueCheckBoxEditor<ConsoleModel> wanEnabled;

    @UiField
    WidgetTooltip spiceRadioButtonTooltip;

    @UiField
    WidgetTooltip vncRadioButtonTooltip;

    @UiField
    WidgetTooltip remoteDesktopRadioButtonTooltip;

    @UiField
    WidgetTooltip noVncImplRadioButtonTooltip;

    @UiField
    WidgetTooltip rdpPluginImplRadioButtonTooltip;

    @UiField
    WidgetTooltip enableSpiceProxyTooltip;

    private ConsolePopupModel model;

    private static final CommonApplicationConstants constants = AssetProvider.getConstants();
    private static final CommonApplicationMessages messages = AssetProvider.getMessages();


    @Inject
    public ConsolePopupView(EventBus eventBus, final DynamicMessages dynamicMessages) {
        super(eventBus);

        spiceRadioButton = new EntityModelRadioButtonEditor("1"); //$NON-NLS-1$
        spiceRadioButton.setLabel(constants.spice());

        vncNativeImplRadioButton = new EntityModelRadioButtonEditor("3"); //$NON-NLS-1$
        vncNativeImplRadioButton.setLabel(constants.nativeClient());
        noVncImplRadioButton = new EntityModelRadioButtonEditor("3"); //$NON-NLS-1$
        noVncImplRadioButton.setLabel(constants.noVnc());

        rdpAutoImplRadioButton = new EntityModelRadioButtonEditor("4"); //$NON-NLS-1$
        rdpAutoImplRadioButton.setLabel(constants.auto());
        rdpNativeImplRadioButton = new EntityModelRadioButtonEditor("4"); //$NON-NLS-1$
        rdpNativeImplRadioButton.setLabel(constants.nativeClient());
        rdpPluginImplRadioButton = new EntityModelRadioButtonEditor("4"); //$NON-NLS-1$
        rdpPluginImplRadioButton.setLabel(constants.browserPlugin());

        clientConsoleResourcesUrl = new Anchor(dynamicMessages.consoleClientResources());

        disableSmartcard = new EntityModelValueCheckBoxEditor<>(Align.RIGHT, new SpiceRenderer() {

            @Override
            protected void updateModel(ConsoleClient spice, boolean value) {
                spice.getOptions().setSmartcardEnabledOverridden(value);
            }

            @Override
            protected boolean extractBoolean(ConsoleClient spice) {
                return spice.getOptions().isSmartcardEnabledOverridden();
            }

        });
        disableSmartcard.setLabel(constants.disableSmartcard());

        wanEnabled = new EntityModelValueCheckBoxEditor<>(Align.RIGHT, new SpiceRenderer() {

            @Override
            protected void updateModel(ConsoleClient spice, boolean value) {
                spice.getOptions().setWanOptionsEnabled(value);
            }

            @Override
            protected boolean extractBoolean(ConsoleClient spice) {
                return spice.getOptions().isWanOptionsEnabled();
            }

        });
        wanEnabled.setLabel(constants.enableWanOptions());

        remapCtrlAltDeleteSpice = new EntityModelValueCheckBoxEditor<>(Align.RIGHT, new SpiceRenderer() {
            @Override
            protected void updateModel(ConsoleClient spice, boolean value) {
                spice.getOptions().setRemapCtrlAltDelete(value);
            }

            @Override
            protected boolean extractBoolean(ConsoleClient spice) {
                return spice.getOptions().isRemapCtrlAltDelete();
            }

        });

        remapCtrlAltDeleteVnc = new EntityModelValueCheckBoxEditor<>(Align.RIGHT, new VncRenderer() {
            @Override
            protected void updateModel(ConsoleClient vnc, boolean value) {
                vnc.getOptions().setRemapCtrlAltDelete(value);
            }

            @Override
            protected boolean extractBoolean(ConsoleClient vnc) {
                return vnc.getOptions().isRemapCtrlAltDelete();
            }
        });

        enableUsbAutoshare = new EntityModelValueCheckBoxEditor<>(Align.RIGHT, new SpiceRenderer() {

            @Override
            protected void updateModel(ConsoleClient spice, boolean value) {
                spice.getOptions().setUsbAutoShare(value);
            }

            @Override
            protected boolean extractBoolean(ConsoleClient spice) {
                return spice.getOptions().isUsbAutoShare();
            }
        });
        enableUsbAutoshare.setLabel(constants.usbAutoshare());

        openInFullScreen = new EntityModelValueCheckBoxEditor<>(Align.RIGHT, new SpiceRenderer() {

            @Override
            protected void updateModel(ConsoleClient spice, boolean value) {
                spice.getOptions().setFullScreen(value);
            }

            @Override
            protected boolean extractBoolean(ConsoleClient spice) {
                return spice.getOptions().isFullScreen();
            }
        });
        openInFullScreen.setLabel(constants.openInFullScreen());

        enableSpiceProxy = new EntityModelValueCheckBoxEditor<>(Align.RIGHT, new SpiceRenderer() {

            @Override
            protected void updateModel(ConsoleClient spice, boolean value) {
                spice.getOptions().setSpiceProxyEnabled(value);
            }

            @Override
            protected boolean extractBoolean(ConsoleClient spice) {
                return spice.getOptions().isSpiceProxyEnabled();
            }
        });
        enableSpiceProxy.setLabel(constants.enableSpiceProxy());

        useLocalDrives = new EntityModelValueCheckBoxEditor<>(Align.RIGHT, new ValueCheckboxRenderer<ConsoleModel>() {

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

        Label spiceInvocationLabel = new Label();
        spiceInvocationLabel.setText(constants.consoleInvocation());
        Label vncInvocationLabel = new Label();
        vncInvocationLabel.setText(constants.consoleInvocation());
        Label rdpInvocationLabel = new Label();
        rdpInvocationLabel.setText(constants.consoleInvocation());

        vncHeadline= new WidgetWithInfo(vncInvocationLabel);
        vncHeadline.setIconTooltipText(SafeHtmlUtils.fromTrustedString(createVncInvocationInfo()));
        rdpHeadline= new WidgetWithInfo(rdpInvocationLabel);
        rdpHeadline.setIconTooltipText(SafeHtmlUtils.fromTrustedString(createRdpInvocationInfo()));

        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        ViewIdHandler.idHandler.generateAndSetIds(this);

        spicePanel.setVisible(false);
        vncPanel.setVisible(false);
        rdpPanel.setVisible(false);

        clientConsoleResourcesUrl.asWidget().addStyleName(style.consoleResourcesLink());
        remapCtrlAltDeleteSpice.getContentWidgetContainer().addStyleName(style.remapCADContentWidget());
        remapCtrlAltDeleteVnc.getContentWidgetContainer().addStyleName(style.remapCADContentWidget());
        asWidget().addStatusWidget(clientConsoleResourcesUrl);
        spiceInvocationLabel.addStyleName(style.labelStyle());
        vncInvocationLabel.addStyleName(style.labelStyle());
        rdpInvocationLabel.addStyleName(style.labelStyle());
    }

    private String createVncInvocationInfo() {
        return new KeyValueHtmlRowMaker(constants.nativeClient(), constants.consoleInvokeNative())
                .append(constants.noVnc(), constants.invokeNoVnc())
                .toString();
    }

    private String createRdpInvocationInfo() {
        return new KeyValueHtmlRowMaker(constants.auto(), constants.rdpInvokeAuto())
                .append(constants.nativeClient(), constants.rdpInvokeNative())
                .append(constants.browserPlugin(), constants.rdpInvokePlugin())
                .toString();
    }

    private class KeyValueHtmlRowMaker {

        private String html;

        private KeyValueHtmlRowMaker(String key, String val) {
            html = "<b>" + key + "</b>: " + val; //$NON-NLS-1$ //$NON-NLS-2$
        }

        public KeyValueHtmlRowMaker append(String key, String val) {
            html += "<br/>" + new KeyValueHtmlRowMaker(key, val).toString(); //$NON-NLS-1$
            return this;
        }

        @Override
        public String toString() {
            return html;
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void edit(ConsolePopupModel model) {
        this.model = model;

        editCheckBoxes(model.getVmConsoles().getConsoleModel(SpiceConsoleModel.class),
                remapCtrlAltDeleteSpice,
                enableUsbAutoshare,
                openInFullScreen,
                enableSpiceProxy,
                wanEnabled,
                disableSmartcard);
        editCheckBoxes(model.getVmConsoles().getConsoleModel(VncConsoleModel.class), remapCtrlAltDeleteVnc);
        editCheckBoxes(model.getVmConsoles().getConsoleModel(RdpConsoleModel.class), useLocalDrives);
    }

    @Override
    public ConsolePopupModel flush() {
        // do nothing, it will be flushed only when the presenter widget
        // decides to
        return null;
    }

    @Override
    public void cleanup() {
        // TODO clean up stuff if needed
    }

    @SuppressWarnings("unchecked")
    @Override
    public void flushToPrivateModel() {
        if (spiceRadioButton.asRadioButton().getValue()) {
            setSelectedProtocol(ConsoleProtocol.SPICE);
            initSelectedSpiceImpl();
        } else if (remoteDesktopRadioButton.asRadioButton().getValue()) {
            setSelectedProtocol(ConsoleProtocol.RDP);
            setSelectedRdpImpl();
        } else if (vncRadioButton.asRadioButton().getValue()) {
            setSelectedProtocol(ConsoleProtocol.VNC);
            setSelectedVncImpl();
        }

        flushCheckBoxes(
                remapCtrlAltDeleteSpice,
                remapCtrlAltDeleteVnc,
                enableUsbAutoshare,
                openInFullScreen,
                enableSpiceProxy,
                useLocalDrives,
                wanEnabled,
                disableSmartcard);
    }

    @Override
    public void setCtrlAltDeleteRemapHotkey(String hotkey) {
        remapCtrlAltDeleteSpice.setLabel(messages.remapCtrlAltDelete(hotkey));
        remapCtrlAltDeleteVnc.setLabel(messages.remapCtrlAltDelete(hotkey));
    }

    private void initSelectedSpiceImpl() {
        SpiceConsoleModel spiceModel = model.getVmConsoles().getConsoleModel(SpiceConsoleModel.class);

        if (spiceModel == null) {
            return;
        }
        spiceModel.initConsole();
    }

    private void setSelectedVncImpl() {
        VncConsoleModel vncConsoleModel= model.getVmConsoles().getConsoleModel(VncConsoleModel.class);

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
        RdpConsoleModel rdpModel = model.getVmConsoles().getConsoleModel(RdpConsoleModel.class);

        if (rdpAutoImplRadioButton.asRadioButton().getValue()) {
            rdpModel.setRdpImplementation(RdpConsoleModel.ClientConsoleMode.Auto);
        } else if (rdpNativeImplRadioButton.asRadioButton().getValue()) {
            rdpModel.setRdpImplementation(RdpConsoleModel.ClientConsoleMode.Native);
        } else if (rdpPluginImplRadioButton.asRadioButton().getValue()) {
            rdpModel.setRdpImplementation(RdpConsoleModel.ClientConsoleMode.Plugin);
        }
    }

    private void setSelectedProtocol(ConsoleProtocol selectedProtocol) {
        model.getVmConsoles().selectProtocol(selectedProtocol);
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
        spiceRadioButtonTooltip.setText(visible ? "" : constants.spiceNotAvailable()); //$NON-NLS-1$
    }

    @Override
    public void setRdpAvailable(boolean visible) {
        remoteDesktopRadioButton.setEnabled(visible);
        remoteDesktopRadioButtonTooltip.setText(visible ? "" : constants.rdpNotAvailable()); //$NON-NLS-1$
    }

    @Override
    public void setVncAvailable(boolean visible) {
        vncRadioButton.setEnabled(visible);
        vncRadioButtonTooltip.setText(visible ? "" : constants.vncNotAvailable()); //$NON-NLS-1$
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
        noVncImplRadioButtonTooltip.setText(enabled ? "" : reason); //$NON-NLS-1$
    }

    abstract class SpiceRenderer implements ValueCheckboxRenderer<ConsoleModel> {

        @Override
        public boolean render(ConsoleModel value) {
            if (value instanceof SpiceConsoleModel) {
                return extractBoolean(((SpiceConsoleModel) value).getSpiceImpl());
            }

            return false;
        }

        @Override
        public ConsoleModel read(boolean value, ConsoleModel model) {
            if (model instanceof SpiceConsoleModel) {
                updateModel(((SpiceConsoleModel) model).getSpiceImpl(), value);
            }

            return model;
        }

        protected abstract void updateModel(ConsoleClient spice, boolean value);

        protected abstract boolean extractBoolean(ConsoleClient spice);
    }

    private abstract class VncRenderer implements ValueCheckboxRenderer<ConsoleModel> {


        @Override
        public boolean render(ConsoleModel value) {
            if (value instanceof VncConsoleModel) {
                return extractBoolean(((VncConsoleModel) value).getVncImpl());
            }

            return false;
        }

        @Override
        public ConsoleModel read(boolean value, ConsoleModel model) {
            if (model instanceof VncConsoleModel) {
                updateModel(((VncConsoleModel) model).getVncImpl(), value);
            }

            return model;
        }

        protected abstract void updateModel(ConsoleClient spice, boolean value);

        protected abstract boolean extractBoolean(ConsoleClient spice);
    }


    @Override
    public void setSpiceConsoleAvailable(boolean hasSpiceConsole) {
        remapCtrlAltDeleteSpice.setVisible(hasSpiceConsole);
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
    public void setDisableSmartcardVisible(boolean visible) {
        disableSmartcardPanel.setVisible(visible);
    }

    @Override
    public void setSpiceProxyEnabled(boolean enabled, String reason) {
        enableSpiceProxy.setEnabled(enabled);
        enableSpiceProxyTooltip.setText(reason);
    }

    @Override
    public void setSpiceProxy(boolean enabled) {
        enableSpiceProxy.asCheckBox().setValue(enabled);
    }

    @Override
    public Boolean getSpiceProxy() {
        return enableSpiceProxy.asCheckBox().getValue();
    }

    @Override
    public void setRdpPluginImplEnabled(boolean enabled, String reason) {
        rdpPluginImplRadioButton.setEnabled(enabled);
        if (!enabled) {
            rdpPluginImplRadioButtonTooltip.setText(reason);
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

    @Override
    public HasValueChangeHandlers<Boolean> getSpiceProxyEnabledCheckBox() {
        return enableSpiceProxy.asCheckBox();
    }

    @Override
    public HasClickHandlers getConsoleClientResourcesAnchor() {
        return clientConsoleResourcesUrl;
    }

    @Override
    public HasValueChangeHandlers<Boolean> getNoVncImplRadioButton() {
        return noVncImplRadioButton.asRadioButton();
    }

    @Override
    public HasValueChangeHandlers<Boolean> getVncNativeImplRadioButton() {
        return vncNativeImplRadioButton.asRadioButton();
    }

    @Override
    public HasEnabledWithHints getEnableUsbAutoshare() {
        return enableUsbAutoshare;
    }
}
