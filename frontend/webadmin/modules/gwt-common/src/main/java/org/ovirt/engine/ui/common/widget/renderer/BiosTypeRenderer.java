package org.ovirt.engine.ui.common.widget.renderer;

import org.ovirt.engine.core.common.businessentities.ArchitectureType;
import org.ovirt.engine.core.common.businessentities.BiosType;
import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.gin.AssetProvider;

/**
 * Renderer for BiosType Enum values.
 *
 * @param <E>
 *            Enum type.
 */
public class BiosTypeRenderer extends EnumRenderer<BiosType> {

    private static final CommonApplicationConstants constants = AssetProvider.getConstants();

    private ArchitectureType architectureType;

    private String defaultString;

    public BiosTypeRenderer() {
    }

    public BiosTypeRenderer(String defaultString) {
        this.defaultString = defaultString;
    }

    @Override
    public String render(BiosType biosType) {
        if (biosType == null && defaultString != null) {
            return defaultString;
        }

        if (biosType == null) {
            return "";
        }

        if (architectureType != null && BiosType.I440FX_SEA_BIOS.equals(biosType)) {
            if (ArchitectureType.ppc.equals(architectureType.getFamily())) {
                return constants.ppcChipset();
            } else if (ArchitectureType.s390x.equals(architectureType.getFamily())) {
                return constants.s390xChipset();
            }
        }
        return super.render(biosType);
    }

    public void setArchitectureType(ArchitectureType architectureType) {
        this.architectureType = architectureType;
    }

    public ArchitectureType getArchitectureType() {
        return architectureType;
    }
}
