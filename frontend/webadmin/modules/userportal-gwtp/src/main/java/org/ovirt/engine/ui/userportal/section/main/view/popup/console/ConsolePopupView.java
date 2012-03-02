package org.ovirt.engine.ui.userportal.section.main.view.popup.console;

import org.ovirt.engine.ui.common.CommonApplicationResources;
import org.ovirt.engine.ui.common.view.popup.AbstractModelBoundPopupView;
import org.ovirt.engine.ui.common.widget.Align;
import org.ovirt.engine.ui.common.widget.dialog.SimpleDialogPanel;
import org.ovirt.engine.ui.common.widget.editor.EntityModelRadioButtonEditor;
import org.ovirt.engine.ui.uicommonweb.models.userportal.IUserPortalListModel;
import org.ovirt.engine.ui.uicommonweb.models.userportal.UserPortalItemModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.ConsoleModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.ISpice;
import org.ovirt.engine.ui.uicommonweb.models.vms.RdpConsoleModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.SpiceConsoleModel;
import org.ovirt.engine.ui.userportal.ApplicationConstants;
import org.ovirt.engine.ui.userportal.ApplicationMessages;
import org.ovirt.engine.ui.userportal.section.main.presenter.popup.console.ConsolePopupPresenterWidget;
import org.ovirt.engine.ui.userportal.section.main.view.popup.console.widget.EntityModelValueCheckBoxEditor;
import org.ovirt.engine.ui.userportal.section.main.view.popup.console.widget.EntityModelValueCheckbox.ValueCheckboxRenderer;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.inject.Inject;

public class ConsolePopupView extends UserPortalModelBoundPopupView<IUserPortalListModel> implements ConsolePopupPresenterWidget.ViewDef {

    @UiField
    Label consoleTitle;

    @UiField(provided = true)
    EntityModelRadioButtonEditor spiceRadioButton;

    @UiField(provided = true)
    EntityModelRadioButtonEditor remoteDesktopRadioButton;

    @UiField(provided = true)
    EntityModelValueCheckBoxEditor<ConsoleModel> ctrlAltDel;

    @UiField(provided = true)
    EntityModelValueCheckBoxEditor<ConsoleModel> enableUsbAutoshare;

    @UiField(provided = true)
    EntityModelValueCheckBoxEditor<ConsoleModel> openInFullScreen;

    @UiField(provided = true)
    EntityModelValueCheckBoxEditor<ConsoleModel> useLocalDrives;

    @UiField
    FlowPanel spicePanel;

    @UiField
    FlowPanel rdpPanel;

    private IUserPortalListModel model;

    interface ViewUiBinder extends UiBinder<SimpleDialogPanel, ConsolePopupView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    @Inject
    public ConsolePopupView(EventBus eventBus, CommonApplicationResources resources,
            ApplicationConstants constants, ApplicationMessages messages) {
        super(eventBus, resources);

        spiceRadioButton = new EntityModelRadioButtonEditor("1");
        spiceRadioButton.setLabel(constants.spice());

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
                return spice.getFullScreen();
            }
        });
        openInFullScreen.setLabel(constants.openInFullScreen());

        useLocalDrives =
                new EntityModelValueCheckBoxEditor<ConsoleModel>(Align.RIGHT,
                        new ValueCheckboxRenderer<ConsoleModel>() {

                            @Override
                            public boolean render(ConsoleModel value) {
                                if (value instanceof RdpConsoleModel) {
                                    ((RdpConsoleModel) value).getrdp().getUseLocalDrives();
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
        useLocalDrives.setLabel(constants.rdpOptions());

        remoteDesktopRadioButton = new EntityModelRadioButtonEditor("1");
        remoteDesktopRadioButton.setLabel(constants.remoteDesctop());

        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));

        consoleTitle.setText(messages.selectConsoleFor("asd"));

        spicePanel.setVisible(false);
        rdpPanel.setVisible(false);

    }

    @Override
    public void edit(IUserPortalListModel model) {
        this.model = model;
        ctrlAltDel.asEditor().getSubEditor().setValue(((UserPortalItemModel)model.getSelectedItem()).getDefaultConsole());
        enableUsbAutoshare.asEditor().getSubEditor().setValue(((UserPortalItemModel)model.getSelectedItem()).getDefaultConsole());
        openInFullScreen.asEditor().getSubEditor().setValue(((UserPortalItemModel)model.getSelectedItem()).getDefaultConsole());
        useLocalDrives.asEditor().getSubEditor().setValue(((UserPortalItemModel)model.getSelectedItem()).getDefaultConsole());
    }

    @Override
    public IUserPortalListModel flush() {
        ctrlAltDel.asEditor().getSubEditor().getValue();
        enableUsbAutoshare.asEditor().getSubEditor().getValue();
        openInFullScreen.asEditor().getSubEditor().getValue();
        useLocalDrives.asEditor().getSubEditor().getValue();
        return model;
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
    public HasValueChangeHandlers<Boolean> getSpiceRadioButton() {
        return spiceRadioButton.asRadioButton();
    }

    @Override
    public HasValueChangeHandlers<Boolean> getRdpRadioButton() {
        return remoteDesktopRadioButton.asRadioButton();
    }

    @Override
    public void rdpSelected(boolean selected) {
        spicePanel.setVisible(selected);
    }

    @Override
    public void spiceSelected(boolean selected) {
        rdpPanel.setVisible(selected);
    }

    @Override
    public void selectSpice(boolean selected) {
        spiceRadioButton.asRadioButton().setValue(selected);
    }

    @Override
    public void selectRdp(boolean selected) {
        remoteDesktopRadioButton.asRadioButton().setValue(selected);

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

}
