package org.ovirt.checkstyle.checks;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

/**
 * Make sure that classes have no-argument constructor (with any access modifier), i.e. either explicit or <a
 * href="http://docs.oracle.com/javase/specs/jls/se7/html/jls-8.html#jls-8.8.9">default</a> constructor without
 * arguments.
 */
public class NoArgConstructorCheck extends AbstractCheck {

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
        return new int[] { TokenTypes.CLASS_DEF };
    }

    private boolean run = true;

    public void setRun(boolean run) {
        this.run = run;
    }

    @Override
    public void visitToken(DetailAST classDef) {
        if (!run) {
            return;
        }

        DetailAST objBlock = classDef.findFirstToken(TokenTypes.OBJBLOCK);
        DetailAST child = objBlock.getFirstChild();
        boolean hasExplicitCtor = false;

        while (child != null) {
            if (child.getType() == TokenTypes.CTOR_DEF) {
                hasExplicitCtor = true;
                DetailAST ctorParams = child.findFirstToken(TokenTypes.PARAMETERS);
                if (ctorParams.getChildCount() == 0) {
                    // Found no-argument constructor
                    return;
                }
            }
            child = child.getNextSibling();
        }

        if (hasExplicitCtor) {
            DetailAST classIdent = classDef.findFirstToken(TokenTypes.LITERAL_CLASS).getNextSibling();
            log(classIdent.getLineNo(), classIdent.getColumnNo(),
                    "Class {0} must have a no-argument constructor (with any access modifier)",
                    classIdent.getText());
        }
    }

}
