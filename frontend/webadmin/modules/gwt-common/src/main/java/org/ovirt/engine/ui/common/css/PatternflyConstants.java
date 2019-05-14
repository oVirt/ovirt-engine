package org.ovirt.engine.ui.common.css;

/**
 * Constants related to the use of PatternFly. Put any special PatternFly classes in here.
 * <p>
 * Note that a lot of them are already defined in the gwtbootstrap3 project (which is included in oVirt),
 * so only put things in here that aren't already defined. For the current list of already defined constants,
 * see https://github.com/gwtbootstrap3/gwtbootstrap3/tree/master/gwtbootstrap3/src/main/java/org/gwtbootstrap3/client/ui/constants.
 * <p>
 * For example, all pficon classes go in here.
 * <p>
 * To use in UIBinder:
 * <pre>
 *     <ui:import field="org.ovirt.engine.ui.common.css.PatternflyConstants.*" />
 *     ...
 *     <h:Span ui:field="icon" addStyleNames="{PFICON}" />
 * </pre>
 */
public class PatternflyConstants {

    public static final int ZINDEX_NAVBAR = 1000;
    public static final int ZINDEX_DROPDOWN = 1000;
    public static final int ZINDEX_POPOVER = 1060;
    public static final int ZINDEX_TOOLTIP = 1070;
    public static final int ZINDEX_NAVBAR_FIXED = 1030;
    public static final int ZINDEX_MODAL_BACKGROUND = 1040;
    public static final int ZINDEX_MODAL = 1050;

    // pficons

    // TODO insert all classes from PatternFly's less/icons.less

    public static final String PFICON = "pficon"; // $NON-NLS-1$
    public static final String PFICON_ERROR_CIRCLE_O = "pficon-error-circle-o"; // $NON-NLS-1$
    public static final String PFICON_WARNING_TRIANGLE_O = "pficon-warning-triangle-o"; // $NON-NLS-1$
    public static final String PFICON_OK = "pficon-ok"; // $NON-NLS-1$
    public static final String PFICON_INFO = "pficon-info"; // $NON-NLS-1$
    public static final String PFICON_NETWORK = "pficon-network"; // $NON-NLS-1$
    public static final String PFICON_CLOSE = "pficon-close"; // $NON-NLS-1$
    public static final String PFICON_ERROR = "pficon-error-circle-o"; // $NON-NLS-1$
    public static final String PFICON_MEMORY = "pficon-memory"; // $NON-NLS-1$

    public static final String PF_SPINNER = "spinner"; // $NON-NLS-1$
    public static final String PF_SPINNER_XS = "spinner-xs"; // $NON-NLS-1$
    public static final String PF_SPINNER_INLINE = "spinner-inline"; // $NON-NLS-1$

    public static final String PF_DRAWER_NOTIFICATION_MESSAGE = "drawer-pf-notification-message"; // $NON-NLS-1$
    public static final String PF_DRAWER_NOTIFICATION = "drawer-pf-notification"; // $NON-NLS-1$
    public static final String PF_DRAWER_NOTIFICATION_INFO = "drawer-pf-notification-info"; // $NON-NLS-1$
    public static final String PF_DRAWER_TRIGGER = "drawer-pf-trigger"; // $NON-NLS-1$
    public static final String PF_DRAWER_TRIGGER_ICON = "drawer-pf-trigger-icon"; // $NON-NLS-1$
    public static final String PF_DRAWER_ACTION = "drawer-pf-action"; // $NON-NLS-1$
    public static final String PF_DRAWER_TITLE = "drawer-pf-title"; // $NON-NLS-1$
    public static final String PF_DRAWER_TOGGLE_EXPAND = "drawer-pf-toggle-expand"; // $NON-NLS-1$
    public static final String PF_DRAWER_CLOSE = "drawer-pf-close"; // $NON-NLS-1$

    public static final String PF_DATE = "date"; // $NON-NLS-1$
    public static final String PF_TIME = "time"; // $NON-NLS-1$
    public static final String PF_CARET = "caret"; // $NON-NLS-1$

    public static final String NAV_ITEM_ICONIC = "nav-item-iconic"; // $NON-NLS-1$

    public static final String PF_KEBAB_DROPDOWN = "dropdown-kebab-pf"; // $NON-NLS-1$

    public static final String PF_TOOLBAR = "toolbar-pf"; // $NON-NLS-1$
    public static final String PF_TOOLBAR_ACTIONS = "toolbar-pf-actions"; // $NON-NLS-1$
    public static final String PF_TOOLBAR_FILTER = "toolbar-pf-filter"; // $NON-NLS-1$
    public static final String PF_TOOLBAR_RESULTS = "toolbar-pf-results"; // $NON-NLS-1$

    public static final String PF_LIST_VIEW_MAIN_INFO = "list-view-pf-main-info"; // $NON-NLS-1$
    public static final String PF_LIST_VIEW_BODY = "list-view-pf-body"; // $NON-NLS-1$
    public static final String PF_LIST_VIEW_DESCRIPTION = "list-view-pf-description"; // $NON-NLS-1$
    public static final String PF_LIST_VIEW_LEFT = "list-view-pf-left"; // $NON-NLS-1$
    public static final String PF_LIST_VIEW_ICON_SM = "list-view-pf-icon-sm"; // $NON-NLS-1$
    public static final String PF_LIST_VIEW_ICON_MD = "list-view-pf-icon-md"; // $NON-NLS-1$
    public static final String PF_LIST_VIEW_ADDITIONAL_INFO = "list-view-pf-additional-info"; // $NON-NLS-1$
    public static final String PF_LIST_VIEW_ADDITIONAL_INFO_ITEM = "list-view-pf-additional-info-item"; // $NON-NLS-1$
    public static final String PF_LIST_VIEW_EXPAND = "list-view-pf-expand"; // $NON-NLS-1$
    public static final String PF_LIST_VIEW_CHECKBOX = "list-view-pf-checkbox"; // $NON-NLS-1$
    public static final String PF_LIST_VIEW_EXPAND_ACTIVE = "list-view-pf-expand-active"; // $NON-NLS-1$
    public static final String PF_LIST_VIEW = "list-view-pf"; // $NON-NLS-1$
    public static final String PF_LIST_VIEW_VIEW = "list-view-pf-view"; // $NON-NLS-1$
    public static final String PF_LIST_VIEW_TOP_ALIGN = "list-view-pf-top-align"; // $NON-NLS-1$
    public static final String PF_LIST_VIEW_STACKED = "list-view-pf-stacked"; // $NON-NLS-1$
    public static final String LIST_VIEW_ICON_PANEL = "list-view-icon-panel"; // $NON-NLS-1$

    public static final String PF_PROGRESS_DESCRIPTION = "progress-description"; // $NON-NLS-1$
    public static final String PF_PROGRESS_LABEL_TOP_RIGHT = "progress-label-top-right"; // $NON-NLS-1$

    public static final String PF_LABEL_INFO = "label-info"; // $NON-NLS-1$
    public static final String CENTER_TEXT = "text-center"; // $NON-NLS-1$

    public static final String PF_PANEL_TITLE = "panel-title"; // $NON-NLS-1$
    public static final String PF_PANEL_COUNTER = "panel-counter"; // $NON-NLS-1$
    public static final String COLLAPSED = "collapsed"; // $NON-NLS-1$

    public static final String PF_TABLE_BORDERED = "table-bordered"; // $NON-NLS-1$
    public static final String PF_TABLE_STRIPED = "table-striped"; // $NON-NLS-1$

}
