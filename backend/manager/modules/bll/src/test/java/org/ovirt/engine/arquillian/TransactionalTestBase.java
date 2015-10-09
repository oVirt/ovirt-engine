package org.ovirt.engine.arquillian;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import org.jboss.aerogear.arquillian.junit.ArquillianRule;
import org.jboss.aerogear.arquillian.junit.ArquillianRules;
import org.jboss.shrinkwrap.api.ArchivePaths;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.ovirt.engine.arquillian.database.DataSourceFactory;
import org.ovirt.engine.arquillian.database.TransactionRollbackRule;
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

@RunWith(ArquillianRules.class)
public abstract class TransactionalTestBase {

    @Inject
    protected VmBuilder vmBuilder;

    @Inject
    protected VdsBuilder vdsBuilder;

    @Inject
    protected ClusterBuilder clusterBuilder;

    @Inject
    private Injector injector;

    protected Cluster defaultCluster;

    protected VDS defaultHost;

    protected VM defaultVM;

    @ArquillianRule
    public TransactionRollbackRule rollbackRule = new TransactionRollbackRule();

    public static JavaArchive createDeployment(){
        return createDeployment(new ArrayList<Class<?>>());
    }

    public static JavaArchive createDeployment(List<Class<?>> classes){
        return createDeployment(classes.toArray(new Class<?>[classes.size()]));
    }

    public static JavaArchive createDeployment(Class<?>[] classes) {
        final Class<?>[] defaultClasses = {
                DataSourceFactory.class,
                TransactionRollbackRule.class,
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
        JavaArchive archive = ShrinkWrap.create(JavaArchive.class)
                .addPackages(true, Dao.class.getPackage()) // add all DAOs
                .addPackage(AbstractBuilder.class.getPackage()) // add all builder
                .addPackage(TransactionRollbackRule.class.getPackage()) // database related stuff
                .addClasses(
                        classList.toArray(new Class<?>[classList.size()])
                ).addAsManifestResource(
                        EmptyAsset.INSTANCE,
                        ArchivePaths.create("beans.xml")
                );
        return archive;
    }

    @Before
    public void setUpDefaultEntities() {
        defaultCluster = clusterBuilder.persist();
        defaultHost = vdsBuilder.cluster(defaultCluster).persist();
        defaultVM = vmBuilder.host(defaultHost).up().persist();
    }
}
