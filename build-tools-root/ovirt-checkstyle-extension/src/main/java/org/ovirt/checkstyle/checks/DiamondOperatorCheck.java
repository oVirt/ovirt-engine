////////////////////////////////////////////////////////////////////////////////
// checkstyle: Checks Java source code for adherence to a set of rules.
// Copyright (C) 2001-2011  Oliver Burn
// Copyright (C) 2015       Allon Mureinik (adoption to oVirt)
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
////////////////////////////////////////////////////////////////////////////////

package org.ovirt.checkstyle.checks;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

/**
 * This Check highlights generic specifications that could be simplified by type inference (<a href=
 * "http://docs.oracle.com/javase/7/docs/technotes/guides/language/type-inference-generic-instance-creation.html">
 * diamond operator</a>).
 *
 * It is largely based on the work of Aleksey Nesterenko from the
 * <a href="https://github.com/sevntu-checkstyle">sventu-checkstyle</a>.
 *
 * @author <a href="mailto:amureini@redhat.com">Allon Mureinik</a> (oVirt adaptation)
 */
public class DiamondOperatorCheck extends AbstractCheck {
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
        return new int[] { TokenTypes.ASSIGN };
    }

    @Override
    public void visitToken(DetailAST assignNode) {
        DetailAST expressionNode = assignNode.getFirstChild();
        while (expressionNode != null) {
            failCheck(expressionNode);
            expressionNode = expressionNode.getNextSibling();
        }
    }

    protected void failCheck(DetailAST expressionNode) {
        if (expressionNode == null) {
            return;
        }

        if (expressionNode.getType() == TokenTypes.LITERAL_NEW) {
            DetailAST typeArgs = expressionNode.findFirstToken(TokenTypes.TYPE_ARGUMENTS);

            if (typeArgs != null &&
                    typeArgs.getFirstChild() != null &&
                    typeArgs.getFirstChild().getType() == TokenTypes.GENERIC_START &&
                    expressionNode.findFirstToken(TokenTypes.OBJBLOCK) == null) {
                DetailAST startToken = typeArgs.getFirstChild();
                DetailAST afterStart = startToken.getNextSibling();
                while (afterStart != null) {
                    if (afterStart.getType() == TokenTypes.GENERIC_END) {
                        return;
                    }
                    if (afterStart.getFirstChild() != null &&
                            !afterStart.getFirstChild().getText().equals("?")) {
                        log(typeArgs.getLineNo(), typeArgs.getColumnNo(), "Diamond operator should be used");
                        return;
                    }
                    afterStart = afterStart.getNextSibling();
                }
            }
        }

        failCheck(expressionNode.getFirstChild());
    }
}

