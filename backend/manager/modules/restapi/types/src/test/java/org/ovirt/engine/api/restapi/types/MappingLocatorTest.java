package org.ovirt.engine.api.restapi.types;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.ovirt.engine.api.restapi.test.mappers.api.IBar;
import org.ovirt.engine.api.restapi.test.mappers.api.IFoo;
import org.ovirt.engine.api.restapi.test.mappers.impl.BarImpl;
import org.ovirt.engine.api.restapi.test.mappers.impl.FooImpl;

public class MappingLocatorTest extends Assert {

    private static final String PACKAGE_NAME = "org.ovirt.engine.api.restapi.test.mapping";
    private MappingLocator mappingLocator;

    @Before
    public void setUp() {
        mappingLocator = new MappingLocator(PACKAGE_NAME);
        mappingLocator.populate();
    }

    @Test
    public void testStaticFooToBarInterfaceMapper() {
        Mapper<IFoo, IBar> mapper = mappingLocator.getMapper(IFoo.class, IBar.class);
        assertNotNull(mapper);
        IBar bar = mapper.map(new FooImpl("foo"), null);
        assertEquals("foo", bar.get());
    }

    @Test
    public void testStaticBarToFooInterfaceMapper() {
        Mapper<IBar, IFoo> mapper = mappingLocator.getMapper(IBar.class, IFoo.class);
        assertNotNull(mapper);
        IFoo foo = mapper.map(new BarImpl("bar"), null);
        assertEquals("bar", foo.get());
    }

    @Test
    public void testStaticFooToBarClassMapper() {
        Mapper<FooImpl, BarImpl> mapper = mappingLocator.getMapper(FooImpl.class, BarImpl.class);
        assertNotNull(mapper);
        IBar bar = mapper.map(new FooImpl("foo"), null);
        assertEquals("foo", bar.get());
    }

    @Test
    public void testStaticBarToFooClassMapper() {
        Mapper<BarImpl, FooImpl> mapper = mappingLocator.getMapper(BarImpl.class, FooImpl.class);
        assertNotNull(mapper);
        IFoo foo = mapper.map(new BarImpl("bar"), null);
        assertEquals("bar", foo.get());
    }

    @Test
    public void testStaticTemplatedFooToBarClassMapper() {
        Mapper<FooImpl, BarImpl> mapper = mappingLocator.getMapper(FooImpl.class, BarImpl.class);
        assertNotNull(mapper);
        BarImpl bar = mapper.map(new FooImpl("foo"), new BarImpl("overwrite", "keep"));
        assertEquals("foo", bar.get());
        assertEquals("keep", bar.other());
    }

    @Test
    public void testStaticTemplatedBarToFooClassMapper() {
        Mapper<BarImpl, FooImpl> mapper = mappingLocator.getMapper(BarImpl.class, FooImpl.class);
        assertNotNull(mapper);
        FooImpl foo = mapper.map(new BarImpl("bar"), new FooImpl("overwrite", "keep"));
        assertEquals("bar", foo.get());
        assertEquals("keep", foo.other());
    }

    @Test
    public void testSelfMapper() {
        Mapper<IFoo, IFoo> mapper = mappingLocator.getMapper(IFoo.class, IFoo.class);
        assertNull(mapper);
    }
}
