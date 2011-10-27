package org.ovirt.engine.core.dal;

import java.lang.annotation.*;

// C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
// [AttributeUsage(AttributeTargets.Class, AllowMultiple = false, Inherited = true)]
// public class TransactiveAttribute extends Attribute
// {
//     private static LogCompat log = LogFactoryCompat.getLog(TransactiveAttribute.class);
// }

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface TransactiveAttribute {
}
