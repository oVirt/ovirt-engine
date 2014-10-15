package org.ovirt.engine.core.bll.utils;

import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;

import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.StoragePoolDAO;
import org.ovirt.engine.core.dao.VdsGroupDAO;
import org.ovirt.engine.core.dao.VmDynamicDAO;
import org.ovirt.engine.core.dao.network.HostNetworkQosDao;
import org.ovirt.engine.core.dao.network.InterfaceDao;
import org.ovirt.engine.core.dao.network.NetworkClusterDao;
import org.ovirt.engine.core.dao.network.NetworkDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
     * this producer enables injection of Logger instance. Its not
     * intended to be used as protected instance as the runtime instance is the declaring class so
     * use directly as a private member.
     *
     *  <code>
     *      @Inject
     *      private Logger log;
     *  </code>
     *
     * @param injectionPoint
     */
    @Produces
    public Logger produceLogger(InjectionPoint injectionPoint) {
        return LoggerFactory.getLogger(injectionPoint.getMember().getDeclaringClass());
    }

    @Produces
    private NetworkDao produceNetworkDao(DbFacade dbFacade) {
        return dbFacade.getNetworkDao();
    }

    @Produces
    private NetworkClusterDao produceNetworkClusterDao(DbFacade dbFacade) {
        return dbFacade.getNetworkClusterDao();
    }

    @Produces
    private VdsGroupDAO produceVdsGroupDao(DbFacade dbFacade) {
        return dbFacade.getVdsGroupDao();
    }

    @Produces
    private StoragePoolDAO produceStoragePoolDAO(DbFacade dbFacade) {
        return dbFacade.getStoragePoolDao();
    }

    @Produces
    private VmDynamicDAO produceVmDynamicDAO(DbFacade dbFacade) {
        return dbFacade.getVmDynamicDao();
    }

    @Produces
    private InterfaceDao produceInterfaceDao(DbFacade dbFacade) {
        return dbFacade.getInterfaceDao();
    }

    @Produces
    private HostNetworkQosDao produceHostNetworkQosDao(DbFacade dbFacade) {
        return dbFacade.getHostNetworkQosDao();
    }

    private BllCDIAdapter() {
        // hide me
    }

}
