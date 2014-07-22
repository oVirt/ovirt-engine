package org.ovirt.engine.extensions.aaa.builtin.kerberosldap.utils.dns;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

import javax.naming.NamingException;

import org.junit.Test;
import org.ovirt.engine.extensions.aaa.builtin.kerberosldap.utils.dns.DnsSRVLocator.DnsSRVResult;


public class DnsSRVLocatorTest {

    protected DnsSRVLocator mockLocator(String domainName, String... records) throws NamingException {
        DnsSRVLocator mock = spy(new DnsSRVLocator());
        doReturn(records).when(mock).getSrvRecords("ldap.tcp.example.com");
        return mock;
    }

    @Test
    public void testZeroWeights() throws NamingException {
        // PRIORITY WEIGHT PORT HOST

        DnsSRVLocator locator = mockLocator("example.com",
                "10 100 389 a.example.com",
                "20 100 389 b.example.com",
                "20 0 389 c.example.com",
                "20 0 389 d.example.com",
                "20 200 398 e.example.com",
                "30 100 389 f.example.com");
        try {
            DnsSRVResult result = locator.getService("ldap", "tcp", "example.com");
            for (String server : locator.getServersList(result)) {
                System.out.println(server);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Test
    public void testZeroPriorities() throws Exception {
        DnsSRVLocator locator = mockLocator("example.com",
                "10 100 389 a.example.com.",
                "10 100 389 b.example.com.",
                "10 100 389 c.example.com.",
                "10 100 389 d.example.com.",
                "10 100 389 e.example.com.",
                "10 100 389 f.example.com.",
                "0 100 389 g.example.com.",
                "10 100 389 h.example.com.",
                "10 100 389 i.example.com.",
                "10 100 389 k.example.com.",
                "10 100 389 l.example.com.",
                "10 100 389 m.example.com.",
                "10 100 389 n.example.com.",
                "10 100 389 o.example.com.",
                "10 100 389 p.example.com.",
                "10 100 389 q.example.com.",
                "10 100 389 r.example.com.",
                "10 100 389 s.example.com.",
                "10 100 389 t.example.com.",
                "10 100 389 u.example.com.",
                "10 100 389 v.example.com.",
                "10 100 389 w.example.com.",
                "10 100 389 x.example.com.",
                "5 100 389 y.example.com.",
                "10 100 389 z.example.com.",
                "10 100 389 alpha.example.com.",
                "0 100 389 betta.example.com.",
                "10 100 389 gamma.example.com.",
                "10 100 389 delta.example.com.",
                "10 100 389 one.example.com.",
                "10 100 389 two.example.com.",
                "10 100 389 three.example.com.");
        DnsSRVResult result = locator.getService("ldap", "tcp", "example.com");
        assertEquals(32, result.getNumOfValidAddresses());

    }


}
