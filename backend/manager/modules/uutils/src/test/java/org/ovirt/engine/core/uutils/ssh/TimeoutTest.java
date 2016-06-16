package org.ovirt.engine.core.uutils.ssh;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import javax.naming.TimeLimitExceededException;

import org.apache.sshd.client.SshClient;
import org.apache.sshd.client.future.AuthFuture;
import org.apache.sshd.client.future.ConnectFuture;
import org.apache.sshd.client.session.ClientSession;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class TimeoutTest extends TestCommon {

    SSHClient client;

    @BeforeEach
    public void setUp() {
        client = new SSHClient();
        client.setSoftTimeout(10 * 1000);
        client.setHardTimeout(60 * 1000);
        client.setHost(TestCommon.host, TestCommon.port);
        client.setUser(TestCommon.user);
        client.setPassword(TestCommon.password);
    }

    @AfterEach
    public void tearDown() throws Exception {
        if (client != null) {
            client.close();
            client = null;
        }
    }

    @Test
    public void testConnectTimeout() throws Exception {
        SSHClient client = spy(this.client);
        SshClient ssh = spy(SshClient.setUpDefaultClient());
        ConnectFuture future = mock(ConnectFuture.class);

        doReturn(ssh).when(client).createSshClient();
        doReturn(future).when(ssh).connect(any(), any(), anyInt());
        when(future.await(anyLong())).thenReturn(false);

        assertThrows(TimeLimitExceededException.class, client::connect);
    }

    @Test
    public void testPasswordTimeout() throws Exception {
        SSHClient client = spy(this.client);
        SshClient ssh = spy(SshClient.setUpDefaultClient());
        ConnectFuture future = mock(ConnectFuture.class);
        ClientSession session = mock(ClientSession.class);

        doReturn(ssh).when(client).createSshClient();
        doReturn(future).when(ssh).connect(any(), any(), anyInt());
        when(future.await(anyLong())).thenReturn(true);
        when(future.getSession()).thenReturn(session);

        AuthFuture authFuture = mock(AuthFuture.class);
        when(authFuture.await(anyLong())).thenReturn(false);
        when(session.auth()).thenReturn(authFuture);

        assertThrows(TimeLimitExceededException.class, () -> {
            client.connect();
            client.authenticate();
        });
    }
}
