package org.ovirt.engine.ui.common.system;

public class StorageKeyUtils {
    // Prefix for keys used to store widths of individual columns
    public static final String GRID_COLUMN_WIDTH_PREFIX = "GridColumnWidth"; //$NON-NLS-1$
    public static final String GRID_SWAPPED_COLUMN_LIST_SUFFIX = "GridSwappedColumns"; //$NON-NLS-1$
    public static final String GRID_HIDDEN = "grid-hidden"; // $NON-NLS-1$
    public static final String GRID_VISIBLE = "grid-visible"; // $NON-NLS-1$
    public static final String GRID_HIDDEN_COLUMN_WIDTH_PREFIX = GRID_HIDDEN +
            "_" + GRID_COLUMN_WIDTH_PREFIX + "_"; //$NON-NLS-1$ //$NON-NLS-2$
    // Key used to store refresh rate of all data grids
    public static final String GRID_REFRESH_RATE_KEY = "GridRefreshRate"; //$NON-NLS-1$
}
