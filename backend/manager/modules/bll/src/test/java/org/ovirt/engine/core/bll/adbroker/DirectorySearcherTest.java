package org.ovirt.engine.core.bll.adbroker;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

import java.net.URI;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.naming.NamingException;
import javax.naming.directory.DirContext;

import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.config.IConfigUtilsInterface;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.powermock.api.mockito.PowerMockito.mock;

/**
 * Tests GetRootDSE functionality In this test it is checked how GetRootDSE handles a various scenarios *
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(Config.class)
public class DirectorySearcherTest extends AbstractLdapTest {

    private static final String BAD_URL = "ldap://badurl.com:389";

    private DirContext dirContext;

    private static final ExecutorService executerService = Executors.newSingleThreadExecutor();;

    @BeforeClass
    public static void beforeClass() {
        System.setProperty("jboss.server.log.dir", "/tmp");
    }

    @Override
    @Before
    public void setup() throws Exception {
        super.setup();
        mockStatic(Config.class);
        when(Config.GetValue(ConfigValues.LDAPQueryTimeout)).thenReturn(30);
        when(Config.GetValue(ConfigValues.LDAPProviderTypes)).thenReturn("example.com:general");
        when(Config.getConfigUtils()).thenReturn(mock(IConfigUtilsInterface.class));
        MockitoAnnotations.initMocks(this);
        dirContext = mockDirContext();
    }

    @Test
    public void testGetRootDSEReachableServers() throws Exception {
        List<URI> urls = new ArrayList<URI>();
        urls.add(new URI("ldap://ldap1.example.com:389"));
        urls.add(new URI("ldap://ldap2.example.com:389"));
        DirectorySearcher dirSearcher = mockDirectorySearcher(urls);
        GetRootDSETask task1 = mockRootDSETask(dirSearcher, "mydomain", urls.get(0));
        GetRootDSETask task2 = mockRootDSETask(dirSearcher, "mydomain", urls.get(1));
        try {
        assertTrue(task1.call());
        assertTrue(task2.call());
        } catch (Exception e) {
            Assert.fail("one of the servers was failed to return rootDSE record due to " + e.getMessage());
        }
    }

    protected GetRootDSETask mockRootDSETask(DirectorySearcher dirSearcher, String domain, URI url) {
        GetRootDSETask task = new GetRootDSETask(dirSearcher, domain, url);
        task = spy(task);
        doReturn(mockGetRootDSE(url)).when(task).createGetRootDSE(url);
        return task;
    }


    @Test
    public void testGetRootDSEFirstSeverUnreachable() throws Exception {
        List<URI> urls = new ArrayList<URI>();
        urls.add(new URI(BAD_URL));
        urls.add(new URI("ldap://ldap1.example.com:389"));
        DirectorySearcher dirSearcher = mockDirectorySearcher(urls);
        try {
            execute(new GetRootDSETask(dirSearcher, "mydomain", urls.get(0)));
        } catch (Exception e) {
            // server should timeout!
            return;
        }
        Assert.fail("Get rootDSE task passed with no timeout. Investigate why the task completed instead of throwing TimeoutException");
    }

    @Test
    public void testGetRootDSENoServers() throws Exception {
        List<URI> urls = new ArrayList<URI>();
        DirectorySearcher dirSearcher = mockDirectorySearcher(urls);
        try {
            GetRootDSETask getRootDSETask = new GetRootDSETask(dirSearcher, "mydomain", urls.get(0));
            execute(getRootDSETask);
        } catch (Exception e) {
            // should fail
            return;
        }
        Assert.fail("Task didn't exited with error");
    }

    @Test
    public void testGetRootDSENoReachableLdapServers() throws Exception {
        List<URI> urls = new ArrayList<URI>();
        urls.add(new URI(BAD_URL));
        DirectorySearcher dirSearcher = mockDirectorySearcher(urls);
        try {
            execute(new GetRootDSETask(dirSearcher, "mydomain", urls.get(0)));
        } catch (Exception e) {
            // non reachable domain should timeout
            if (e instanceof TimeoutException) {
                return;
            }
        }
        Assert.fail("Task ended with error which is not timout or no error at all");
    }

    @SuppressWarnings("unchecked")
    private DirectorySearcher mockDirectorySearcher(final List<URI> urls) throws NamingException {
        DirectorySearcher dirSearcher = spy(new DirectorySearcher(new LdapCredentials("username", "password")));
        doAnswer(new Answer<Domain>() {
            @Override
            public Domain answer(InvocationOnMock invocation) throws Throwable {
                return mockDomainObject(urls);
            }

        }).when(dirSearcher).getDomainObject(any(String.class));

        doAnswer(new Answer<GetRootDSE>() {
            @Override
            public GetRootDSE answer(InvocationOnMock invocation) throws Throwable {
                URI uri = (URI) invocation.getArguments()[0];
                return mockGetRootDSE(uri);
            }
        }).when(dirSearcher).createRootDSE(any(URI.class));
        return dirSearcher;
    }

    private Domain mockDomainObject(List<URI> urls) {
        final Domain domain = new Domain("");
        domain.setLdapServers(urls);
        domain.setLdapServers(urls);
        domain.setLdapProviderType(LdapProviderType.general);
        return domain;
    }

    @SuppressWarnings("unchecked")
    private GetRootDSE mockGetRootDSE(URI uri) {
        GetRootDSE getRootDSE = spy(new GetRootDSE(uri));
        try {
            doReturn(dirContext).when(getRootDSE).createContext(any(Hashtable.class));
        } catch (NamingException e) {
        }
        doAnswer(new Answer<URI>() {
            @Override
            public URI answer(InvocationOnMock invocation) throws Throwable {
                URI uri = (URI) invocation.callRealMethod();
                setValidProvider(!uri.toString().equals(BAD_URL));
                return uri;
            }
        }).when(getRootDSE).getLdapURI();
        return getRootDSE;
    }

    private Boolean execute(GetRootDSETask task) throws InterruptedException, ExecutionException, TimeoutException {
        // Execution timeout after 20 seconds.
        // Why 20?
        // Currently GetRootDSE swallows NamingException that is
        // thrown during a socket connect, causing the callable to exit with
        // no Timeout exception at all, leading to false test results.
        //
        // To satisfy different machines and enviornments this call will timeout
        // earlier than most resonable tcp/ip setups
        return executerService.submit(task).get(20, TimeUnit.SECONDS);
    }
}
