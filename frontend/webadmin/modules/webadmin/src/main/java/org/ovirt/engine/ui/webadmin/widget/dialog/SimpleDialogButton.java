package org.ovirt.engine.ui.webadmin.widget.dialog;

import org.ovirt.engine.ui.common.widget.dialog.AbstractDialogButton;
import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.ApplicationTemplates;
import org.ovirt.engine.ui.webadmin.gin.ClientGinjectorProvider;

import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.AbstractImagePrototype;

public class SimpleDialogButton extends AbstractDialogButton {

    private static final ApplicationTemplates templates = ClientGinjectorProvider.instance().getApplicationTemplates();
    private static final ApplicationResources resources = ClientGinjectorProvider.instance().getApplicationResources();

    private static final String BUTTON_STYLE = "dialog-button-text";
    private static final String BUTTON_STYLE_DISABLED = "dialog-button-text-disabled";

    private static final String upStart = resources.dialogButtonUpStart().getURL();
    private static final String upStretch = resources.dialogButtonUpStretch().getURL();
    private static final String upEnd = resources.dialogButtonUpEnd().getURL();

    private static final String upOverStart = resources.dialogButtonOverStart().getURL();
    private static final String upOverStretch = resources.dialogButtonOverStretch().getURL();
    private static final String upOverEnd = resources.dialogButtonOverEnd().getURL();

    private static final String downStart = resources.dialogButtonDownStart().getURL();
    private static final String downStretch = resources.dialogButtonDownStretch().getURL();
    private static final String downEnd = resources.dialogButtonDownEnd().getURL();

    private static final String upDisabledStart = resources.dialogButtonUpDisabledStart().getURL();
    private static final String upDisabledStretch = resources.dialogButtonUpDisabledStretch().getURL();
    private static final String upDisabledEnd = resources.dialogButtonUpDisabledEnd().getURL();

    private SafeHtml image = SafeHtmlUtils.EMPTY_SAFE_HTML;
    private String customStyle = "";

    public SimpleDialogButton() {
        super("");
    }

    public void setImage(ImageResource image) {
        this.image = imagetoSafeHtml(image);
        updateFaces();
    }

    public void setCustomContentStyle(String customStyle) {
        this.customStyle = customStyle;
        updateFaces();
    }

    private SafeHtml imagetoSafeHtml(ImageResource resource) {
        if (resource == null) {
            return image;
        }
        return SafeHtmlUtils.fromTrustedString(AbstractImagePrototype.create(resource).getHTML());
    }

    @Override
    protected void updateFaces() {
        SafeHtml up = templates.dialogButton(image, text,
                upStart, upStretch, upEnd, BUTTON_STYLE, customStyle);
        SafeHtml upHovering = templates.dialogButton(image, text,
                upOverStart, upOverStretch, upOverEnd, BUTTON_STYLE, customStyle);
        SafeHtml upDisabled = templates.dialogButton(image, text,
                upDisabledStart, upDisabledStretch, upDisabledEnd, BUTTON_STYLE_DISABLED, customStyle);
        SafeHtml down = templates.dialogButton(image, text,
                downStart, downStretch, downEnd, BUTTON_STYLE, customStyle);

        getUpFace().setHTML(up);
        getUpHoveringFace().setHTML(upHovering);
        getUpDisabledFace().setHTML(upDisabled);
        getDownFace().setHTML(down);
    }

}
