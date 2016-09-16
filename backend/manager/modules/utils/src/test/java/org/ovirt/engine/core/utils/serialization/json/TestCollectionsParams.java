package org.ovirt.engine.core.utils.serialization.json;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TestCollectionsParams implements Serializable {
    private HashSet<String> concreteSet;
    private Set<String> nonConcreteSet;
    private Set<String> singletonSet;
    private Set<String> unmodifiableSet;
    private List<String> singletonList;
    private List<String> unmodifiableList;
    private List<String> arraysAsList;
    private HashMap<String, String> concreteMap;
    private Map<String, String> nonConcreteMap;
    private Map<String, String> singletonMap;
    private Map<String, String> unmodifiableMap;

    public HashSet<String> getConcreteSet() {
        return concreteSet;
    }

    public void setConcreteSet(HashSet<String> concreteSet) {
        this.concreteSet = concreteSet;
    }

    public Set<String> getNonConcreteSet() {
        return nonConcreteSet;
    }

    public void setNonConcreteSet(Set<String> nonConcreteSet) {
        this.nonConcreteSet = nonConcreteSet;
    }

    public HashMap<String, String> getConcreteMap() {
        return concreteMap;
    }

    public void setConcreteMap(HashMap<String, String> concreteMap) {
        this.concreteMap = concreteMap;
    }

    public Map<String, String> getNonConcreteMap() {
        return nonConcreteMap;
    }

    public void setNonConcreteMap(Map<String, String> nonConcreteMap) {
        this.nonConcreteMap = nonConcreteMap;
    }

    public Set<String> getSingletonSet() {
        return singletonSet;
    }

    public void setSingletonSet(Set<String> singletonSet) {
        this.singletonSet = singletonSet;
    }

    public List<String> getSingletonList() {
        return singletonList;
    }

    public void setSingletonList(List<String> singletonList) {
        this.singletonList = singletonList;
    }

    public Map<String, String> getSingletonMap() {
        return singletonMap;
    }

    public void setSingletonMap(Map<String, String> singletonMap) {
        this.singletonMap = singletonMap;
    }

    public Set<String> getUnmodifiableSet() {
        return unmodifiableSet;
    }

    public void setUnmodifiableSet(Set<String> unmodifiableSet) {
        this.unmodifiableSet = unmodifiableSet;
    }

    public List<String> getUnmodifiableList() {
        return unmodifiableList;
    }

    public void setUnmodifiableList(List<String> unmodifiableList) {
        this.unmodifiableList = unmodifiableList;
    }

    public Map<String, String> getUnmodifiableMap() {
        return unmodifiableMap;
    }

    public void setUnmodifiableMap(Map<String, String> unmodifiableMap) {
        this.unmodifiableMap = unmodifiableMap;
    }

    public List<String> getArraysAsList() {
        return arraysAsList;
    }

    public void setArraysAsList(List<String> arraysAsList) {
        this.arraysAsList = arraysAsList;
    }
}
