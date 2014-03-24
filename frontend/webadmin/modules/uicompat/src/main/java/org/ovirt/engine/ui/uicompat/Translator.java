package org.ovirt.engine.ui.uicompat;


public class Translator<T> {

    public String get(T key) {
        if(key == null) {
            return null;
        }
        return key.toString();
    }

    public boolean containsKey(T key){
        return get(key)!=null;
    }
}
