package org.ovirt.engine.ui.common.widget.dialog;

import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.CustomButton;
import com.google.gwt.user.client.ui.HasText;

public abstract class AbstractDialogButton extends CustomButton implements HasText {

    protected String text;
    protected SafeHtml image = SafeHtmlUtils.EMPTY_SAFE_HTML;
    protected String customStyle = ""; //$NON-NLS-1$

    protected AbstractDialogButton(String text) {
        super();
        this.text = text;
    }

    @Override
    public void setText(String text) {
        this.text = text;
        updateFaces();
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

    protected abstract void updateFaces();

}
