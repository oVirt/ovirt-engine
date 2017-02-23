package org.ovirt.engine.ui.common.widget.dialog;

import org.ovirt.engine.ui.common.CommonApplicationResources;
import org.ovirt.engine.ui.common.gin.AssetProvider;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiConstructor;

public class InfoIcon extends TooltippedIcon {

    private static final CommonApplicationResources resources = AssetProvider.getResources();


    @UiConstructor
    public InfoIcon() {
        this(SafeHtmlUtils.EMPTY_SAFE_HTML);
    }

    public InfoIcon(SafeHtml text) {
        super(text, resources.dialogIconHelp(), resources.dialogIconHelpRollover());
    }
}
