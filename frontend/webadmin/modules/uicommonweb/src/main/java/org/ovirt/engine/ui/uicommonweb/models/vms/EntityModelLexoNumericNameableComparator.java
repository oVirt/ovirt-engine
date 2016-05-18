package org.ovirt.engine.ui.uicommonweb.models.vms;

import java.util.Comparator;

import org.ovirt.engine.core.common.businessentities.Nameable;
import org.ovirt.engine.core.common.businessentities.comparators.LexoNumericNameableComparator;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;


/**
 * implementing an EntityModel comparator based on LexoNumericNameableComparator comparison.
 */
public class EntityModelLexoNumericNameableComparator<T extends EntityModel<E>, E extends Nameable> implements Comparator<T> {

    private final LexoNumericNameableComparator<E> comparator = new LexoNumericNameableComparator<>();

    @Override
    public int compare(T em1, T em2) {
        return comparator.compare(em1.getEntity(), em2.getEntity());
    }
}
