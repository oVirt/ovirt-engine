package org.ovirt.engine.ui.common.widget.editor;

import java.util.List;
import java.util.Objects;

import org.gwtbootstrap3.client.ui.Alert;
import org.gwtbootstrap3.client.ui.Button;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.editor.UiCommonEditor;
import org.ovirt.engine.ui.common.gin.AssetProvider;
import org.ovirt.engine.ui.common.widget.AbstractValidatedWidget;
import org.ovirt.engine.ui.common.widget.dialog.InfoIcon;
import org.ovirt.engine.ui.uicommonweb.models.vms.IconWithOsDefault;
import org.ovirt.engine.ui.uicommonweb.validation.IconValidation;
import org.ovirt.engine.ui.uicommonweb.validation.ValidationResult;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.ImageElement;
import com.google.gwt.editor.client.LeafValueEditor;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.FileUpload;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.UIObject;
import com.google.gwt.user.client.ui.Widget;

/**
 * Icon editor. It allows to set custom icon to VM-like entities.
 */
public class IconEditorWidget extends AbstractValidatedWidget
        implements LeafValueEditor<IconWithOsDefault>,
                   HasValueChangeHandlers<IconWithOsDefault>,
                   UiCommonEditor<IconWithOsDefault> {

    interface ViewUiBinder extends UiBinder<HTMLPanel, IconEditorWidget> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    protected interface Style extends CssResource {
        String iconImageDisabled();
    }

    @UiField
    protected Style style;

    @UiField
    protected Image image;

    @UiField
    protected HTMLPanel hiddenPanel;

    @UiField
    protected Button uploadButton;

    @UiField(provided = true)
    protected InfoIcon uploadInfoIcon;

    @UiField
    protected Button defaultButton;

    @UiField
    protected Alert errorMessage;

    /**
     * current value, the visible image <br/>
     * in dataUri format
     */
    private String icon;

    /**
     * default value (given by OS of VM) <br/>
     * in dataUri format
     */
    private String defaultIcon;

    /**
     * Small icon id matching the icon downloaded from server or null.
     */
    private Guid smallIconId;

    /**
     * relates to {@link com.google.gwt.user.client.ui.HasEnabled} implementation
     */
    private boolean enabled;

    /**
     * relates to {@link org.ovirt.engine.ui.common.widget.HasAccess} implementation
     */
    private boolean accessible;

    /**
     * Result of validation of current icon.
     * <p>
     *     null means that current icon hasn't been validated yet.
     * </p>
     */
    private ValidationResult validationResult;

    private static final CommonApplicationConstants constants = AssetProvider.getConstants();

    public IconEditorWidget() {
        uploadInfoIcon = new InfoIcon(
                SafeHtmlUtils.fromTrustedString(constants.iconLimitationsIconVmPopup()));
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        setEnabled(true);
        setAccessible(true);
    }

    private void validateIcon() {
        final IconWithOsDefault oldValue = getValue();
        createValidationImageElement(icon, new ImageElementCallback() {
            @Override
            public void onElementReady(ImageElement imageElement) {
                validationResult = new IconValidation(imageElement).validate(icon);
                updateErrorIconLabel(validationResult);
                final IconWithOsDefault newValue = getValue();
                ValueChangeEvent.fireIfNotEqual(IconEditorWidget.this, oldValue, newValue);
            }
        });
    }

    /**
     * {@link Image} widget can't be used because it loads images lazily. {@link ImageElement} can't be used because it
     * doesn't have Java methods register 'load' and 'error' callbacks.
     */
    private native void createValidationImageElement(String imageUrl, ImageElementCallback imageElementCallback) /*-{
        var imageElement = document.createElement('img');
        var callback = $entry(function () {
            imageElementCallback.@org.ovirt.engine.ui.common.widget.editor.IconEditorWidget.ImageElementCallback::onJavaScriptImageObjectReady(Lcom/google/gwt/core/client/JavaScriptObject;)(imageElement);
        });
        imageElement.addEventListener('load', callback);
        imageElement.addEventListener('error', callback);
        imageElement.src = imageUrl;
    }-*/;

    @Override
    public void setValue(IconWithOsDefault value) {
        final IconWithOsDefault oldPair = getValue();
        if (Objects.equals(value, oldPair)) {
            return;
        }
        if (value == null) {
            defaultIcon = null;
            smallIconId = null;
            validationResult = null;
            setIcon(null);
        } else {
            defaultIcon = value.getOsDefaultIcon();
            smallIconId = value.getSmallIconId();
            validationResult = value.getValidationResult();
            setIcon(value.getIcon());
        }
        final IconWithOsDefault newPair = getValue();
        ValueChangeEvent.fireIfNotEqual(this, oldPair, newPair);
    }

    @Override
    public IconWithOsDefault getValue() {
        if (icon == null || defaultIcon == null) {
            return null;
        }
        return new IconWithOsDefault(icon, defaultIcon, smallIconId, validationResult);
    }

    @Override
    protected Widget getValidatedWidget() {
        return this;
    }

    @UiHandler("uploadButton")
    void onUploadIconButton(ClickEvent event) {
        hiddenPanel.clear();
        final FileUpload inputFileWidget = new FileUpload();
        inputFileWidget.getElement().setAttribute("accept", "image/gif,image/jpeg,image/png"); //$NON-NLS-1$ //$NON-NLS-2$
        inputFileWidget.addChangeHandler(e -> readUploadedIconFile(inputFileWidget.getElement()));
        inputFileWidget.getElement().setTabIndex(-1);
        hiddenPanel.add(inputFileWidget);
        inputFileWidget.click();
    }

    @UiHandler("defaultButton")
    void onDefaultIconButton(ClickEvent event) {
        setIconAndFireChangeEvent(defaultIcon, ValidationResult.ok());
    }

    protected void setIcon(final String icon) {
        this.icon = icon;
        image.setUrl(icon == null ? "" : icon); //$NON-NLS-1$
        if (validationResult == null) {
            validateIcon();
        } else {
            updateErrorIconLabel(validationResult);
        }
    }

    protected void setIconAndFireChangeEvent(String icon, ValidationResult validationResult) {
        final IconWithOsDefault oldPair = getValue();
        this.validationResult = validationResult;
        smallIconId = null;
        setIcon(icon);
        final IconWithOsDefault newPair = getValue();
        ValueChangeEvent.fireIfNotEqual(this, oldPair, newPair);
    }

    private void updateErrorIconLabel(ValidationResult validationResult) {
        if (!validationResult.getSuccess() && validationResult.getReasons().isEmpty()) {
            throw new IllegalArgumentException("Unsuccessful validation without any reason not allowed."); //$NON-NLS-1$
        }
        updateErrorIconLabel(validationResult.getReasons());
    }

    private void updateErrorIconLabel(List<String> reasons) {
        if (reasons.isEmpty()) {
            errorMessage.setText(SafeHtmlUtils.EMPTY_SAFE_HTML.asString());
            errorMessage.setVisible(false);
        } else {
            final SafeHtml htmlReasons = toList(reasons);
            errorMessage.setText(htmlReasons.asString());
            errorMessage.setVisible(true);
        }
    }

    private SafeHtml toList(List<String> stringItems) {
        SafeHtmlBuilder builder = new SafeHtmlBuilder();
        builder.appendEscaped(stringItems.get(0));
        return builder.toSafeHtml();
    }

    native void readUploadedIconFile(Element inputFileElement) /*-{
        var self = this;
        var javaCallback = $entry(function (dataUri) {
            return self.@org.ovirt.engine.ui.common.widget.editor.IconEditorWidget::setIconAndFireChangeEvent(Ljava/lang/String;Lorg/ovirt/engine/ui/uicommonweb/validation/ValidationResult;)(dataUri, null);
        });
        if (inputFileElement.files.length > 0) {
            var file = inputFileElement.files[0];
            var fileReader = new FileReader();
            fileReader.onload = onFileRead;
            fileReader.readAsDataURL(file);
        }

        function onFileRead(event) {
            var iconDataUri = event.target.result;
            javaCallback(iconDataUri);
        }
    }-*/;


    /*
     * see com.google.gwt.user.client.ui.ValueListBox.addValueChangeHandler()
     */
    @Override
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<IconWithOsDefault> handler) {
        return this.addHandler(handler, ValueChangeEvent.getType());
    }

    @Override
    public void markAsValid() {
        super.markAsValid();
        errorMessage.setVisible(false);
        getValidatedWidgetStyle().setBorderColor("transparent"); //$NON-NLS-1$
    }

    @Override
    public void markAsInvalid(List<String> validationHints) {
        super.markAsInvalid(validationHints);
        updateErrorIconLabel(validationHints);
        errorMessage.setVisible(true);
    }

    @Override
    public LeafValueEditor<IconWithOsDefault> getActualEditor() {
        return this;
    }

    @Override
    public int getTabIndex() {
        return uploadButton.getTabIndex();
    }

    @Override
    public void setAccessKey(char key) {
        uploadButton.setAccessKey(key);
    }

    @Override
    public void setFocus(boolean focused) {
        uploadButton.setFocus(focused);
    }

    @Override
    public void setTabIndex(int index) {
        uploadButton.setTabIndex(index);
        defaultButton.setTabIndex(index);
    }

    @Override
    public void disable(String disabilityHint) {
        setEnabled(false, disabilityHint);
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void setEnabled(boolean enabled) {
        setEnabled(enabled, ""); //$NON-NLS-1$
    }

    protected void setEnabled(boolean enabled, String hint) {
        this.enabled = enabled;
        uploadButton.setEnabled(enabled);
        uploadButton.setTitle(hint);
        defaultButton.setEnabled(enabled);
        defaultButton.setTitle(hint);
        ensureStyleNamePresent(image, !enabled, style.iconImageDisabled());
        image.setTitle(hint);
    }

    private static void ensureStyleNamePresent(UIObject object, boolean styleNameExists, String styleName) {
        if (styleNameExists) {
            object.addStyleName(styleName);
        } else {
            object.removeStyleName(styleName);
        }
    }

    @Override
    public boolean isAccessible() {
        return accessible;
    }

    @Override
    public void setAccessible(boolean accessible) {
        this.accessible = accessible;
        setVisible(accessible);
    }

    // None of these are called anywhere, not sure what they are doing.
    @Override
    public HandlerRegistration addKeyDownHandler(KeyDownHandler handler) {
        return null;
    }

    @Override
    public HandlerRegistration addKeyPressHandler(KeyPressHandler handler) {
        return null;
    }

    @Override
    public HandlerRegistration addKeyUpHandler(KeyUpHandler handler) {
        return null;
    }

    private abstract class ImageElementCallback {

        private void onJavaScriptImageObjectReady(JavaScriptObject jsImageObject) {
            if (!ImageElement.is(jsImageObject)) {
                throw new RuntimeException("Unexpected type of JavaScript object"); //$NON-NLS-1$
            }
            final ImageElement imageElement = (ImageElement) ImageElement.as(jsImageObject);
            onElementReady(imageElement);
        }

        public abstract void onElementReady(ImageElement imageElement);
    }
}
