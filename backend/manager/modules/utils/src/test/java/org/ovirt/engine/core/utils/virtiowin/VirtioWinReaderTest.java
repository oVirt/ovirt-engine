package org.ovirt.engine.core.utils.virtiowin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.osinfo.OsRepository;
import org.ovirt.engine.core.common.utils.SimpleDependencyInjector;
import org.ovirt.engine.core.utils.MockConfigDescriptor;
import org.ovirt.engine.core.utils.MockConfigExtension;

@ExtendWith({MockitoExtension.class, MockConfigExtension.class})
@MockitoSettings(strictness = Strictness.LENIENT)
public class VirtioWinReaderTest {

    @Mock
    private OsRepository osRepository;

    private VirtioWinReader vwReader;

    public static Stream<MockConfigDescriptor<?>> mockConfiguration() throws URISyntaxException {
        return Stream.of(
                MockConfigDescriptor.of(ConfigValues.VirtioWinIsoPath, VirtioWinReader.class.getResource("/virtiowin").toURI().getPath())
        );
    }

    @BeforeEach
    public void init() {
        ArrayList<Integer> os64 = new ArrayList<>(1);
        os64.add(1);
        when(osRepository.get64bitOss()).thenReturn(os64);
        SimpleDependencyInjector.getInstance().bind(OsRepository.class, osRepository);

        vwReader = new VirtioWinReader();
        vwReader.load();
    }

    @Test
    public void testDriversForOsId64Bit() {
        assertEquals("101.1.0", vwReader.getAgentVersionByOsName(1));
    }

    @Test
    public void testDriversForOsId32Bit() {
        assertEquals("101.1.0", vwReader.getAgentVersionByOsName(2));
    }

    @Test
    public void testVirtioWinIsoName() {
        assertEquals("virtio-win-0.1.185.iso", vwReader.getVirtioIsoName());
    }

}
