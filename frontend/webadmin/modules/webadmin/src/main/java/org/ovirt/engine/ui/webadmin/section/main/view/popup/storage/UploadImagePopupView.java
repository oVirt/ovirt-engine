package org.ovirt.engine.ui.webadmin.section.main.view.popup.storage;

import org.ovirt.engine.core.common.businessentities.storage.VolumeFormat;
import org.ovirt.engine.ui.common.idhandler.WithElementId;
import org.ovirt.engine.ui.common.view.popup.AbstractModelBoundPopupView;
import org.ovirt.engine.ui.common.widget.RadioButtonsHorizontalPanel;
import org.ovirt.engine.ui.common.widget.dialog.SimpleDialogPanel;
import org.ovirt.engine.ui.common.widget.editor.ListModelListBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.generic.StringEntityModelTextBoxEditor;
import org.ovirt.engine.ui.common.widget.renderer.EnumRenderer;
import org.ovirt.engine.ui.common.widget.uicommon.popup.vm.VmDiskPopupWidget;
import org.ovirt.engine.ui.uicommonweb.models.storage.UploadImageModel;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.IEventListener;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.storage.UploadImagePopupPresenterWidget;

import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.Editor;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FileUpload;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.inject.Inject;

public class UploadImagePopupView extends AbstractModelBoundPopupView<UploadImageModel> implements UploadImagePopupPresenterWidget.ViewDef {

    interface Driver extends SimpleBeanEditorDriver<UploadImageModel, UploadImagePopupView> {
    }

    interface ViewUiBinder extends UiBinder<SimpleDialogPanel, UploadImagePopupView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    interface WidgetStyle extends CssResource {
        String imageUriEditor();
        String imageUriEditorContent();
        String volumeFormatEditor();
        String volumeFormatEditorContent();
    }

    @UiField
    WidgetStyle style;

    @UiField
    @Ignore
    Label imageSourceLabel;

    @UiField
    RadioButtonsHorizontalPanel imageSourcePanel;

    @UiField
    @Ignore
    HorizontalPanel imageFileUploadPanel;

    @UiField
    FlowPanel uploadMessagePanel;

    @UiField
    FlowPanel progressMessagePanel;

    @UiField
    @Ignore
    Label imageFileUploadLabel;

    @UiField
    FileUpload imageFileUpload;

    @UiField
    @Ignore
    FlowPanel imageFileDownloadPanel;

    @UiField
    FlowPanel downloadMessagePanel;

    @UiField
    @Editor.Path(value = "imageUri.entity")
    StringEntityModelTextBoxEditor imageUriEditor;

    @UiField(provided = true)
    @Editor.Path(value = "volumeFormat.selectedItem")
    ListModelListBoxEditor<VolumeFormat> volumeFormatEditor;

    @UiField
    @Ignore
    Label diskOptionsLabel;

    @UiField(provided = true)
    @Ignore
    @WithElementId("vmdisk")
    VmDiskPopupWidget vmDiskPopupWidget;

    private final Driver driver = GWT.create(Driver.class);

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    @Inject
    public UploadImagePopupView(EventBus eventBus) {
        super(eventBus);
        initEditors();
        vmDiskPopupWidget = new VmDiskPopupWidget(false);
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        localize();
        addStyles();
        driver.initialize(this);
    }

    void initEditors() {
        volumeFormatEditor = new ListModelListBoxEditor<>(new EnumRenderer<VolumeFormat>());
    }

    void localize() {
        imageSourceLabel.setText(constants.uploadImageSourceLabel());
        imageFileUploadLabel.setText(constants.uploadImageFileLabel());
        imageUriEditor.setLabel(constants.uploadImageUriLabel());
        volumeFormatEditor.setLabel(constants.uploadImageTypeLabel());
        diskOptionsLabel.setText(constants.uploadImageDiskOptionsLabel());
    }

    void addStyles() {
        imageUriEditor.addContentWidgetContainerStyleName(style.imageUriEditorContent());
        imageUriEditor.addStyleName(style.imageUriEditor());
        volumeFormatEditor.addContentWidgetContainerStyleName(style.volumeFormatEditorContent());
        volumeFormatEditor.addStyleName(style.volumeFormatEditor());
    }

    @Override
    public void edit(final UploadImageModel model) {
        driver.edit(model);

        model.setImageFileUploadElement(imageFileUpload.getElement());

        imageFileUpload.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent changeEvent) {
                model.getImagePath().setEntity(imageFileUpload.getFilename());
            }
        });

        model.getPropertyChangedEvent().addListener(new IEventListener<PropertyChangedEventArgs>() {

            @Override
            public void eventRaised(Event<? extends PropertyChangedEventArgs> ev, Object sender, PropertyChangedEventArgs args) {
                if ("Message".equals(args.propertyName)) { //$NON-NLS-1$
                    setPanelMessage(uploadMessagePanel, model.getMessage());
                }
                else if ("Progress".equals(args.propertyName)) { //$NON-NLS-1$
                    setPanelMessage(progressMessagePanel, model.getProgressStr());
                }
                else if ("IsValid".equals(args.propertyName)) { //$NON-NLS-1$
                    if (!model.getIsValid()) {
                        setPanelMessage(uploadMessagePanel, model.getInvalidityReasons().get(0));
                    }
                }
            }
        });

        // This is called before adding the radio buttons because it updates the selected option
        handleImageUploadBrowserSupport(model);

        imageSourcePanel.addRadioButton(
                constants.uploadImageSourceLocal(),
                model.getImageSourceLocalEnabled().getEntity(),
                true,
                new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent event) {
                        model.getImageSourceLocalEnabled().setEntity(true);
                        setSourceVisibility(model);
                    }
                });
        imageSourcePanel.addRadioButton(
                constants.uploadImageSourceRemote(),
                !model.getImageSourceLocalEnabled().getEntity(),
                true,
                new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent event) {
                        model.getImageSourceLocalEnabled().setEntity(false);
                        setSourceVisibility(model);
                    }
                });

        setSourceVisibility(model);

        vmDiskPopupWidget.edit(model.getDiskModel());

        if (model.getIsResumeUpload()) {
            diskOptionsLabel.setText(constants.uploadImageDiskOptionsInfoOnlyLabel());
        }
    }

    private void handleImageUploadBrowserSupport(final UploadImageModel model) {
        model.setBrowserSupportsUpload(model.browserSupportsUploadAPIs());

        if (!model.getBrowserSupportsUpload()) {
            model.getOkCommand().setIsExecutionAllowed(false);
            imageFileUpload.setEnabled(false);
            volumeFormatEditor.setEnabled(false);
            setPanelMessage(uploadMessagePanel, constants.uploadImageUploadNotSupportedMessage());
            model.getImageSourceLocalEnabled().setEntity(false);
        }
    }

    private void setSourceVisibility(final UploadImageModel model) {
        imageFileUploadPanel.setVisible(model.getImageSourceLocalEnabled().getEntity());
        imageFileDownloadPanel.setVisible(!model.getImageSourceLocalEnabled().getEntity());
        volumeFormatEditor.setEnabled(!model.getImageSourceLocalEnabled().getEntity()
                || model.getBrowserSupportsUpload());
    }

    private void setPanelMessage(FlowPanel panel, String message) {
        panel.clear();
        panel.add(new Label(message));
        panel.setVisible(message != null && !message.isEmpty());
    }

    private void setDownloadMessage(String message) {
        downloadMessagePanel.clear();
        downloadMessagePanel.add(new Label(message));
        downloadMessagePanel.setVisible(message != null && !message.isEmpty());
    }

    @Override
    public UploadImageModel flush() {
        vmDiskPopupWidget.flush();
        return driver.flush();
    }
}
