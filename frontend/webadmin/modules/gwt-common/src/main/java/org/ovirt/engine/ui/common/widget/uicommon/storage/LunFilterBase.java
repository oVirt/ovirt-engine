package org.ovirt.engine.ui.common.widget.uicommon.storage;

/**
 * Base class of LUN model filter.
 */
public class LunFilterBase {

    private boolean isHideUsedLuns;

    public LunFilterBase(boolean isHideUsedLuns) {
        this.isHideUsedLuns = isHideUsedLuns;
    }

    public boolean getIsHideUsedLuns() {
        return isHideUsedLuns;
    }

    public void setIsHideUsedLuns(boolean isHideUsedLuns) {
        this.isHideUsedLuns = isHideUsedLuns;
    }

    /**
     * Is it necessary to apply a filter.
     *
     * @return true - if at least one of the filter fields has a value other than the default value
     */
    public boolean needFilter() {
        return isHideUsedLuns;
    }
}
