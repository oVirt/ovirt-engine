package org.ovirt.engine.ui.webadmin.widget.renderer;

import org.ovirt.engine.core.compat.RpmVersion;
import org.ovirt.engine.ui.common.widget.renderer.EmptyValueRenderer;
import org.ovirt.engine.ui.webadmin.gin.ClientGinjectorProvider;

import com.google.gwt.text.shared.AbstractRenderer;

public class VersionRenderer extends AbstractRenderer<RpmVersion> {

    @Override
    public String render(RpmVersion version) {

        String formattedVersion;
        formattedVersion = version.getRpmName();

        return new EmptyValueRenderer<String>(ClientGinjectorProvider.getApplicationConstants()
                .unAvailablePropertyLabel()).render(formattedVersion);
    }
}
