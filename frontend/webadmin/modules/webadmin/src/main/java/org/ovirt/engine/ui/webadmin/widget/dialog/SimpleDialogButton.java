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

    public SimpleDialogButton() {
    }

    @Override
    public void setText(String text) {
        this.text = text;
        UpdateFaces();
    }

    private void UpdateFaces() {
        getUpFace().setHTML(templates.dialogButton(text, upStart, upStretch, upEnd, BUTTON_STYLE));
        getUpHoveringFace().setHTML(templates.dialogButton(text, upOverStart, upOverStretch, upOverEnd, BUTTON_STYLE));
        getDownFace().setHTML(templates.dialogButton(text, downStart, downStretch, downEnd, BUTTON_STYLE));
        getUpDisabledFace().setHTML(templates.dialogButton(text,
                upDisabledStart,
                upDisabledStretch,
                upDisabledEnd,
                BUTTON_STYLE_DISABLED));
    }

    private SafeHtml ImagetoSafeHtml(ImageResource resource) {
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
