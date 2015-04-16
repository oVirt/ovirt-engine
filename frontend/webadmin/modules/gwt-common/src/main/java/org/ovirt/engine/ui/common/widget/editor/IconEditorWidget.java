package org.ovirt.engine.ui.common.widget.editor;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.editor.client.LeafValueEditor;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.dom.client.KeyPressEvent;
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
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FileUpload;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.UIObject;
import com.google.gwt.user.client.ui.Widget;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.CommonApplicationResources;
import org.ovirt.engine.ui.common.CommonApplicationTemplates;
import org.ovirt.engine.ui.common.editor.UiCommonEditor;
import org.ovirt.engine.ui.common.widget.AbstractValidatedWidget;
import org.ovirt.engine.ui.common.widget.dialog.InfoIcon;
import org.ovirt.engine.ui.uicommonweb.models.vms.IconWithOsDefault;
import org.ovirt.engine.ui.uicommonweb.validation.IconValidation;
import org.ovirt.engine.ui.uicommonweb.validation.ValidationResult;

import javax.inject.Inject;
import java.util.List;

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
        String grey();
        String iconImageDisabled();
    }

    @UiField
    protected Style style;

    @UiField
    protected Image image;

    @UiField
    protected Button uploadButton;

    @UiField(provided = true)
    protected InfoIcon uploadInfoIcon;

    @UiField
    protected FileUpload fileUpload;

    @UiField
    protected Button defaultButton;

    @UiField
    protected HTML errorMessageHtml;

    protected final CommonApplicationTemplates templates;

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

    @Inject
    public IconEditorWidget(CommonApplicationConstants constants,
                            CommonApplicationTemplates templates,
                            CommonApplicationResources resources) {
        this.templates = templates;
        uploadInfoIcon = new InfoIcon(
                SafeHtmlUtils.fromTrustedString(constants.iconLimitationsIconVmPopup()));
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        fileUpload.getElement().setAttribute("accept", "image/gif,image/jpeg,image/png"); //$NON-NLS-1$ //$NON-NLS-2$
        fileUpload.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) {
                IconEditorWidget.this.readUploadedIconFile();
            }
        });
        fileUpload.getElement().setTabIndex(-1); // can be moved to *.ui.xml file in form of attribute `tabIndex="-1"` since GWT 2.7.0

        KeyPressHandler preventEnterKeyPressHandler = createPreventEnterKeyPressHandler();
        uploadButton.addKeyPressHandler(preventEnterKeyPressHandler);
        defaultButton.addKeyPressHandler(preventEnterKeyPressHandler);

        setEnabled(true);
        setAccessible(true);
    }

    private KeyPressHandler createPreventEnterKeyPressHandler() {
        return new KeyPressHandler() {
            @Override
            public void onKeyPress(KeyPressEvent event) {
                if (!event.isAnyModifierKeyDown()
                        && event.getNativeEvent().getKeyCode() == KeyCodes.KEY_ENTER) {
                    event.preventDefault();
                    event.stopPropagation();
                }
            }
        };
    }

    @Override
    public void setValue(IconWithOsDefault value) {
        final IconWithOsDefault oldPair = getValue();
        if (value == null) {
            defaultIcon = null;
            smallIconId = null;
            setIcon(null);
        } else {
            defaultIcon = value.getOsDefaultIcon();
            smallIconId = value.getSmallIconId();
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
        return new IconWithOsDefault(icon, defaultIcon, smallIconId);
    }

    @Override
    protected Widget getValidatedWidget() {
        return this;
    }

    @UiHandler("uploadButton")
    void onUploadIconButton(ClickEvent event) {
        clickElement(fileUpload.getElement());
    }

    @UiHandler("defaultButton")
    void onDefaultIconButton(ClickEvent event) {
        setIconAndFireChangeEvent(defaultIcon);
    }

    /**
     * There is FileUpload#click() method since GWT 2.7.0.
     */
    native void clickElement(Element element) /*-{
        element.click();
    }-*/;

    protected void setIcon(final String icon) {
        this.icon = icon;
        if (icon != null) {
            initializeBrowserInternalImageCache(icon, new Callback() {
                @Override
                public void onLoadOrError() {
                    final ValidationResult validation = (new IconValidation()).validate(icon);
                    updateErrorIconLabel(validation);
                    image.getElement().setAttribute("src", icon); //$NON-NLS-1$
                }
            });
        } else {
            updateErrorIconLabel(ValidationResult.ok());
            image.getElement().setAttribute("src", ""); //$NON-NLS-1$ //$NON-NLS-2$
        }
    }

    /**
     * Assigning to img.src attribute is generally an asynchronous operation. Current browsers behavior:
     * <pre>
     *     schema  | http(s): | data: |
     *     browser
     *     ========|==========|=======|
     *     Firefox | async    | async |
     *     Chrome  | sync     | sync  |
     *     IE      | async    | async |
     * </pre>
     * Once the image is loaded, assigning to img.src attribute behaves synchronously (even though it is not guarantied
     * by spec). This method does the first dummy load of param {@code icon} in order to make subsequent calls
     * synchronous.
     * <p>
     *     This is a hack. Proper solution is to make validators work asynchronously.
     * </p>
     * Works since IE9, pure gwt implementation would require to attach image to DOM + CSS hiding.
     */
    private native void initializeBrowserInternalImageCache(String icon, Callback callback) /*-{
        var image = new Image();
        image.addEventListener('load', runCallback);
        image.addEventListener('error', runCallback);
        image.src = icon;

        function runCallback() {
            callback.@org.ovirt.engine.ui.common.widget.editor.IconEditorWidget.Callback::onLoadOrError()();
        }
    }-*/;

    protected void setIconAndFireChangeEvent(String icon) {
        final IconWithOsDefault oldPair = getValue();
        setIcon(icon);
        smallIconId = null;
        final IconWithOsDefault newPair = getValue();
        ValueChangeEvent.fireIfNotEqual(this, oldPair, newPair);
    }

    private void updateErrorIconLabel(ValidationResult validation) {
        if (!validation.getSuccess() && validation.getReasons().isEmpty()) {
            throw new IllegalArgumentException("Unsuccessful validation without any reason not allowed."); //$NON-NLS-1$
        }
        updateErrorIconLabel(validation.getReasons());
    }

    private void updateErrorIconLabel(List<String> reasons) {
        if (reasons.isEmpty()) {
            errorMessageHtml.setHTML(SafeHtmlUtils.EMPTY_SAFE_HTML);
        } else {
            final SafeHtml htmlReasons = toUnorderedList(reasons);
            errorMessageHtml.setHTML(htmlReasons);
        }
    }

    private SafeHtml toUnorderedList(List<String> stringItems) {
        SafeHtmlBuilder builder = new SafeHtmlBuilder();
        for (String stringItem : stringItems) {
            builder.append(templates.listItem(stringItem));
        }
        return templates.unorderedList(builder.toSafeHtml());
    }

    native void readUploadedIconFile() /*-{
        var inputFileElement = this.@org.ovirt.engine.ui.common.widget.editor.IconEditorWidget::fileUpload.@com.google.gwt.user.client.ui.FileUpload::getElement()();
        var self = this;
        var javaCallback = $entry(function (dataUri) {
            return self.@org.ovirt.engine.ui.common.widget.editor.IconEditorWidget::setIconAndFireChangeEvent(Ljava/lang/String;)(dataUri);
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
        getValidatedWidgetStyle().setBorderColor("transparent"); //$NON-NLS-1$
    }

    @Override
    public void markAsInvalid(List<String> validationHints) {
        super.markAsInvalid(validationHints);
        updateErrorIconLabel(validationHints);
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
        ensureStyleNamePresent(errorMessageHtml, !enabled, style.grey());
        errorMessageHtml.setTitle(hint);
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

    @Override
    public HandlerRegistration addKeyDownHandler(KeyDownHandler handler) {
        return CompositeHandlerRegistration.of(
                uploadButton.addKeyDownHandler(handler),
                defaultButton.addKeyDownHandler(handler));
    }

    @Override
    public HandlerRegistration addKeyPressHandler(KeyPressHandler handler) {
        return CompositeHandlerRegistration.of(
                uploadButton.addKeyPressHandler(handler),
                defaultButton.addKeyPressHandler(handler));
    }

    @Override
    public HandlerRegistration addKeyUpHandler(KeyUpHandler handler) {
        return CompositeHandlerRegistration.of(
                uploadButton.addKeyUpHandler(handler),
                defaultButton.addKeyUpHandler(handler));
    }

    private static interface Callback {
        public void onLoadOrError();
    }
}
