package org.ovirt.engine.ui.uicommonweb.models.vms;

public enum WanColorDepth {
    depth16(16),
    depth32(32);
    private final int depth;

    private WanColorDepth(int depth) {
        this.depth = depth;
    }
    public static WanColorDepth fromInt(int integerDepth) {
        for (WanColorDepth value : values()) {
            if (value.depth == integerDepth) {
                return value;
            }
        }

        throw new IllegalArgumentException("Illegal int value: " + integerDepth); //$NON-NLS-1$
    }
    public int asInt() {
        return depth;
    }
}
