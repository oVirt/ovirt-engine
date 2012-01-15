package org.ovirt.engine.ui.uicommonweb;

@SuppressWarnings("unused")
public class TypeResolver
{
    private static TypeResolver privateInstance;

    public static TypeResolver getInstance()
    {
        return privateInstance;
    }

    private static void setInstance(TypeResolver value)
    {
        privateInstance = value;
    }

    private ITypeResolver implementation;

    private TypeResolver(ITypeResolver implementation)
    {
        this.implementation = implementation;
    }

    public static void Initialize(ITypeResolver implementation)
    {
        setInstance(new TypeResolver(implementation));
    }

    public Object Resolve(java.lang.Class type)
    {
        return implementation.Resolve(type);
    }
}
