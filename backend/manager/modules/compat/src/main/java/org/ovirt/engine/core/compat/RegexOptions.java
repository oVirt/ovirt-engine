package org.ovirt.engine.core.compat;

import java.util.regex.Pattern;

/**
 * @deprecated Use {@link Pattern}'s options directly instead
 */
@Deprecated
public class RegexOptions {

    public final static int Compiled = 0; // Java RegExp are always compiled,
                                          // this option makes no sense in
                                          // Java context
    public final static int IgnoreCase = Pattern.CASE_INSENSITIVE;
    public final static int Singleline = Pattern.DOTALL;
    public static final RegexOptions None = null;

}
