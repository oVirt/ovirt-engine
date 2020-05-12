package org.ovirt.engine.core.utils.virtiowin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.io.File;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.ArrayList;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.core.common.osinfo.OsRepository;
import org.ovirt.engine.core.common.utils.SimpleDependencyInjector;

@MockitoSettings(strictness = Strictness.LENIENT)
public class VirtioWinReaderTest {

    @Mock
    private OsRepository osRepository;

    private VirtioWinReader vwReader;

    @BeforeEach
    public void init() throws URISyntaxException {
        ArrayList<Integer> os64 = new ArrayList<>(1);
        os64.add(1);
        when(osRepository.get64bitOss()).thenReturn(os64);
        SimpleDependencyInjector.getInstance().bind(OsRepository.class, osRepository);

        Path directoryPath = new File(VirtioWinReader.class.getResource("/virtiowin").toURI().getPath()).toPath();
        vwReader = new VirtioWinReader();
        vwReader.init(directoryPath);
    }

    @Test
    public void testDriversForOsId64Bit() {
        assertEquals("101.1.0", vwReader.getAgentVersionByOsName(1));
    }

    @Test
    public void testDriversForOsId32Bit() {
        assertEquals("101.1.0", vwReader.getAgentVersionByOsName(2));
    }

}
