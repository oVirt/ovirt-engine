package org.ovirt.engine.core.bll.utils;

import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;

import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;

/**
 * This class is made to keep all producers of bll dependencies, generally singletons from other jars in one place for the
 * dependency scanner to identify. except for producers like for Log (see produceLogger),
 * this class will bridge the dependency resolution limitation we have between non-ejb module jars which contains
 * dependencies to the bll ejb-jar. NOTE: future jboss version should solve that so we could have beans dependencies
 * scanned from other non-ejb module jars such as org.ovirt.engine.core:dal
 *
 * Producers could be declared either by a field or by a method, to get more control on the production of an instance
 *
 * There is no need to instantiate this class and there is no visible usage to it but to the dependency scanner
 *
 *
 */
public class BllCDIAdapter {

    @Produces
    DbFacade dbFacade = DbFacade.getInstance();

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


    private BllCDIAdapter() {
        // hide me
    }

}
