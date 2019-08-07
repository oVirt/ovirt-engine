package org.ovirt.engine.ui.uicommonweb.models.templates;

import java.util.Objects;

import org.ovirt.engine.core.common.businessentities.VmTemplate;

/**
 * Connection of 'base template' and 'template version'.
 *
 * <p>
 *     Ordering: <br/>
 *     Lexicographically - first by base template then by template version <br/>
 *     Ordering of base templates:
 *     <ul>
 *         <li>Blank</li>
 *         <li>Alphabetically by displayed name
 *             {@link org.ovirt.engine.core.common.businessentities.VmTemplate#getName()}</li>
 *     </ul>
 *     Ordering of template versions:
 *     <ul>
 *         <li>Latest</li>
 *         <li>by version number
 *             {@link org.ovirt.engine.core.common.businessentities.VmTemplate#getTemplateVersionNumber()}</li>
 *     </ul>
 * </p>
 *
 * <p>
 *     Equality based on {@link #baseTemplate}, {@link #templateVersion} and being latest template.
 * </p>
 */
public class TemplateWithVersion implements Comparable<TemplateWithVersion> {

    private VmTemplate baseTemplate;

    private VmTemplate templateVersion;

    /**
     * It has to hold {@code baseTemplate.getId().equals(templateVersion.getBaseTemplateId())}
     * @param baseTemplate base template, required non-null
     * @param templateVersion template version of {@code baseTemplate}, required non-null
     */
    public TemplateWithVersion(VmTemplate baseTemplate, VmTemplate templateVersion) {
        if (baseTemplate == null
                || templateVersion == null
                || !baseTemplate.getId().equals(templateVersion.getBaseTemplateId())) {
            throw new IllegalArgumentException("Arguments of TemplateWithVersion constructor has to be non-null, " + //$NON-NLS-1$
                    "templateVersion has to be version of baseTemplate."); //$NON-NLS-1$
        }
        this.baseTemplate = baseTemplate;
        this.templateVersion = templateVersion;
    }

    public VmTemplate getTemplateVersion() {
        return templateVersion;
    }

    public VmTemplate getBaseTemplate() {
        return baseTemplate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof TemplateWithVersion)) {
            return false;
        }

        TemplateWithVersion that = (TemplateWithVersion) o;

        if (!baseTemplate.equals(that.baseTemplate)) {
            return false;
        }
        if (!templateVersion.equals(that.templateVersion)) {
            return false;
        }

        boolean isThisLatest = templateVersion instanceof LatestVmTemplate;
        boolean isOtherLatest = that.getTemplateVersion() instanceof LatestVmTemplate;
        if (isThisLatest != isOtherLatest) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return Objects.hash(baseTemplate,
                templateVersion,
                templateVersion instanceof LatestVmTemplate);
    }

    @Override
    public int compareTo(TemplateWithVersion other) {
        final int baseTemplateComparison = compareBaseTemplate(
                this.getBaseTemplate(), other.getBaseTemplate());
        if (baseTemplateComparison != 0) {
            return baseTemplateComparison;
        }
        return compareTemplateVersion(this.getTemplateVersion(), other.getTemplateVersion());
    }

    /**
     * First <em>Latest</em>, then by version number - latter version first
     */
    private static int compareTemplateVersion(VmTemplate a, VmTemplate b) {
        if (a instanceof LatestVmTemplate) {
            if (b instanceof LatestVmTemplate) {
                return 0;
            } else {
                return -1;
            }
        } else {
            if (b instanceof LatestVmTemplate) {
                return 1;
            } else {
                return - Integer.signum(Integer.compare(a.getTemplateVersionNumber(), b.getTemplateVersionNumber()));
            }
        }
    }

    /**
     * First blank, then by name
     */
    private static int compareBaseTemplate(VmTemplate a, VmTemplate b) {
        if (a.isBlank()) {
            if (b.isBlank()) {
                return 0;
            } else {
                return -1;
            }
        } else {
            if (b.isBlank()) {
                return 1;
            } else {
                return a.getName().compareTo(b.getName());
            }
        }
    }

    public boolean isLatest() {
        return this.getTemplateVersion() instanceof LatestVmTemplate;
    }
}
