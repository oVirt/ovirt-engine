package org.ovirt.engine.ui.webadmin.widget.label;

import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.ui.webadmin.widget.renderer.VersionRenderer;

import com.google.gwt.user.client.ui.ValueLabel;

public class VersionLabel extends ValueLabel<Version> {

    public VersionLabel() {
        super(new VersionRenderer());
    }

}
