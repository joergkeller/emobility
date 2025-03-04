package edu.jke.emobility.usecase.error;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class TryTest {

    @Nested
    class Successes {

        @Test
        void createSuccess() {
            Try<String,Throwable> success = Try.success("done");
            assertThat(success.isSuccess()).isTrue();
            assertThat(success.isFailure()).isFalse();
            success
                    .onSuccess(val -> assertThat(val).isEqualTo("done"))
                    .onFailure(_ -> Assertions.fail()); // don't execute
            assertThat(success.get()).isEqualTo("done");
            assertThat(success.orElse("default")).isEqualTo("done");
        }

        @Test
        void createSuccessWithErrorMapper() {
            Try<String,String> success = Try.success("done", Throwable::getMessage);
            assertThat(success.isSuccess()).isTrue();
            assertThat(success.isFailure()).isFalse();
            success
                    .onSuccess(val -> assertThat(val).isEqualTo("done"))
                    .onFailure(_ -> Assertions.fail()); // don't execute
        }

        @Test
        void successfulOperation() {
            Try<String,Throwable> success = Try.failable(() -> "finally done");
            assertThat(success.isSuccess()).isTrue();
            success.onSuccess(val -> assertThat(val).isEqualTo("finally done"));
        }

        @Test
        void successfulOperationWithErrorMapper() {
            Try<String,String> success = Try.failable(() -> "finally done", Throwable::getMessage);
            assertThat(success.isSuccess()).isTrue();
            success.onSuccess(val -> assertThat(val).isEqualTo("finally done"));
        }

        @Test
        void mappingSuccess() {
            Try<String,String> origin = Try.success("42", Throwable::getMessage);
            Try<Integer,String> result = origin.map(Integer::parseInt);
            assertThat(result.isSuccess()).isTrue();
            result.onSuccess(val -> assertThat(val).isEqualTo(42));
        }

        private Try<Integer,String> parsing(String value) {
            return Try.failable(() -> Integer.parseInt(value), Throwable::getMessage);
        }

        @Test
        void flatMappingSuccess() {
            Try<String,String> origin = Try.success("42", Throwable::getMessage);
            Try<Integer,String> result = origin.flatMap(this::parsing);
            assertThat(result.isSuccess()).isTrue();
            result.onSuccess(val -> assertThat(val).isEqualTo(42));
        }

    }

    @Nested
    class Failures {

        @Test
        void createFailure() {
            Try<String,Throwable> fail = Try.failure(new RuntimeException("fail"));
            assertThat(fail.isSuccess()).isFalse();
            assertThat(fail.isFailure()).isTrue();
            fail
                .onFailure(th -> assertThat(th.getMessage()).isEqualTo("fail"))
                .onSuccess(_ -> Assertions.fail()); // don't execute
            RuntimeException thrown = assertThrows(RuntimeException.class, fail::get);
            assertThat(thrown.getMessage()).isEqualTo("fail");
            assertThat(fail.orElse("default")).isEqualTo("default");
        }

        static class CheckedException extends Exception {
            CheckedException(String message) { super(message); }
        }

        private String throwing() throws CheckedException {
            throw new CheckedException("nope");
        }

        @Test
        void failedOperationWithMapping() {
            Try<String, String> fail = Try.failable(this::throwing, Throwable::getMessage);
            RuntimeException thrown = assertThrows(RuntimeException.class, fail::get);
            assertThat(thrown.getMessage()).isEqualTo("nope");
        }

        @Test
        void failedOperation() {
            Try<?,Throwable> fail = Try.failable(this::throwing, ex -> ex);
            assertThat(fail.isFailure()).isTrue();
            fail.onFailure(th -> assertThat(th.getMessage()).isEqualTo("nope"));
            RuntimeException thrown = assertThrows(RuntimeException.class, fail::get);
            assertThat(thrown.getCause().getMessage()).isEqualTo("nope");
        }

        @Test
        void mappingFailure() {
            Try<String,Throwable> origin = Try.failure(new RuntimeException("fail"));
            Try<Integer,Throwable> result = origin.map(Integer::parseInt);
            assertThat(result.isFailure()).isTrue();
            result.onFailure(th -> assertThat(th.getMessage()).isEqualTo("fail"));
        }

        private Try<Integer,Throwable> parsing(String value) {
            return Try.failable(() -> Integer.parseInt(value), ex -> ex);
        }

        @Test
        void flatMappingFailure() {
            Try<String,Throwable> origin = Try.failure(new RuntimeException("fail"));
            Try<Integer,Throwable> result = origin.flatMap(this::parsing);
            assertThat(result.isFailure()).isTrue();
            result.onFailure(th -> assertThat(th.getMessage()).isEqualTo("fail"));
        }

        private Integer failingTransformation(String value) {
            throw new RuntimeException("Cannot map " + value);
        }

        @Test
        void mappingFails() {
            Try<String,Throwable> origin = Try.success("42", ex -> ex);
            Try<Integer,Throwable> result = origin.map(this::failingTransformation);
            assertThat(result.isFailure()).isTrue();
            result.onFailure(th -> assertThat(th.getMessage()).isEqualTo("Cannot map 42"));
        }

        private Try<Integer,Throwable> failingFlatTransformation(String value) {
            throw new RuntimeException("Cannot map " + value);
        }

        @Test
        void flatMappingFails() {
            Try<String,Throwable> origin = Try.success("42", ex -> ex);
            Try<Integer,Throwable> result = origin.flatMap(this::failingFlatTransformation);
            assertThat(result.isFailure()).isTrue();
            result.onFailure(th -> assertThat(th.getMessage()).isEqualTo("Cannot map 42"));
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
