package org.ovirt.engine.core.utils;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.apache.commons.collections.IteratorUtils;

/**
 * This class is similar to Stream, but all its operations are lazily evaluated.
 * Mainly the distinct() operation, which is strict for Stream.
 */
public class Pipeline<T> {
    private Iterator<T> iterator;

    private Pipeline(Iterator<T> iterator) {
        this.iterator = iterator;
    }

    public Iterator<T> iterator() {
        return iterator;
    }

    @SuppressWarnings("unchecked")
    public Pipeline<T> append(Iterator<T> iter) {
        iterator = IteratorUtils.chainedIterator(iterator, iter);
        return this;
    }

    public Pipeline<T> append(Supplier<Iterator<T>> supplier) {
        return append(new LazyIterator<>(supplier));
    }

    @SuppressWarnings("unchecked")
    public Pipeline<T> filter(Predicate<T> predicate) {
        iterator = IteratorUtils.filteredIterator(iterator, item -> predicate.test((T)item));
        return this;
    }

    @SuppressWarnings("unchecked")
    public <U> Pipeline<U> map(Function<T, U> function) {
        Iterator<U> result = IteratorUtils.transformedIterator(iterator, item -> function.apply((T)item));
        return new Pipeline<>(result);
    }

    public Pipeline<T> execute(Consumer<T> consumer) {
        return this.map(item -> {
            consumer.accept(item);
            return item;
        });
    }

    public Pipeline<T> distinct() {
        Set<T> set = new HashSet<>();
        return this.filter(item -> !set.contains(item))
                .execute(set::add);
    }

    public static <U> Pipeline<U> create(Iterator<U> iterator) {
        return new Pipeline<>(iterator);
    }

    public static <U> Pipeline<U> create(Supplier<Iterator<U>> supplier) {
        return create(new LazyIterator<>(supplier));
    }

    private static class LazyIterator<U> implements Iterator<U> {
        private Supplier<Iterator<U>> supplier;

        public LazyIterator(Supplier<Iterator<U>> supplier) {
            this.supplier = new MemoizingSupplier<>(supplier);
        }

        @Override
        public boolean hasNext() {
            return supplier.get().hasNext();
        }

        @Override
        public U next() {
            return supplier.get().next();
        }
    }
}
