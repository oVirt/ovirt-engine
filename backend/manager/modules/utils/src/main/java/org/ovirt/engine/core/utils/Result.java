package org.ovirt.engine.core.utils;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

public final class Result<ERR, VAL> {
    private final Optional<ERR> error;
    private final Optional<VAL> value;

    private Result(Optional<ERR> error, Optional<VAL> value) {
        this.error = error;
        this.value = value;
    }

    public static <ERR, VAL> Result<ERR, VAL> error(ERR error) {
        return new Result<>(Optional.of(error), Optional.empty());
    }

    public static <ERR, VAL> Result<ERR, VAL> value(VAL value) {
        return new Result<>(Optional.empty(), Optional.of(value));
    }

    public <T> Result<T, VAL> mapError(Function<? super ERR, ? extends T> fnErr) {
        return map(fnErr, Function.identity());
    }

    public <NEW_ERR, NEW_VAL> Result<NEW_ERR, NEW_VAL> map(
            Function<? super ERR, ? extends NEW_ERR> fnErr,
            Function<? super VAL, ? extends NEW_VAL> fnValue) {
        return new Result<>(error.map(fnErr), value.map(fnValue));
    }

    public Optional<VAL> orError(Consumer<ERR> handleErr) {
        return mapError(err -> {
            error.ifPresent(handleErr);
            return Void.TYPE;
        }).value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Result<?, ?> result = (Result<?, ?>) o;
        return Objects.equals(error, result.error) &&
                Objects.equals(value, result.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(error, value);
    }

    @Override
    public String toString() {
        return "Result{" +
                "error=" + error +
                ", value=" + value +
                '}';
    }
}
