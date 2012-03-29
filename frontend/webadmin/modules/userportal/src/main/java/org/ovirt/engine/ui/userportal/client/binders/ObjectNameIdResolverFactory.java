package org.ovirt.engine.ui.userportal.client.binders;

import java.util.Map.Entry;
import java.util.MissingResourceException;

import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.network;
import org.ovirt.engine.core.common.businessentities.storage_domains;
import org.ovirt.engine.core.common.businessentities.storage_pool;
import org.ovirt.engine.ui.uicompat.EnumTranslator;
import org.ovirt.engine.ui.uicompat.Translator;

public class ObjectNameIdResolverFactory {
	public static ObjectNameIdResolver getResolver(final Class<?> type) {
		
		if (type.isEnum()) {
			return new ObjectNameIdResolver() {
				@Override
				public String getItemName(Object o) {
					Translator translator = EnumTranslator.Create(type);
					String s;
					try {
						s = translator.get(o);
					}
					catch (MissingResourceException e) {
						s = ((Enum)o).name();
					}
					return s;
				}
				@Override
				public String getItemId(Object o) {
					return ((Enum)(o)).name();
				}
			};
		} 
		else if (type.equals(storage_pool.class)) {
			return new ObjectNameIdResolver() {
				public String getItemName(Object o) {
					return ((storage_pool)o).getname();
				}
				public String getItemId(Object o) {
					return ((storage_pool)o).getId().toString();
				}
			};		
		}
		else if (type.equals(VDSGroup.class)) {
			return new ObjectNameIdResolver() {
				public String getItemId(Object o) {
					if (o == null)
						return null;
					return ((VDSGroup)o).getId().toString();
				}
				public String getItemName(Object o) {
					if (o == null)
						return null;
					return ((VDSGroup)o).getname();
				}
			};
		}
		else if (type.equals(VmTemplate.class)) {
			return new ObjectNameIdResolver() {
				public String getItemId(Object o) {
					if (o == null)
						return null;
					return ((VmTemplate)o).getId().toString();
				}
				public String getItemName(Object o) {
					if (o == null)
						return null;
					return ((VmTemplate)o).getname();
				}
			};	
		}
		else if (type.equals(Entry.class)) {
			// Will work ONLY with Entry<String,String> which is what the model works with, in any case of a different generics, the cast will throw an exception in runtime
			return new ObjectNameIdResolver() {
				public String getItemId(Object o) {
					if (o == null)
						return null;
					return ((Entry<String,String>)o).getKey();
				}
				public String getItemName(Object o) {
					return ((Entry<String,String>)o).getValue();
				}
			};	
		}
		else if (type.equals(Integer.class)) {
			return new ObjectNameIdResolver() {
				public String getItemId(Object o) {
					if (o == null)
						return null;
					return ((Integer)o).toString();
				}
				public String getItemName(Object o) {
					if (o == null)
						return null;
					return ((Integer)o).toString();
				}
			};	
		}
		else if (type.equals(VDS.class)) {
			return new ObjectNameIdResolver() {
				@Override
				public String getItemId(Object o) {
					if (o == null)
						return null;
					return ((VDS)o).getId().toString();
				}
				
				@Override
				public String getItemName(Object o) {
					if (o == null)
						return null;
					return ((VDS)o).getvds_name();
				}
			};
		}
		else if (type.equals(storage_domains.class)) {
			return new ObjectNameIdResolver() {
				public String getItemId(Object o) {
					if (o == null) 
						return null;
					return ((storage_domains)o).getId().toString();
				}
				public String getItemName(Object o) {
					if (o == null) 
						return null;
					return ((storage_domains)o).getstorage_name();
				}
			};	
		}
		else if (type.equals(String.class)) {
			return new ObjectNameIdResolver() {
				public String getItemId(Object o) {
					return (String)o;
				}
				public String getItemName(Object o) {
					return (String)o;
				}
			};	
		}
		else if (type.equals(network.class)) {
			return new ObjectNameIdResolver() {
				public String getItemName(Object o) {
					return (((network)o).getname());
				}
				public String getItemId(Object o) {
					return (((network)o).getId().toString());
				}
			};
		}
		else {
			throw new RuntimeException("Could not find an Object name/id resolver for the class type " + type.getClass().getName());
		}
	}
}
