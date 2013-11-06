package org.ovirt.engine.ui.webadmin.widget.label;

import org.ovirt.engine.core.compat.RpmVersion;
import org.ovirt.engine.ui.common.widget.label.TextBoxLabelBase;
import org.ovirt.engine.ui.webadmin.widget.renderer.VersionRenderer;

public class VersionTextBoxLabel extends TextBoxLabelBase<RpmVersion> {

    public VersionTextBoxLabel() {
        super(new VersionRenderer());
    }

}
