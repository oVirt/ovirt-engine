package org.ovirt.engine.ui.uicompat;


public class Translator {

    public String get(Object key) {
        if(key == null) {
            return null;
        }
        return key.toString();
    }

    public boolean containsKey(Object key){
        return get(key)!=null;
    }
}
