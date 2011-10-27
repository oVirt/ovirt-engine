package org.ovirt.engine.ui.uicommonweb;
import java.util.Collections;
import org.ovirt.engine.core.compat.*;
import org.ovirt.engine.ui.uicompat.*;
import org.ovirt.engine.core.common.businessentities.*;
import org.ovirt.engine.core.common.vdscommands.*;
import org.ovirt.engine.core.common.queries.*;
import org.ovirt.engine.core.common.action.*;
import org.ovirt.engine.ui.frontend.*;
import org.ovirt.engine.ui.uicommonweb.*;
import org.ovirt.engine.ui.uicommonweb.models.*;
import org.ovirt.engine.core.common.*;

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