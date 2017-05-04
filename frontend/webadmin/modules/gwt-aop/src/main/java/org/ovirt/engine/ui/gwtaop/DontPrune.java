package org.ovirt.engine.ui.gwtaop;

import java.util.regex.Pattern;

import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

import com.google.gwt.dev.jjs.ast.JMethod;
import com.google.gwt.dev.jjs.ast.JProgram;
import com.google.gwt.dev.jjs.impl.ControlFlowAnalyzer;

/**
 * Advises GWT {@linkplain ControlFlowAnalyzer live code analyzer} to ensure that
 * given Java classes, as defined by <code>{@value GWT_DONTPRUNE}</code> system
 * property, are always treated as live code and never pruned from the GWT-generated
 * JavaScript code.
 */
@Aspect
public class DontPrune {

    /**
     * The name of the system property holding a regular expression that indicates
     * which types (based on their {@linkplain Class#getName binary name}) should
     * not be pruned.
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
     * Right after constructing the liveness analyzer, make sure that all types
     * matched by the regular expression are marked as if they were referenced
     * by the GWT application code.
     */
    @After("createLivenessAnalyzer(analyzer, program)")
    public void afterCreateLivenessAnalyzer(ControlFlowAnalyzer analyzer, JProgram program) {
        // Get the regular expression and warn the user if it's empty:
        String dontPruneRe = System.getProperty(GWT_DONTPRUNE);
        if (dontPruneRe == null) {
            String error = "The system property '" + GWT_DONTPRUNE
                    + "' that specifies the types not to be pruned wasn't set!";
            System.err.println(error);
            throw new RuntimeException(error);
        }

        // Compile the regular expression:
        Pattern dontPrunePattern = Pattern.compile(dontPruneRe);

        // Scan all the types and make sure that the ones matching the given regular
        // expression are not pruned:
        program.getDeclaredTypes().stream()
                .filter(type -> dontPrunePattern.matcher(type.getName()).matches())
                .forEach(type -> {
                    // Note: calling "analyzer.traverseFromInstantiationOf(type)"
                    // may cause GWT compiler to crash on NPE; don't use that method:
                    analyzer.traverseFromReferenceTo(type);

                    // Make sure that all methods of the given type are not pruned:
                    for (JMethod method : type.getMethods()) {
                        analyzer.traverseFrom(method);
                    }
                });
    }

}
