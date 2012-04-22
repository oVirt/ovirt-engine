package org.ovirt.engine.ui.uicompat;

import com.google.gwt.core.client.GWT;

public class EnumTranslator<T> extends Translator {
    private Enums enums = GWT.create(Enums.class);
    private T type;
    
    public EnumTranslator(T type) {
        this.type = type;
    }
    
	public static <T> Translator Create(T type) {
	    return new EnumTranslator(type);
	}
	
	@Override
	public String get(Object key) {
	    //FIXME: hack: due to java restriction for method names with chars that are not letters, digits, and underscores, replace . with 0
	    if(key == null){
	        return null;
	    }
	    String enumName = type.toString();
	    enumName = enumName.substring(enumName.lastIndexOf(".")+1,enumName.length()); //$NON-NLS-1$
	    String trasnlatedEnum = enums.getString(enumName + "___" + key.toString()); //$NON-NLS-1$

	    return trasnlatedEnum;
	}
}