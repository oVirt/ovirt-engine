package org.ovirt.engine.ui.gwtextension;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.gwt.dev.jjs.ast.JDeclaredType;
import com.google.gwt.dev.jjs.ast.JMethod;
import com.google.gwt.dev.jjs.ast.JProgram;
import com.google.gwt.dev.jjs.impl.ControlFlowAnalyzer;
//import com.google.gwt.dev.util.log.PrintWriterTreeLogger;
//import com.google.gwt.core.ext.TreeLogger;

import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

@Aspect
public class DontPrune {
  //PrintWriterTreeLogger logger = new PrintWriterTreeLogger();

  /**
   * The name of the system property that contains the regular expression that
   * indicates which types should not be pruned.
   */
  private static final String GWT_DONTPRUNE = "gwt.dontPrune";

  /**
   * This pointcut captures the execution of the constructor of the liveness
   * analyzer.
   */
  @Pointcut(
      "target(analyzer) && " +
      "args(program) && " +
      "execution(public com.google.gwt.dev.jjs.impl.ControlFlowAnalyzer.new(com.google.gwt.dev.jjs.ast.JProgram))"
  )
  public void createLivenessAnalyzer(ControlFlowAnalyzer analyzer, JProgram program) {}

  /**
   * Just after the invocation of the liveness analyzer exclude all the fields
   * and methods inside the classes whose name match the regular expression given
   * in the system property.
   */
  @After("createLivenessAnalyzer(analyzer, program)")
  public void afterCreateLivenessAnalyzer(ControlFlowAnalyzer analyzer, JProgram program) {
    // Get the regular expression and warn the user if it is empty:
    String dontPruneRe = System.getProperty(GWT_DONTPRUNE);
    if (dontPruneRe == null) {
      //TODO: Replace with logger
      System.err.println("The system property \"" + GWT_DONTPRUNE + "\" that specifies the types not to be pruned wasn't set!");
      throw new RuntimeException("The system property \"" + GWT_DONTPRUNE + "\" that specifies the types not to be pruned wasn't set!");
    }

    //logger.finest("Types matching \"" + dontPruneRe + "\" won't be pruned.");

    // Compile the regular expression:
    Pattern dontPrunePattern = Pattern.compile(dontPruneRe);

    // Scan all the types and make sure that the ones matching the regular expression
    // are not pruned:
    for (JDeclaredType type : program.getDeclaredTypes()) {
      String typeName = type.getName();
      Matcher matcher = dontPrunePattern.matcher(typeName);
      if (matcher.matches()) {
        //logger.log(TreeLogger.SPAM,"Type \"" + typeName + "\" matched and won't be pruned!");
        //TODO: Too verbose, needs to be logged by a standard GWT logger in a SPAM level
        //System.out.println("Type \"" + typeName + "\" matched and won't be pruned!");
        analyzer.traverseFromInstantiationOf(type);
        for (JMethod method : type.getMethods()) {
          analyzer.traverseFrom(method);
        }
      }
    }
  }

}
