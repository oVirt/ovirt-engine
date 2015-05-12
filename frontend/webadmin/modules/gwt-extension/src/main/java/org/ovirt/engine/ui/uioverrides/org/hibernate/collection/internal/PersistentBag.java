package org.hibernate.collection.internal;

import java.util.ArrayList;

/**
 * This is how we tell GWT to serialize the hibernate class PersistentBag.
 * We don't need the real implementation in the UI, and since we only use
 * the List interface, the transformation to ArrayList is valid.
 */
public class PersistentBag extends ArrayList {

}
