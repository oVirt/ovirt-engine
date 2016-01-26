package org.ovirt.engine.ui.common.widget.dialog;

import org.ovirt.engine.ui.common.CommonApplicationResources;
import org.ovirt.engine.ui.common.CommonApplicationTemplates;
import org.ovirt.engine.ui.common.gin.AssetProvider;
import com.google.gwt.safehtml.shared.SafeHtml;

public class SimpleDialogButton extends AbstractDialogButton {

    private static final String BUTTON_STYLE = "dialog-button-text"; //$NON-NLS-1$
    private static final String BUTTON_STYLE_DISABLED = "dialog-button-text-disabled"; //$NON-NLS-1$

    private static final CommonApplicationTemplates templates = AssetProvider.getTemplates();
    private static final CommonApplicationResources resources = AssetProvider.getResources();

    public SimpleDialogButton() {
        super(""); //$NON-NLS-1$
    }

    @Override
    protected void updateFaces() {
        SafeHtml up = templates.dialogButton(image, text,
                resources.dialogButtonUpStart().getURL(),
                resources.dialogButtonUpStretch().getURL(),
                resources.dialogButtonUpEnd().getURL(),
                BUTTON_STYLE, customStyle);
        SafeHtml upHovering = templates.dialogButton(image, text,
                resources.dialogButtonOverStart().getURL(),
                resources.dialogButtonOverStretch().getURL(),
                resources.dialogButtonOverEnd().getURL(),
                BUTTON_STYLE, customStyle);
        SafeHtml upDisabled = templates.dialogButton(image, text,
                resources.dialogButtonUpDisabledStart().getURL(),
                resources.dialogButtonUpDisabledStretch().getURL(),
                resources.dialogButtonUpDisabledEnd().getURL(),
                BUTTON_STYLE_DISABLED, customStyle);
        SafeHtml down = templates.dialogButton(image, text,
                resources.dialogButtonDownStart().getURL(),
                resources.dialogButtonDownStretch().getURL(),
                resources.dialogButtonDownEnd().getURL(),
                BUTTON_STYLE, customStyle);

        getUpFace().setHTML(up);
        getUpHoveringFace().setHTML(upHovering);
        getUpDisabledFace().setHTML(upDisabled);
        getDownFace().setHTML(down);
    }

}
