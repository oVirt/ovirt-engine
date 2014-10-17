package org.ovirt.engine.core.utils.log;

import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;

import org.slf4j.LoggerFactory;

@Deprecated
public class LogFactory {

    public static Log getLog(Class<?> loggedClass) {
        return new Log(LoggerFactory.getLogger(loggedClass));
    }

    /**
     * this producer enables injection of Log instance. Its not
     * intended to be used as protected instance as the runtime instance is the declaring class so
     * use directly as a private member.
     *
     *  <code>
     *      @Inject
     *      private Log log;
     *  </code>
     *
     * @param injectionPoint
     */
    @Produces
    public Log produceLogger(InjectionPoint injectionPoint) {
        return LogFactory.getLog(injectionPoint.getMember().getDeclaringClass());
    }
}
