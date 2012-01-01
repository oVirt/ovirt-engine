package org.ovirt.engine.core.itests;

import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.jboss.embedded.Bootstrap;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.ovirt.engine.core.bll.Backend;
import org.ovirt.engine.core.bll.InitBackendServicesOnStartup;
import org.ovirt.engine.core.bll.InitBackendServicesOnStartupBean;
import org.ovirt.engine.core.bll.MultiLevelAdministrationHandler;
import org.ovirt.engine.core.bll.PredefinedRoles;
import org.ovirt.engine.core.bll.interfaces.BackendInternal;
import org.ovirt.engine.core.bll.session.SessionDataContainer;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.businessentities.DbUser;
import org.ovirt.engine.core.common.businessentities.permissions;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.common.users.VdcUser;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.utils.ThreadLocalParamsContainer;
import org.ovirt.engine.core.utils.ejb.EjbUtils;
import org.ovirt.engine.core.utils.ejb.JBossEmbeddedEJBUtilsStrategy;
import org.ovirt.engine.core.utils.timer.SchedulerUtilQuartzImpl;

/**
 * A self contained, ready to use JBoss loader to test backend flows, actions, queries etc. This class starts a stripped
 * embedded JBoss with a DB data-source and all required EJB's registered. In order to perform a unit test one has to
 * extend this class and created add a test method of its own.(simple void method annotated with @Test) The class
 * Junit's <code>BeforeClass</code> method will start the embedded JBoss container. Check list for the test to startup:
 * 1.make sure your current machine doesn't run JBoss already ("port already in use error...") 2.make sure the test runs
 * with JVM flag -Dsun.lang.ClassLoader.allowArraySyntax=true
 *
 */
public abstract class AbstractBackendTest {
    private static String SESSION;
    protected static Bootstrap bootstrap;
    protected static BackendInternal backend;
    private static BasicTestSetup basicTestSetup;

    public static String testSequence;
    public static long testSequenceNumber;
    private static VdcUser user;
    private static String sessionId;

    @BeforeClass
    public static void startJboss() throws Exception {

        try {
            bootstrap = Bootstrap.getInstance();
            if (!bootstrap.isStarted()) {
                bootstrap.bootstrap();
                EjbUtils.setStrategy(new JBossEmbeddedEJBUtilsStrategy());

                bootstrap.deployResourceBase(SchedulerUtilQuartzImpl.class);

                SchedulerUtilQuartzImpl.getInstance().create(); // Start is
                                                               // called as
                                                               // post
                                                               // construct is
                                                               // not activated
                                                               // upon lookup
                // replace this resource manager with a test resource manager
                // for we wont have real hosts while testing
                bootstrap.deployResourceBase(VoidResourceManager.class);
                // bootstrap.deployResourceBase(Backend.class);

                backend = Backend.getInstance();
                assertNotNull(backend);
                backend.Initialize(); // Initialize is called as post construct
                                      // is not activated upon lookup
                InitBackendServicesOnStartup initBean = new InitBackendServicesOnStartupBean();
                initBean.create();
            }
        } catch (Throwable ex) {
            ex.printStackTrace();
            throw new Exception(ex);
        }
    }

    @Before
    public void setup() throws Exception {
        testSequenceNumber = System.currentTimeMillis();
        testSequence = "-JUnit-" + testSequenceNumber;
        System.out.println("\n\tReady to run tests.\n\t your test sequence is " + testSequence);
        generateUserSession();
    }

    @After
    public void cleanup() {
        if (basicTestSetup != null) {
            runAsSuperAdmin();
            basicTestSetup.cleanSetup();
            basicTestSetup = null;
        } else {
            removeSessionUser();
        }
    }

    /**
     *
     * Creates a user and put it on the session.Mainly needed for different permissions tests.
     *
     * This method is being called automatically by the JUnit framework using {@link #createSessionAndRandomUser()}
     */
    private static void generateUserSession() {
        sessionId = "SESSIONID" + testSequence;
        String domain = "example.com";
        String groupName = "JUnitTester";
        // add user to db
        VdcUser vdcUser = new VdcUser();
        vdcUser.setUserId(Guid.NewGuid());
        vdcUser.setDomainControler(domain);
        vdcUser.setUserName(groupName);
        vdcUser.setGroupNames(groupName);

        DbUser dbUser = new DbUser();
        dbUser.setuser_id(vdcUser.getUserId());
        dbUser.setusername(vdcUser.getUserName());
        dbUser.setdomain(vdcUser.getDomainControler());
        dbUser.setgroups(vdcUser.getGroupNames());

        DbFacade.getInstance().getDbUserDAO().save(dbUser);

        SessionDataContainer.getInstance().SetData(getSessionId(), "VdcUser", vdcUser);
        ThreadLocalParamsContainer.setHttpSessionId(getSessionId());
        ThreadLocalParamsContainer.setVdcUser(vdcUser);
        user = vdcUser;
    }

    /**
     * make the current user SuperAdmin by adding him permissions of SuperUser role on SYSTEM object
     */
    public static void runAsSuperAdmin() {
        if (sessionId == null) {
            generateUserSession();
        }

        permissions perms = new permissions();
        perms.setad_element_id(user.getUserId());
        perms.setObjectId(MultiLevelAdministrationHandler.SYSTEM_OBJECT_ID);
        perms.setObjectType(VdcObjectType.System);
        perms.setrole_id(PredefinedRoles.SUPER_USER.getId());
        try {
            DbFacade.getInstance().getPermissionDAO().save(perms);
        } catch (Exception e) {
            System.out.println("User is already super user.");
        }
    }

    /**
     * @return {@link BasicTestSetup} instance(one per test run) with entities ready to perform action on
     */
    protected BasicTestSetup getBasicSetup() {
        if (basicTestSetup == null) {
            basicTestSetup = new BasicTestSetup(backend);
        }
        return basicTestSetup;
    }

    public static String getSessionId() {
        return sessionId;
    }

    public static VdcActionParametersBase sessionize(VdcActionParametersBase params) {
        params.setSessionId(getSessionId());
        return params;
    }

    public static VdcQueryParametersBase sessionize(VdcQueryParametersBase params) {
        params.setSessionId(getSessionId());
        return params;
    }

    public static VdcUser getUser() {
        return user;
    }

    protected static void removeSessionUser() {
        if (getUser() != null) {
            List<permissions> perms =
                    DbFacade.getInstance().getPermissionDAO().getAllForAdElement(getUser().getUserId());
            for (permissions perm : perms) {
                DbFacade.getInstance().getPermissionDAO().remove(perm.getId());
            }
            DbFacade.getInstance().getDbUserDAO().remove(getUser().getUserId());
        }
    }
}
