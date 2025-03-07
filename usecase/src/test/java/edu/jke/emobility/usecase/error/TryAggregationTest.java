package edu.jke.emobility.usecase.error;

import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;


public class TryAggregationTest {

    private record Request(String first, Integer second, UUID third) {
        static Request from(Try<String,?> first, Try<Integer,?> second, Try<UUID,?> third) {
            return new Request(first.get(), second.get(), third.get());
        }
    }

    @Test
    void combineSuccess() {
        Try<String, Throwable> first = Try.success("Hello");
        Try<Integer, Throwable> second = Try.success(42);
        Try<UUID, Throwable> third = Try.success(UUID.randomUUID());

        Try<Request, Collection<Throwable>> request = Try.aggregate(
                () -> Request.from(first, second, third),
                List::of,
                first, second, third);

        assertThat(request.isSuccess()).isTrue();
        assertThat(request.get().first).isEqualTo("Hello");
        assertThat(request.get().second).isEqualTo(42);
        assertThat(request.get().third.toString().length()).isEqualTo(36);
    }

    @Test
    void aggregateFailures() {
        Try<String, Throwable> first = Try.success("Hello");
        Try<Integer, Throwable> second = Try.success(42);
        Try<UUID, Throwable> third = Try.failable(() -> UUID.fromString("1"));

        Try<Request, Collection<Throwable>> request = Try.aggregate(
                () -> Request.from(first, second, third),
                List::of,
                first, second, third);

        assertThat(request.isFailure()).isTrue();
        request.onFailure(errors -> assertThat(errors.size()).isEqualTo(1));
    }

}
