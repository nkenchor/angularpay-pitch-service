package io.angularpay.pitch.domain.commands;

public interface ResourceReferenceCommand<T, R> {

    R map(T referenceResponse);
}
