package org.ovirt.engine.extensions.aaa.builtin.kerberosldap;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.List;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

public class AbstractLdapTest {

    private boolean validProvider = false;

    protected void setup() throws Exception {
        validProvider = false;
    }

    protected void setValidProvider(boolean value) {
        validProvider = value;
    }

    protected DirContext mockDirContext(final List<SearchResult> searchResults)
            throws NamingException {
        DirContext dirContext = mock(DirContext.class);
        doAnswer(new Answer<NamingEnumeration<SearchResult>>() {
            @Override
            public NamingEnumeration<SearchResult> answer(InvocationOnMock invocation) throws Throwable {
                if (!validProvider) {
                    throw new NamingException("LDAP provider is invalid");
                }
                NamingEnumeration<SearchResult> result = createNamingEnumeration(searchResults);
                return result;
            }

        }).when(dirContext).search(any(String.class), any(String.class), any(SearchControls.class));
        return dirContext;
    }

    protected DirContext mockDirContext() throws NamingException {
        Attributes attributes = new BasicAttributes();
        attributes.put("namingContexts", "DC=example,DC=com");
        attributes.put("domainControllerFunctionality", "1");
        attributes.put("defaultNamingContext", "dummyContext");
        final List<SearchResult> searchResults = new ArrayList<SearchResult>();
        searchResults.add(new SearchResult("1", "dummy", attributes));
        searchResults.add(new SearchResult("2", "dummy2", attributes));
        searchResults.add(new SearchResult("3", "dummy3", attributes));
        return mockDirContext(searchResults);
    }

    protected NamingEnumeration<SearchResult> createNamingEnumeration(final List<SearchResult> searchResults) {
        return new NamingEnumeration<SearchResult>() {

            private int index = 0;

            @Override
            public boolean hasMoreElements() {
                return index < searchResults.size();
            }

            @Override
            public SearchResult nextElement() {
                return searchResults.get(index++);
            }

            @Override
            public SearchResult next() throws NamingException {
                return nextElement();
            }

            @Override
            public boolean hasMore() throws NamingException {
                return hasMoreElements();
            }

            @Override
            public void close() throws NamingException {
            }
        };
    }
}
