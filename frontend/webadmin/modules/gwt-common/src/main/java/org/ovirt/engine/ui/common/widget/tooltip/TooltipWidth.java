package org.ovirt.engine.ui.common.widget.tooltip;

public enum TooltipWidth {

    W220("tooltip-w220"), //$NON-NLS-1$
    W320("tooltip-w320"), //$NON-NLS-1$
    W420("tooltip-w420"), //$NON-NLS-1$
    W520("tooltip-w520"), //$NON-NLS-1$
    W620("tooltip-w620"); //$NON-NLS-1$

    private final String className; // in px

    TooltipWidth(String className) {
        this.className = className;
    }

    public String getClassName() {
        return className;
    }

}
