package org.ovirt.engine.core.extensions.mgr;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ConfigurationTest {
    private File temporaryFolder;

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @BeforeEach
    public void createTemporaryFolder() throws IOException {
        temporaryFolder = File.createTempFile("configurationTest", "", null);
        temporaryFolder.delete();
        temporaryFolder.mkdir();
        temporaryFolder.deleteOnExit();
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @AfterEach
    public void removeTemporaryFolder() {
        temporaryFolder.delete();
    }

    /**
     * Check that retrieving a parameter works as expected.
     */
    @Test
    public void testGetString() throws Exception {
        File file = writeConf(
            "a=a"
        );
        Configuration config = Configuration.loadFile(file);
        assertNotNull(config);
        assertEquals("a", config.getString("a"));
    }

    /**
     * Check that retrieving a boolean parameter works as expected.
     */
    @Test
    public void testGetBoolean() throws Exception {
        File file = writeConf(
            "true=true",
            "TRUE=TRUE",
            "false=false",
            "FALSE=FALSE"
        );
        Configuration config = Configuration.loadFile(file);
        assertNotNull(config);
        assertTrue(config.getBoolean("true"));
        assertTrue(config.getBoolean("TRUE"));
        assertFalse(config.getBoolean("false"));
        assertFalse(config.getBoolean("FALSE"));
    }

    /**
     * Check that retrieving an integer parameter works as expected.
     */
    @Test
    public void testGetInteger() throws Exception {
        File file = writeConf(
            "0=0",
            "1=1",
            "m1=-1",
            "1234=1234",
            "m1234=-1234"
        );
        Configuration config = Configuration.loadFile(file);
        assertNotNull(config);
        assertEquals(Integer.valueOf(0), config.getInteger("0"));
        assertEquals(Integer.valueOf(1), config.getInteger("1"));
        assertEquals(Integer.valueOf(-1), config.getInteger("m1"));
        assertEquals(Integer.valueOf(1234), config.getInteger("1234"));
        assertEquals(Integer.valueOf(-1234), config.getInteger("m1234"));
    }

    /**
     * Check that retrieving an enum parameter works as expected.
     */
    @Test
    public void testGetEnum() throws Exception {
        File file = writeConf(
            "a=A"
        );
        Configuration config = Configuration.loadFile(file);
        assertNotNull(config);
        assertEquals(TestEnum.A, config.getEnum(TestEnum.class, "a"));
    }

    public enum TestEnum {
        A
    }

    /**
     * Check that retrieving a file works as expected.
     */
    @Test
    public void testGetFile() throws Exception {
        File file = writeConf(
                "file=file"
        );
        Configuration config = Configuration.loadFile(file);
        assertNotNull(config);
        assertEquals(new File("file"), config.getFile("file"));
    }

    /**
     * Check that retrieving a parameter from a prefix view works as expected.
     */
    @Test
    public void testPrefixGet() throws Exception {
        File file = writeConf(
            "a.b=c"
        );
        Configuration config = Configuration.loadFile(file);
        assertNotNull(config);
        Configuration view = config.getView("a");
        assertNotNull(view);
        assertEquals("c", view.getString("b"));
    }

    /**
     * Check that a prefix view isn't null even if it doesn't contain any parameter.
     */
    @Test
    public void testPrefixViewNotNullEvenIfEmpty() throws Exception {
        File file = writeConf();
        Configuration config = Configuration.loadFile(file);
        assertNotNull(config);
        Configuration view = config.getView("a");
        assertNotNull(view);
    }

    /**
     * Check that root of the configuration doesn't have a parent.
     */
    @Test
    public void testRootHasNoParent() throws Exception {
        File file = writeConf();
        Configuration config = Configuration.loadFile(file);
        assertNotNull(config);
        assertNull(config.getParent());
    }

    /**
     * Check that prefix view has a parent.
     */
    @Test
    public void testPrefixViewHasParent() throws Exception {
        File file = writeConf();
        Configuration config = Configuration.loadFile(file);
        assertNotNull(config);
        Configuration view = config.getView("a");
        assertNotNull(view);
        assertNotNull(view.getParent());
    }

    /**
     * Check that typed views support strings.
     */
    @Test
    public void testTypedString() throws Exception {
        File file = writeConf(
            "a=b"
        );
        Configuration config = Configuration.loadFile(file);
        assertNotNull(config);
        TypedString view = config.getView(TypedString.class);
        assertNotNull(view);
        assertEquals("b", view.getA());
    }

    public interface TypedString {
        String getA();
    }

    /**
     * Check that typed views support primitive boolean parameters.
     */
    @Test
    public void testTypedPrimitiveBoolean() throws Exception {
        File file = writeConf(
            "a=true",
            "b=false"
        );
        Configuration config = Configuration.loadFile(file);
        assertNotNull(config);
        TestTypedPrimitiveBoolean view = config.getView(TestTypedPrimitiveBoolean.class);
        assertNotNull(view);
        assertTrue(view.isA());
        assertFalse(view.isB());
    }

    public interface TestTypedPrimitiveBoolean {
        boolean isA();
        boolean isB();
    }

    /**
     * Check that typed views support boxed boolean parameters.
     */
    @Test
    public void testTypedBoxedBoolean() throws Exception {
        File file = writeConf(
            "a=true",
            "b=false"
        );
        Configuration config = Configuration.loadFile(file);
        assertNotNull(config);
        TestTypedBoxedBoolean view = config.getView(TestTypedBoxedBoolean.class);
        assertNotNull(view);
        assertEquals(Boolean.TRUE, view.isA());
        assertEquals(Boolean.FALSE, view.isB());
    }

    public interface TestTypedBoxedBoolean {
        Boolean isA();
        Boolean isB();
    }

    /**
     * Check that typed views support primitive integer parameters, including negative values.
     */
    @Test
    public void testTypedPrimitiveInteger() throws Exception {
        File file = writeConf(
            "0=0",
            "1=1",
            "m1=-1",
            "1234=1234",
            "m1234=-1234"
        );
        Configuration config = Configuration.loadFile(file);
        assertNotNull(config);
        TestTypedPrimitiveInteger view = config.getView(TestTypedPrimitiveInteger.class);
        assertNotNull(view);
        assertEquals(0, view.get0());
        assertEquals(1, view.get1());
        assertEquals(-1, view.getM1());
        assertEquals(1234, view.get1234());
        assertEquals(-1234, view.getM1234());
    }

    public interface TestTypedPrimitiveInteger {
        int get0();
        int get1();
        int getM1();
        int get1234();
        int getM1234();
    }

    /**
     * Check that typed views support boxed integer parameters, including negative values.
     */
    @Test
    public void testTypedBoxedInteger() throws Exception {
        File file = writeConf(
            "0=0",
            "1=1",
            "m1=-1",
            "1234=1234",
            "m1234=-1234"
        );
        Configuration config = Configuration.loadFile(file);
        assertNotNull(config);
        TestTypedBoxedInteger view = config.getView(TestTypedBoxedInteger.class);
        assertNotNull(view);
        assertEquals(Integer.valueOf(0), view.get0());
        assertEquals(Integer.valueOf(1), view.get1());
        assertEquals(Integer.valueOf(-1), view.getM1());
        assertEquals(Integer.valueOf(1234), view.get1234());
        assertEquals(Integer.valueOf(-1234), view.getM1234());
    }

    public interface TestTypedBoxedInteger {
        Integer get0();
        Integer get1();
        Integer getM1();
        Integer get1234();
        Integer getM1234();
    }

    /**
     * Check that typed views support enum parameters.
     */
    @Test
    public void testTypedEnum() throws Exception {
        File file = writeConf(
            "a=A"
        );
        Configuration config = Configuration.loadFile(file);
        assertNotNull(config);
        TestTypedEnum view = config.getView(TestTypedEnum.class);
        assertNotNull(view);
        assertEquals(TestEnum.A, view.getA());
    }

    public interface TestTypedEnum {
        TestEnum getA();
    }

    /**
     * Check that typed views supports arrays of strings.
     */
    @Test
    public void testTypedArray() throws Exception {
        File file = writeConf(
            "a=b,c"
        );
        Configuration config = Configuration.loadFile(file);
        assertNotNull(config);
        TypedArray view = config.getView(TypedArray.class);
        assertNotNull(view);
        String[] array = view.getA();
        assertNotNull(array);
        assertEquals("b", array[0]);
        assertEquals("c", array[1]);
    }

    public interface TypedArray {
        String[] getA();
    }

    /**
     * Check that typed views supports lists of strings.
     */
    @Test
    public void testTypedList() throws Exception {
        File file = writeConf(
            "a=b,c"
        );
        Configuration config = Configuration.loadFile(file);
        assertNotNull(config);
        TypedList view = config.getView(TypedList.class);
        assertNotNull(view);
        List<String> list = view.getA();
        assertNotNull(list);
        assertEquals("b", list.get(0));
        assertEquals("c", list.get(1));
    }

    public interface TypedList {
        List<String> getA();
    }

    /**
     * Check that typed views supports files.
     */
    @Test
    public void testTypedFile() throws Exception {
        File file = writeConf(
                "a=b"
        );
        Configuration config = Configuration.loadFile(file);
        assertNotNull(config);
        TypedFile view = config.getView(TypedFile.class);
        assertNotNull(view);
        assertEquals(new File("b"), view.getA());
    }

    public interface TypedFile {
        File getA();
    }

    /**
     * Check that nested typed views are supported.
     */
    @Test
    public void testTypedNested() throws Exception {
        File file = writeConf(
            "a.b=c"
        );
        Configuration config = Configuration.loadFile(file);
        assertNotNull(config);
        TypedNested view = config.getView(TypedNested.class);
        assertNotNull(view);
        assertEquals("c", view.getA().getB());
    }

    public interface TypedNested {
        String getB();
        TypedNested getA();
    }

    /**
     * Check that the absolute key of a parameter inside a view is constructed correctly.
     */
    @Test
    public void testGetAbsoluteKey() throws Exception {
        File file = writeConf(
            "a.b=c"
        );
        Configuration config = Configuration.loadFile(file);
        assertNotNull(config);
        Configuration view = config.getView("a");
        assertNotNull(view);
        assertEquals("a.b", view.getAbsoluteKey("b"));
    }

    /**
     * Check that the prefix views are cached, so if the same view is requested twice exactly the same object is
     * returned.
     */
    @Test
    public void testPrefixViewCache() throws Exception {
        File file = writeConf();
        Configuration config = Configuration.loadFile(file);
        assertNotNull(config);
        Configuration view1 = config.getView("a");
        assertNotNull(view1);
        Configuration view2 = config.getView("a");
        assertNotNull(view2);
        assertSame(view1, view2);
    }

    /**
     * Check that the typed views are cached, so if the same view is requested twice exactly the same object is
     * returned.
     */
    @Test
    public void testTypedViewCache() throws Exception {
        File file = writeConf();
        Configuration config = Configuration.loadFile(file);
        assertNotNull(config);
        TypedString view1 = config.getView(TypedString.class);
        assertNotNull(view1);
        TypedString view2 = config.getView(TypedString.class);
        assertNotNull(view2);
        assertSame(view1, view2);
    }

    /**
     * This is a helper method that generates a configuration file. Each string passed as argument is a line that
     * will be written to the file.
     *
     * @return the reference to the generated file
     * @throws IOException if something fails while writing the file
     */
    public File writeConf(String... lines) throws IOException {
        File file = new File(temporaryFolder, "tmp.conf");
        try (PrintWriter writer = new PrintWriter(file, "UTF-8")) {
            for (String line : lines) {
                writer.println(line);
            }
        }
        return file;
    }
}
