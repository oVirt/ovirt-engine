package org.ovirt.engine.ui.webadmin.widget.renderer;

import org.ovirt.engine.core.compat.RpmVersion;
import org.ovirt.engine.ui.common.widget.renderer.EmptyValueRenderer;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;

import com.google.gwt.text.shared.AbstractRenderer;

public class VersionRenderer extends AbstractRenderer<RpmVersion> {

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    @Override
    public String render(RpmVersion version) {

        String formattedVersion = version.getRpmName();

        return new EmptyValueRenderer<String>(constants.unAvailablePropertyLabel()).render(formattedVersion);
    }
}
