package org.ovirt.engine.core.uutils.ssh;

import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import javax.naming.TimeLimitExceededException;

import org.apache.sshd.ClientSession;
import org.apache.sshd.SshClient;
import org.apache.sshd.client.future.AuthFuture;
import org.apache.sshd.client.future.ConnectFuture;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TimeoutTest extends TestCommon {

    SSHClient client;

    @Before
    public void setUp() {
        client = new SSHClient();
        client.setSoftTimeout(10 * 1000);
        client.setHardTimeout(60 * 1000);
        client.setHost(TestCommon.host, TestCommon.port);
        client.setUser(TestCommon.user);
        client.setPassword(TestCommon.password);
    }

    @After
    public void tearDown() throws Exception {
        if (client != null) {
            client.close();
            client = null;
        }
    }

    @Test(expected = TimeLimitExceededException.class)
    public void testConnectTimeout() throws Exception {
        SSHClient client = spy(this.client);
        SshClient ssh = spy(SshClient.setUpDefaultClient());
        ConnectFuture future = mock(ConnectFuture.class);

        doReturn(ssh).when(client).createSshClient();
        doReturn(future).when(ssh).connect(anyString(), anyInt());
        when(future.await(anyLong())).thenReturn(false);

        client.connect();
    }

    @Test(expected = TimeLimitExceededException.class)
    public void testPasswordTimeout() throws Exception {
        SSHClient client = spy(this.client);
        SshClient ssh = spy(SshClient.setUpDefaultClient());
        ConnectFuture future = mock(ConnectFuture.class);
        ClientSession session = mock(ClientSession.class);

        doReturn(ssh).when(client).createSshClient();
        doReturn(future).when(ssh).connect(anyString(), anyInt());
        when(future.await(anyLong())).thenReturn(true);
        when(future.getSession()).thenReturn(session);

        AuthFuture authFuture = mock(AuthFuture.class);
        when(authFuture.await(anyLong())).thenReturn(false);
        when(session.authPassword(anyString(), anyString())).thenReturn(authFuture);

        client.connect();
        client.authenticate();
    }
}
