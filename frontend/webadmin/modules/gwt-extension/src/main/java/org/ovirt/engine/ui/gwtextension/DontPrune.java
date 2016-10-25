package org.ovirt.engine.ui.gwtextension;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

import com.google.gwt.dev.jjs.ast.JDeclaredType;
import com.google.gwt.dev.jjs.ast.JMethod;
import com.google.gwt.dev.jjs.ast.JProgram;
import com.google.gwt.dev.jjs.impl.ControlFlowAnalyzer;

@Aspect
public class DontPrune {

    /**
     * The name of the system property that contains the regular expression that indicates
     * which types should not be pruned.
     */
    private static final String GWT_DONTPRUNE = "gwt.dontPrune";

    /**
     * This pointcut captures the execution of the constructor of the liveness analyzer.
     */
    @Pointcut("target(analyzer) && " +
            "args(program) && " +
            "execution(public com.google.gwt.dev.jjs.impl.ControlFlowAnalyzer.new(com.google.gwt.dev.jjs.ast.JProgram))")
    public void createLivenessAnalyzer(ControlFlowAnalyzer analyzer, JProgram program) {
    }

    /**
     * Just after the invocation of the liveness analyzer, exclude all the fields and methods
     * inside the classes whose name match the regular expression given in the system property.
     */
    @After("createLivenessAnalyzer(analyzer, program)")
    public void afterCreateLivenessAnalyzer(ControlFlowAnalyzer analyzer, JProgram program) {
        // Get the regular expression and warn the user if it is empty:
        String dontPruneRe = System.getProperty(GWT_DONTPRUNE);
        if (dontPruneRe == null) {
            String error = "The system property \"" + GWT_DONTPRUNE
                    + "\" that specifies the types not to be pruned wasn't set!";
            System.err.println(error);
            throw new RuntimeException(error);
        }

        // Compile the regular expression:
        Pattern dontPrunePattern = Pattern.compile(dontPruneRe);

        // Scan all the types and make sure that the ones matching the regular expression
        // are not pruned:
        for (JDeclaredType type : program.getDeclaredTypes()) {
            String typeName = type.getName();
            Matcher matcher = dontPrunePattern.matcher(typeName);

            if (matcher.matches()) {
                // logger.log(TreeLogger.SPAM,"Type \"" + typeName + "\" matched and won't be pruned!");
                // TODO: Too verbose, needs to be logged by a standard GWT logger in a SPAM level
                // System.out.println("Type \"" + typeName + "\" matched and won't be pruned!");

                // Following line causes GWT compiler to crash on NPE with gwt-jackson:
                // analyzer.traverseFromInstantiationOf(type);
                analyzer.traverseFromReferenceTo(type);

                for (JMethod method : type.getMethods()) {
                    analyzer.traverseFrom(method);
                }
            }
        }
    }

}
