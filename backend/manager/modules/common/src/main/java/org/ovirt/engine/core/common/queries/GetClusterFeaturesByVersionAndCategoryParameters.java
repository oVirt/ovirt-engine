package org.ovirt.engine.core.common.queries;

import org.ovirt.engine.core.common.mode.ApplicationMode;
import org.ovirt.engine.core.common.utils.ToStringBuilder;
import org.ovirt.engine.core.compat.Version;

public class GetClusterFeaturesByVersionAndCategoryParameters extends QueryParametersBase {
    private static final long serialVersionUID = 4623643366378282119L;
    private Version version;
    private ApplicationMode category;

    public GetClusterFeaturesByVersionAndCategoryParameters() {

    }

    public GetClusterFeaturesByVersionAndCategoryParameters(Version version, ApplicationMode category) {
        this.version = version;
        this.category = category;
        setRefresh(false);
    }
    public Version getVersion() {
        return version;
    }

    public void setVersion(Version version) {
        this.version = version;
    }

    public ApplicationMode getCategory() {
        return category;
    }

    public void setCategory(ApplicationMode category) {
        this.category = category;
    }

    @Override
    protected ToStringBuilder appendAttributes(ToStringBuilder tsb) {
        return super.appendAttributes(tsb)
                .append("version", getVersion())
                .append("category", category);

    }
}
