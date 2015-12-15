package org.ovirt.checkstyle.checks;

import com.puppycrawl.tools.checkstyle.api.Check;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

/**
 * Make sure that classes have no-argument constructor (with any access modifier), i.e. either explicit or <a
 * href="http://docs.oracle.com/javase/specs/jls/se7/html/jls-8.html#jls-8.8.9">default</a> constructor without
 * arguments.
 */
public class NoArgConstructorCheck extends Check {

    private boolean run = true;

    public void setRun(boolean run) {
        this.run = run;
    }

    @Override
    public int[] getDefaultTokens() {
        return new int[] { TokenTypes.CLASS_DEF };
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
