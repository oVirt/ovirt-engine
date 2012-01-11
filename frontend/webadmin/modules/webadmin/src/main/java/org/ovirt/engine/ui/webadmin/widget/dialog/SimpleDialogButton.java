package org.ovirt.engine.ui.webadmin.widget.dialog;

import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.ApplicationTemplates;
import org.ovirt.engine.ui.webadmin.gin.ClientGinjectorProvider;

import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.CustomButton;

public class SimpleDialogButton extends CustomButton {
    ApplicationTemplates templates = ClientGinjectorProvider.instance().getApplicationTemplates();
    ApplicationResources resources = ClientGinjectorProvider.instance().getApplicationResources();

    private static final String BUTTON_STYLE = "dialog-button-text";
    private static final String BUTTON_STYLE_DISABLED = "dialog-button-text-disabled";

    String upStart = (resources.dialogButtonUpStart().getURL());
    String upStretch = resources.dialogButtonUpStretch().getURL();
    String upEnd = (resources.dialogButtonUpEnd().getURL());

    String upOverStart = (resources.dialogButtonOverStart().getURL());
    String upOverStretch = resources.dialogButtonOverStretch().getURL();
    String upOverEnd = (resources.dialogButtonOverEnd().getURL());

    String downStart = (resources.dialogButtonDownStart().getURL());
    String downStretch = resources.dialogButtonDownStretch().getURL();
    String downEnd = (resources.dialogButtonDownEnd().getURL());

    String upDisabledStart = (resources.dialogButtonUpDisabledStart().getURL());
    String upDisabledStretch = resources.dialogButtonUpDisabledStretch().getURL();
    String upDisabledEnd = (resources.dialogButtonUpDisabledEnd().getURL());

    private String text;
    private SafeHtml image;
    private String customStyle;

    public SimpleDialogButton() {
        this.text = "";
        this.image = SafeHtmlUtils.EMPTY_SAFE_HTML;
        this.customStyle = "";
    }

    @Override
    public void setText(String text) {
        this.text = text;
        UpdateFaces();
    }

    public void setImage(ImageResource image) {
        this.image = ImagetoSafeHtml(image);
        UpdateFaces();
    }

    public void setCustomContentStyle(String customStyle) {
        this.customStyle = customStyle;
        UpdateFaces();
    }

    private void UpdateFaces() {
        SafeHtml up = templates.dialogButton(
                image, text, upStart, upStretch, upEnd, BUTTON_STYLE, customStyle);
        SafeHtml upHovering = templates.dialogButton(
                image, text, upOverStart, upOverStretch, upOverEnd, BUTTON_STYLE, customStyle);
        SafeHtml upDisabled = templates.dialogButton(
                image, text, upDisabledStart, upDisabledStretch, upDisabledEnd, BUTTON_STYLE_DISABLED, customStyle);
        SafeHtml down = templates.dialogButton(
                image, text, downStart, downStretch, downEnd, BUTTON_STYLE, customStyle);

        getUpFace().setHTML(up);
        getUpHoveringFace().setHTML(upHovering);
        getUpDisabledFace().setHTML(upDisabled);
        getDownFace().setHTML(down);
    }

    private SafeHtml ImagetoSafeHtml(ImageResource resource) {
        if (resource == null) {
            return image;
        }
        return SafeHtmlUtils.fromTrustedString(AbstractImagePrototype.create(resource).getHTML());
    }

    @Override
    protected void onClick() {
        setDown(false);
        super.onClick();
    }

    @Override
    protected void onClickCancel() {
        setDown(false);
    }

    @Override
    protected void onClickStart() {
        setDown(true);
    }

}
