package org.ovirt.engine.arquillian;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ArchivePaths;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.ovirt.engine.arquillian.database.DataSourceFactory;
import org.ovirt.engine.core.bll.aaa.SessionDataContainer;
import org.ovirt.engine.core.bll.scheduling.CommonTestMocks;
import org.ovirt.engine.core.builder.AbstractBuilder;
import org.ovirt.engine.core.builder.ClusterBuilder;
import org.ovirt.engine.core.builder.VdsBuilder;
import org.ovirt.engine.core.builder.VmBuilder;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.Dao;
import org.ovirt.engine.core.di.Injector;

@RunWith(Arquillian.class)
public abstract class TransactionalTestBase {
    @Inject
    protected VmBuilder vmBuilder;

    @Inject
    protected VdsBuilder vdsBuilder;

    @Inject
    protected ClusterBuilder clusterBuilder;

    @Inject
    private Injector injector;

    @Inject
    private DbFacade dbFacade;

    protected Cluster defaultCluster;

    protected VDS defaultHost;

    protected VM defaultVM;

    public static JavaArchive createDeployment(){
        return createDeployment(new ArrayList<>());
    }

    public static JavaArchive createDeployment(List<Class<?>> classes){
        return createDeployment(classes.toArray(new Class<?>[classes.size()]));
    }

    public static JavaArchive createDeployment(Class<?>[] classes) {
        final Class<?>[] defaultClasses = {
                DataSourceFactory.class,
                CommonTestMocks.class,
                Injector.class,
                SessionDataContainer.class,
                DbFacade.class
        };
        final List<Class<?>> classList = new ArrayList<>();
        classList.addAll(Arrays.asList(defaultClasses));
        if (classes != null && classes.length > 0){
            classList.addAll(Arrays.asList(classes));
        }
        return ShrinkWrap.create(JavaArchive.class)
                .addPackages(true, Dao.class.getPackage()) // add all DAOs
                .addPackage(AbstractBuilder.class.getPackage()) // add all builder
                .addClasses(
                        classList.toArray(new Class<?>[classList.size()])
                ).addAsManifestResource(
                        EmptyAsset.INSTANCE,
                        ArchivePaths.create("beans.xml")
                );
    }

    @Before
    public void setUpDefaultEnvironment() {
        // Set the right location of a minimal oVirt configuration
        System.setProperty("ovirt-engine.config.defaults", "src/test/resources/engine.conf.defaults");

        defaultCluster = clusterBuilder.persist();
        defaultHost = vdsBuilder.cluster(defaultCluster).persist();
        defaultVM = vmBuilder.host(defaultHost).up().persist();
    }
}
