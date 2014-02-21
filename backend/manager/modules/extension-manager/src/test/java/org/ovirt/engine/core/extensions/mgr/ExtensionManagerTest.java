package org.ovirt.engine.core.extensions.mgr;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.ovirt.engine.core.extensions.mgr.ExtensionManager.ExtensionEntry;
import org.ovirt.engine.core.utils.EngineLocalConfig;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;


//Currently ignored as jboss modules are not used in the test, need to revisit.
@Ignore
public class ExtensionManagerTest {

    @Rule
    public TemporaryFolder tmp = new TemporaryFolder();
    private ExtensionManager loader;
    private List<File> extensionsDirecotries;


    private class TestEngineLocalConfig extends EngineLocalConfig {

        public TestEngineLocalConfig() {
            super(Collections.<String, String> emptyMap());
        }

        public List<File> getExtensionsDirectories() {
            return extensionsDirecotries;
        }
    }

    public File createConfigurationFile(String fileName,
            String extensionName,
            String module,
            String provides,
            boolean enabled) throws FileNotFoundException {

        Random r = new Random();
        File f = new File(extensionsDirecotries.get(Math.abs(r.nextInt() % extensionsDirecotries.size())), fileName);
        PrintWriter writer = new PrintWriter(f);
        writer.println("ovirt.engine.extension.name = " + extensionName);
        writer.println("ovirt.engine.extension.module = " + module);
        writer.println("ovirt.engine.extension.provides = " + provides);
        writer.println("ovirt.engine.extension.enabled = " + enabled);
        writer.flush();
        writer.close();
        return f;

    }

    @Before
    public void setup() {
        extensionsDirecotries = new ArrayList<>();
        extensionsDirecotries.add(tmp.newFolder("etc", "ovirt-engine", "extension.d"));
        extensionsDirecotries.add(tmp.newFolder("usr", "share", "ovirt-engine", "extension.d"));
    }

    @Test
    public void testEmptyFolders() {
        ExtensionManager loader = new ExtensionManager();
        loader.load(new TestEngineLocalConfig());
        List<ExtensionEntry> extensionsByService = loader.getProvidedExtensions("myservice");
        assertNotNull(extensionsByService);
        assertEquals(0, extensionsByService.size());
    }

    @Test
    public void testActivatedConfig() throws FileNotFoundException {
        ExtensionManager loader = new ExtensionManager();
        createConfigurationFile("my-ipa.properties", "myipa", "", "authn", true);
        loader.load(new TestEngineLocalConfig());
        List<ExtensionEntry> providedExtensions = loader.getProvidedExtensions("authn");
        assertNotNull(providedExtensions);
        assertEquals(1, providedExtensions.size());
        ExtensionEntry extension = providedExtensions.get(0);
        assertEquals("myipa", extension.getName());
        assertEquals(true, extension.isEnabled());
        assertEquals("authn", extension.getProvides());
    }

    @Test
    public void testDeactivatedConfig() throws FileNotFoundException {
        ExtensionManager loader = new ExtensionManager();
        createConfigurationFile("my-ipa.properties", "myipa", "", "authn", false);
        loader.load(new TestEngineLocalConfig());
        List<ExtensionEntry> extensionsByService = loader.getProvidedExtensions("authn");
        assertNotNull(extensionsByService);
        assertEquals(0, extensionsByService.size());
    }


}
