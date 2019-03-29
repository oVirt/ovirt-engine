package org.ovirt.checkstyle.checks;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;
import com.puppycrawl.tools.checkstyle.utils.ScopeUtil;

/**
 * <p>
 * Checks if any class or object member is explicitly initialized instead of being initialized in the class'
 * constructor.
 * </p>
 * <p>
 * Rationale: Such calls are not interpreted by GWT's compiler, and if these objects are shared to the frontend, these
 * members may not be initialized.
 * </p>
 * <p>
 * This code was adapted from {@link com.puppycrawl.tools.checkstyle.checks.coding.ExplicitInitializationCheck}, and
 * should be removed once such a check is integrated into the checkstyle project.
 * </p>
 */
public class NoMemberInitializationCheck extends AbstractCheck {
    private boolean run = true;

    @Override
    public int[] getAcceptableTokens() {
        return getDefaultTokens();
    }

    @Override
    public int[] getDefaultTokens() {
        return getRequiredTokens();
    }

    @Override
    public int[] getRequiredTokens() {
        return new int[] { TokenTypes.VARIABLE_DEF };
    }

    @Override
    public void visitToken(DetailAST aAST) {
        if (!run) {
            return;
        }

        // do not check local variables and
        // fields declared in interface/annotations
        if (ScopeUtil.isLocalVariableDef(aAST) || ScopeUtil.isInInterfaceOrAnnotationBlock(aAST)) {
            return;
        }

        final DetailAST assign = aAST.findFirstToken(TokenTypes.ASSIGN);
        if (assign == null) {
            // no assign - no check
            return;
        }

        final DetailAST modifiers = aAST.findFirstToken(TokenTypes.MODIFIERS);
        if ((modifiers != null) && modifiers.branchContains(TokenTypes.FINAL)) {
            // do not check final variables
            return;
        }

        final DetailAST ident = aAST.findFirstToken(TokenTypes.IDENT);
        log(ident, "Initialization of members is not allowed", ident.getText());
    }

    public void setRun(boolean run) {
        this.run = run;
    }
}
