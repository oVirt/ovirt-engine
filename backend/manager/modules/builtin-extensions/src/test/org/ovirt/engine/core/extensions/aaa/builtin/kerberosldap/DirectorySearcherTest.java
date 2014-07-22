package org.ovirt.engine.extensions.aaa.builtin.kerberosldap;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.ovirt.engine.core.utils.MockConfigRule.mockConfig;

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

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.utils.MockConfigRule;
import org.ovirt.engine.extensions.aaa.builtin.kerberosldap.utils.ldap.LdapProviderType;

/**
 * Tests GetRootDSE functionality In this test it is checked how GetRootDSE handles a various scenarios *
 */
public class DirectorySearcherTest extends AbstractLdapTest {

    @ClassRule
    public static MockConfigRule mcr = new MockConfigRule(
            mockConfig(ConfigValues.LDAPQueryTimeout, 2),
            mockConfig(ConfigValues.LDAPConnectTimeout, 10),
            mockConfig(ConfigValues.LDAPOperationTimeout, 30),
            mockConfig(ConfigValues.LDAPProviderTypes, "example.com:general")
            );

    private static final String BAD_URL = "ldap://badurl.com:389";

    private DirContext dirContext;

    private static final ExecutorService executerService = Executors.newSingleThreadExecutor();

    @Override
    @Before
    public void setup() throws Exception {
        super.setup();
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
        assertTrue(task1.call());
        assertTrue(task2.call());
    }

    protected GetRootDSETask mockRootDSETask(DirectorySearcher dirSearcher, String domain, URI url) {
        GetRootDSETask task = new GetRootDSETask(dirSearcher, domain, url);
        task = spy(task);
        doReturn(mockGetRootDSE(url)).when(task).createGetRootDSE(url);
        return task;
    }

    @Test(expected = TimeoutException.class)
    public void testGetRootDSEFirstSeverUnreachable() throws Exception {
        List<URI> urls = new ArrayList<URI>();
        urls.add(new URI(BAD_URL));
        urls.add(new URI("ldap://ldap1.example.com:389"));
        DirectorySearcher dirSearcher = mockDirectorySearcher(urls);
        execute(new GetRootDSETask(dirSearcher, "mydomain", urls.get(0)));
    }

    @Test(expected = TimeoutException.class)
    public void testGetRootDSENoServers() throws Exception {
        List<URI> urls = new ArrayList<URI>();
        DirectorySearcher dirSearcher = mockDirectorySearcher(urls);
        GetRootDSETask getRootDSETask = new GetRootDSETask(dirSearcher, "mydomain", null);
        execute(getRootDSETask);
    }

    @Test(expected = TimeoutException.class)
    public void testGetRootDSENoReachableLdapServers() throws Exception {
        List<URI> urls = new ArrayList<URI>();
        urls.add(new URI(BAD_URL));
        DirectorySearcher dirSearcher = mockDirectorySearcher(urls);
        execute(new GetRootDSETask(dirSearcher, "mydomain", urls.get(0)));
    }

    private DirectorySearcher mockDirectorySearcher(final List<URI> urls) {
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

    protected Domain mockDomainObject(List<URI> urls) {
        final Domain domain = new Domain("");
        domain.setLdapServers(urls);
        domain.setLdapServers(urls);
        domain.setLdapProviderType(LdapProviderType.openLdap);
        return domain;
    }

    @SuppressWarnings("unchecked")
    protected GetRootDSE mockGetRootDSE(URI uri) {
        GetRootDSE getRootDSE = spy(new GetRootDSE(uri));
        try {
            doReturn(dirContext).when(getRootDSE).createContext(any(Hashtable.class));
        } catch (NamingException ignored) {
            // this exception is expected
        }
        doAnswer(new Answer<URI>() {
            @Override
            public URI answer(InvocationOnMock invocation) throws Throwable {
                URI realURIResult = (URI) invocation.callRealMethod();
                setValidProvider(!realURIResult.toString().equals(BAD_URL));
                return realURIResult;
            }
        }).when(getRootDSE).getLdapURI();
        return getRootDSE;
    }

    private static Boolean execute(GetRootDSETask task) throws InterruptedException, ExecutionException,
            TimeoutException {
        return executerService.submit(task).get(1, TimeUnit.MILLISECONDS);
    }
}
