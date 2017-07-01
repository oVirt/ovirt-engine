package org.ovirt.engine.core.common.queries;

import java.util.List;

import org.ovirt.engine.core.compat.Guid;

public class GetVmIconsParameters extends QueryParametersBase {

    private List<Guid> iconIds;

    private GetVmIconsParameters() {
    }

    public static GetVmIconsParameters create(List<Guid> iconIds) {
        if (iconIds == null) {
            throw new IllegalArgumentException("iconIds argument should not be null.");
        }
        final GetVmIconsParameters iconsParameters = new GetVmIconsParameters();
        iconsParameters.iconIds = iconIds;
        return iconsParameters;
    }

    public List<Guid> getIconIds() {
        return iconIds;
    }
}
