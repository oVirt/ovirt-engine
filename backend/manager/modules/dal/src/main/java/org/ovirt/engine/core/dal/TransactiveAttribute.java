package org.ovirt.engine.core.dal;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface TransactiveAttribute {
}
