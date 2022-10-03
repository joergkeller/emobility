package edu.jke.emobility.usecase.error;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.stream.StreamSupport;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class TryTest {

    @Nested
    class Successes {

        @Test
        void createSuccess() {
            Try<String,Throwable> success = Try.success("done");
            assertThat(success.isSuccess(), equalTo(true));
            assertThat(success.isFailure(), equalTo(false));
            success
                    .onSuccess(val -> assertThat(val, equalTo("done")))
                    .onFailure(th -> Assertions.fail()); // don't execute
            assertThat(success.get(), equalTo("done"));
            assertThat(success.orElse("default"), equalTo("done"));
        }

        @Test
        void createSuccessWithErrorMapper() {
            Try<String,String> success = Try.success("done", Throwable::getMessage);
            assertThat(success.isSuccess(), equalTo(true));
            assertThat(success.isFailure(), equalTo(false));
            success
                    .onSuccess(val -> assertThat(val, equalTo("done")))
                    .onFailure(th -> Assertions.fail()); // don't execute
        }

        @Test
        void successfulOperation() {
            Try<String,Throwable> success = Try.failable(() -> "finally done");
            assertThat(success.isSuccess(), equalTo(true));
            success.onSuccess(val -> assertThat(val, equalTo("finally done")));
        }

        @Test
        void successfulOperationWithErrorMapper() {
            Try<String,String> success = Try.failable(() -> "finally done", Throwable::getMessage);
            assertThat(success.isSuccess(), equalTo(true));
            success.onSuccess(val -> assertThat(val, equalTo("finally done")));
        }

        @Test
        void mappingSuccess() {
            Try<String,String> origin = Try.success("42", Throwable::getMessage);
            Try<Integer,String> result = origin.map(Integer::parseInt);
            assertThat(result.isSuccess(), equalTo(true));
            result.onSuccess(val -> assertThat(val, equalTo(42)));
        }

        private Try<Integer,String> parsing(String value) {
            return Try.failable(() -> Integer.parseInt(value), Throwable::getMessage);
        }

        @Test
        void flatMappingSuccess() {
            Try<String,String> origin = Try.success("42", Throwable::getMessage);
            Try<Integer,String> result = origin.flatMap(this::parsing);
            assertThat(result.isSuccess(), equalTo(true));
            result.onSuccess(val -> assertThat(val, equalTo(42)));
        }

    }

    @Nested
    class Failures {

        @Test
        void createFailure() {
            Try<String,Throwable> fail = Try.failure(new RuntimeException("fail"));
            assertThat(fail.isSuccess(), equalTo(false));
            assertThat(fail.isFailure(), equalTo(true));
            fail
                .onFailure(th -> assertThat(th.getMessage(), equalTo("fail")))
                .onSuccess(val -> Assertions.fail()); // don't execute
            RuntimeException thrown = assertThrows(RuntimeException.class, () -> fail.get());
            assertThat(thrown.getMessage(), equalTo("fail"));
            assertThat(fail.orElse("default"), equalTo("default"));
        }

        class CheckedException extends Exception {
            CheckedException(String message) { super(message); }
        }

        private String throwing() throws CheckedException {
            throw new CheckedException("nope");
        }

        @Test
        void failedOperationWithMapping() {
            Try<String, String> fail = Try.failable(this::throwing, Throwable::getMessage);
            RuntimeException thrown = assertThrows(RuntimeException.class, () -> fail.get());
            assertThat(thrown.getMessage(), equalTo("nope"));
        }

        @Test
        void failedOperation() {
            Try<?,Throwable> fail = Try.failable(this::throwing, ex -> ex);
            assertThat(fail.isFailure(), equalTo(true));
            fail.onFailure(th -> assertThat(th.getMessage(), equalTo("nope")));
            RuntimeException thrown = assertThrows(RuntimeException.class, () -> fail.get());
            assertThat(thrown.getCause().getMessage(), equalTo("nope"));
        }

        @Test
        void mappingFailure() {
            Try<String,Throwable> origin = Try.failure(new RuntimeException("fail"));
            Try<Integer,Throwable> result = origin.map(Integer::parseInt);
            assertThat(result.isFailure(), equalTo(true));
            result.onFailure(th -> assertThat(th.getMessage(), equalTo("fail")));
        }

        private Try<Integer,Throwable> parsing(String value) {
            return Try.failable(() -> Integer.parseInt(value), ex -> ex);
        }

        @Test
        void flatMappingFailure() {
            Try<String,Throwable> origin = Try.failure(new RuntimeException("fail"));
            Try<Integer,Throwable> result = origin.flatMap(this::parsing);
            assertThat(result.isFailure(), equalTo(true));
            result.onFailure(th -> assertThat(th.getMessage(), equalTo("fail")));
        }

        private Integer failingTransformation(String value) {
            throw new RuntimeException("Cannot map " + value);
        }

        @Test
        void mappingFails() {
            Try<String,Throwable> origin = Try.success("42", ex -> ex);
            Try<Integer,Throwable> result = origin.map(this::failingTransformation);
            assertThat(result.isFailure(), equalTo(true));
            result.onFailure(th -> assertThat(th.getMessage(), equalTo("Cannot map 42")));
        }

        private Try<Integer,Throwable> failingFlatTransformation(String value) {
            throw new RuntimeException("Cannot map " + value);
        }

        @Test
        void flatMappingFails() {
            Try<String,Throwable> origin = Try.success("42", ex -> ex);
            Try<Integer,Throwable> result = origin.flatMap(this::failingFlatTransformation);
            assertThat(result.isFailure(), equalTo(true));
            result.onFailure(th -> assertThat(th.getMessage(), equalTo("Cannot map 42")));
        }
    }

    @Nested
    class Invalid {

        @Test
        void successIsNull() {
            assertThrows(NullPointerException.class, () -> Try.success(null, ex -> ex));
        }

        @Test
        void errorMapperIsNull() {
            assertThrows(NullPointerException.class, () -> Try.success("Hello", null));
        }

        @Test
        void failureIsNull() {
            assertThrows(NullPointerException.class, () -> Try.failure(null));
        }

        @Test
        void failableIsNull() {
            assertThrows(NullPointerException.class, () -> Try.failable(null, ex -> ex));
        }

        @Test
        void failableErrorMapperIsNull() {
            assertThrows(NullPointerException.class, () -> Try.failable(() -> "Hello", null));
        }

        @Test
        void failableProvidesNull() {
            assertThrows(NullPointerException.class, () -> Try.failable(() -> null, ex -> ex));
        }
    }
}
