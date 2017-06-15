package org.ovirt.engine.ui.webadmin.widget.label;

import java.util.Date;

import org.ovirt.engine.ui.common.widget.renderer.FullDateTimeRenderer;

import com.google.gwt.user.client.ui.ValueLabel;

public class FullDateTimeLabel extends ValueLabel<Date> {

    public FullDateTimeLabel(boolean includeTime) {
        super(new FullDateTimeRenderer(includeTime));
    }

    public FullDateTimeLabel() {
        super(new FullDateTimeRenderer());
    }
}
