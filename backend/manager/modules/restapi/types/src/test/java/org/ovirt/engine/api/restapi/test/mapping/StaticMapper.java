package org.ovirt.engine.api.restapi.test.mapping;

import org.ovirt.engine.api.restapi.test.mappers.api.IBar;
import org.ovirt.engine.api.restapi.test.mappers.api.IFoo;
import org.ovirt.engine.api.restapi.test.mappers.impl.BarImpl;
import org.ovirt.engine.api.restapi.test.mappers.impl.FooImpl;
import org.ovirt.engine.api.restapi.types.Mapping;

public class StaticMapper {

    @Mapping(from = IFoo.class, to = IBar.class)
    public static IBar mapFooToBarInterfaces(IFoo foo, IBar template) {
        IBar bar = template != null ? template : new BarImpl();
        bar.set(foo.get());
        return bar;
    }

    @Mapping(from = IBar.class, to = IFoo.class)
    public static IFoo mapBarToFooInterfaces(IBar bar, IFoo template) {
        IFoo foo = template != null ? template : new FooImpl();
        foo.set(bar.get());
        return foo;
    }

    @Mapping(from = FooImpl.class, to = BarImpl.class)
    public static BarImpl mapFooToBarClasses(IFoo foo, BarImpl template) {
        BarImpl bar = template != null ? template : new BarImpl();
        bar.set(foo.get());
        return bar;
    }

    @Mapping(from = BarImpl.class, to = FooImpl.class)
    public static FooImpl mapBarToFooClasses(IBar bar, FooImpl template) {
        FooImpl foo = template != null ? template : new FooImpl();
        foo.set(bar.get());
        return foo;
    }

}
