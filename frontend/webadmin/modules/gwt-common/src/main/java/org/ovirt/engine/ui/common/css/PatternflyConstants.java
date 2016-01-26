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

    // pficons

    // TODO insert all classes from PatternFly's less/icons.less

    public static final String PFICON = "pficon"; //$NON-NLS-1$
    public static final String PFICON_ERROR_CIRCLE_O = "pficon-error-circle-o"; //$NON-NLS-1$
    public static final String PFICON_WARNING_TRIANGLE_O = "pficon-warning-triangle-o"; //$NON-NLS-1$
    public static final String PFICON_OK = "pficon-ok"; //$NON-NLS-1$
    public static final String PFICON_INFO = "pficon-info"; //$NON-NLS-1$

    // things in ovirt-patternfly-compat.css

    public static final String TEMP_LINK_COLOR = "temp-link-color"; //$NON-NLS-1$


}
