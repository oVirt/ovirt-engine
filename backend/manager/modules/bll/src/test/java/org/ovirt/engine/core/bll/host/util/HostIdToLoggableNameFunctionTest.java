package org.ovirt.engine.core.bll.host.util;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.common.businessentities.VdsStatic;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.VdsStaticDao;

@RunWith(MockitoJUnitRunner.class)
public class HostIdToLoggableNameFunctionTest {

    private static final String HOST_NAME = "host name";
    private static final Guid hostId = Guid.newGuid();

    @Mock
    private VdsStaticDao vdsStaticDao;

    @InjectMocks
    private HostIdToLoggableNameFunction underTest;

    private VdsStatic host;

    @Before
    public void setUp() {
        host = createHost();
        Mockito.when(vdsStaticDao.get(hostId)).thenReturn(host);
    }

    @Test
    public void testApply() {
        final String actual = underTest.eval(hostId);

        assertThat(actual, is(HOST_NAME));
    }

    @Test
    public void testApplyHostNotFound() {
        Mockito.when(vdsStaticDao.get(hostId)).thenReturn(null);

        final String actual = underTest.eval(hostId);

        assertThat(actual, is(hostId.toString()));
    }

    @Test
    public void testApplyNullInput() {
        final String actual = underTest.eval(null);

        assertThat(actual, is("null"));
    }

    @Test
    public void testApplyNullName() {
        host.setName(null);

        final String actual = underTest.eval(hostId);

        assertThat(actual, is(hostId.toString()));
    }

    private VdsStatic createHost() {
        final VdsStatic host = new VdsStatic();
        host.setName(HOST_NAME);
        return host;
    }

}
