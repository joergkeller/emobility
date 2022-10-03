package edu.jke.emobility.usecase.session;

import edu.jke.emobility.domain.session.LoadSession;

import java.util.List;
import java.util.function.Function;

/**
 * Creates instances of output writers that can be used to write CSV or JSON files for a given data structure.
 */
public interface WriterFactory {
    <T> OutputWriter<T> createWriter(String baseName, List<String> fieldList, Function<T, List<String>> fieldMapper);
}
