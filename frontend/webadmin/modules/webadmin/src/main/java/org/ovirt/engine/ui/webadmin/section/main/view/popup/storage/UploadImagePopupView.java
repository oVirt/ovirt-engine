package org.ovirt.engine.ui.webadmin.section.main.view.popup.storage;

import org.ovirt.engine.ui.common.editor.UiCommonEditorDriver;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.idhandler.WithElementId;
import org.ovirt.engine.ui.common.view.popup.AbstractModelBoundPopupView;
import org.ovirt.engine.ui.common.widget.HasUiCommandClickHandlers;
import org.ovirt.engine.ui.common.widget.RadioButtonPanel;
import org.ovirt.engine.ui.common.widget.UiCommandButton;
import org.ovirt.engine.ui.common.widget.dialog.SimpleDialogButton;
import org.ovirt.engine.ui.common.widget.dialog.SimpleDialogPanel;
import org.ovirt.engine.ui.common.widget.panel.AlertPanel;
import org.ovirt.engine.ui.common.widget.uicommon.popup.vm.VmDiskPopupWidget;
import org.ovirt.engine.ui.common.widget.uicommon.storage.ImageInfoForm;
import org.ovirt.engine.ui.uicommonweb.models.storage.UploadImageModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.ApplicationMessages;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.storage.UploadImagePopupPresenterWidget;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.InputElement;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.FileUpload;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.inject.Inject;

public class UploadImagePopupView extends AbstractModelBoundPopupView<UploadImageModel> implements UploadImagePopupPresenterWidget.ViewDef {

    interface Driver extends UiCommonEditorDriver<UploadImageModel, UploadImagePopupView> {
    }

    interface ViewUiBinder extends UiBinder<SimpleDialogPanel, UploadImagePopupView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    interface ViewIdHandler extends ElementIdHandler<UploadImagePopupView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    @UiField
    @Ignore
    Label imageSourceLabel;

    @UiField
    RadioButtonPanel imageSourcePanel;

    @UiField
    @Ignore
    HorizontalPanel imageFileUploadPanel;

    @UiField
    AlertPanel messagePanel;

    @UiField
    @WithElementId("fileUpload")
    FileUpload imageFileUpload;

    @UiField
    @WithElementId("fileUploadButton")
    SimpleDialogButton imageFileUploadButton;

    @UiField
    @Ignore
    @WithElementId("fileUploadLabel")
    Label imageFileUploadLabel;

    @UiField
    @Ignore
    Label diskOptionsLabel;

    @UiField(provided = true)
    @Ignore
    VmDiskPopupWidget vmDiskPopupWidget;

    @UiField
    @Ignore
    ImageInfoForm imageInfoForm;

    @UiField
    UiCommandButton testButton;

    private final Driver driver = GWT.create(Driver.class);

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    private static final ApplicationMessages messages = AssetProvider.getMessages();

    @Inject
    public UploadImagePopupView(EventBus eventBus) {
        super(eventBus);
        vmDiskPopupWidget = new VmDiskPopupWidget(false);
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        localize();
        UploadImagePopupView.ViewIdHandler.idHandler.generateAndSetIds(this);
        driver.initialize(this);
    }

    void localize() {
        imageSourceLabel.setText(constants.uploadImageSourceLabel());
        diskOptionsLabel.setText(constants.uploadImageDiskOptionsLabel());
    }

    @Override
    public void edit(final UploadImageModel model) {
        driver.edit(model);

        model.setImageFileUploadElement(imageFileUpload.getElement());

        imageFileUpload.addChangeHandler(changeEvent -> model.getImagePath().setEntity(imageFileUpload.getFilename()));

        model.getPropertyChangedEvent().addListener((ev, sender, args) -> {
            if ("Message".equals(args.propertyName)) { //$NON-NLS-1$
                setPanelMessage(messagePanel, model.getMessage(), AlertPanel.Type.WARNING);
            } else if ("IsValid".equals(args.propertyName)) { //$NON-NLS-1$
                hidePanelMessage();
                if (!model.getIsValid() && !model.getInvalidityReasons().isEmpty()) {
                    setPanelMessage(messagePanel, model.getInvalidityReasons().get(0), AlertPanel.Type.WARNING);
                }
            }
        });

        // This is called before adding the radio buttons because it updates the selected option
        handleImageUploadBrowserSupport(model);

        imageSourcePanel.addRadioButton(
                constants.uploadImageSourceLocal(),
                model.getImageSourceLocalEnabled().getEntity(),
                true,
                event -> {
                    model.getImageSourceLocalEnabled().setEntity(true);
                    setSourceVisibility(model);
                });
        imageSourcePanel.addRadioButton(
                constants.uploadImageSourceRemote(),
                !model.getImageSourceLocalEnabled().getEntity(),
                true,
                event -> {
                    model.getImageSourceLocalEnabled().setEntity(false);
                    setSourceVisibility(model);
                });

        setSourceVisibility(model);

        vmDiskPopupWidget.edit(model.getDiskModel());

        if (model.getIsResumeUpload()) {
            diskOptionsLabel.setText(constants.uploadImageDiskOptionsInfoOnlyLabel());
        }

        imageInfoForm.initialize(model.getImageInfoModel());
        model.getImageInfoModel().getEntityChangedEvent().addListener((ev, sender, args) -> model.setIsValid(model.getImageInfoModel().getIsValid()));
        model.getImageInfoModel().initialize(model.getImageFileUploadElement());

        // Add image upload click handler and label updater
        imageFileUploadButton.addClickHandler(event -> imageFileUpload.getElement().<InputElement>cast().click());
        imageFileUpload.addChangeHandler(event -> imageFileUploadLabel.setText(
                imageFileUpload.getFilename().replaceFirst(
                        "^C:\\\\fakepath\\\\", ""))); //$NON-NLS-1$ //$NON-NLS-2$
    }

    private void handleImageUploadBrowserSupport(final UploadImageModel model) {
        model.setBrowserSupportsUpload(UploadImageModel.browserSupportsUploadAPIs());

        if (!model.getBrowserSupportsUpload()) {
            model.getOkCommand().setIsExecutionAllowed(false);
            imageFileUpload.setEnabled(false);
            setPanelMessage(messagePanel, constants.uploadImageUploadNotSupportedMessage(), AlertPanel.Type.DANGER);
            model.getImageSourceLocalEnabled().setEntity(false);
        }
    }

    private void setSourceVisibility(final UploadImageModel model) {
        imageFileUploadPanel.setVisible(model.getImageSourceLocalEnabled().getEntity());
    }

    private void setPanelMessage(AlertPanel panel, String message, AlertPanel.Type type) {
        panel.clearMessages();
        panel.setVisible(false);
        if (message != null && !message.isEmpty()) {
            panel.setType(type);
            panel.addMessage(SafeHtmlUtils.fromString(message));
            panel.setVisible(true);
        }
    }

    private void hidePanelMessage() {
        messagePanel.clearMessages();
        messagePanel.setVisible(false);
    }

    @Override
    public UploadImageModel flush() {
        vmDiskPopupWidget.flush();
        return driver.flush();
    }

    @Override
    public void cleanup() {
        driver.cleanup();
    }

    @Override
    public int setTabIndexes(int nextTabIndex) {
        nextTabIndex = vmDiskPopupWidget.setTabIndexes(nextTabIndex);
        return nextTabIndex;
    }

    @Override
    public HasUiCommandClickHandlers getTestButton() {
        return testButton;
    }

    @Override
    public void updateTestResult(boolean succeeded) {
        messagePanel.clearMessages();
        messagePanel.setVisible(true);
        if (succeeded) {
            messagePanel.setType(AlertPanel.Type.SUCCESS);
            messagePanel.addMessage(SafeHtmlUtils.fromSafeConstant(
                    constants.testImageIOProxyConnectionSuccess()));
        } else {
            messagePanel.setType(AlertPanel.Type.WARNING);
            messagePanel.addMessage(SafeHtmlUtils.fromSafeConstant(
                    messages.testImageIOProxyConnectionFailure(getProxyLocation())));
        }
    }

    @Override
    public void showTestCommand(boolean show) {
        testButton.setVisible(show);
    }

    @Override
    public String getProxyLocation() {
        return Window.Location.getProtocol() + "//" + Window.Location.getHost(); //$NON-NLS-1$
    }
}
