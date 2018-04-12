package org.ovirt.checkstyle.checks;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

/**
 * Since GWT can't handle member variables which have the private modifier, this check was added to make sure it is not
 * used in the packages that need to undergo GWT compilation.
 */
public class NoFinalMemberCheck extends AbstractCheck {
    private boolean run = true;

    /** This check is not configurable */

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
        return new int[] { TokenTypes.VARIABLE_DEF};
    }

    @Override
    public void visitToken(DetailAST aAST) {
        if (run) {
            if (aAST.getType() == TokenTypes.VARIABLE_DEF // A variable
                    && aAST.getParent().getType() == TokenTypes.OBJBLOCK // which is a class variable
                    && aAST.getParent().getParent().getType() != TokenTypes.ENUM_DEF // and not in an enum
                    ) {

                // find the modifiers
                DetailAST child =  aAST.getFirstChild();
                while (child != null) {
                    // final is only allowed for statics (i.e., constants)
                    if (child.branchContains(TokenTypes.FINAL) &&
                            !child.branchContains(TokenTypes.LITERAL_STATIC)) {
                        log(child.getLineNo(), child.getColumnNo(), "non-static final member variables are not allowed");
                    }
                    child = child.getNextSibling();
                }
            }
        }
    }

    public void setRun(boolean run) {
        this.run = run;
    }
}
