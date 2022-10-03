package edu.jke.emobility.usecase.session;

import edu.jke.emobility.domain.session.LoadSession;

import java.io.Closeable;
import java.util.List;

/**
 * An output writer that can be used to write a CSV or JSON file for a given data structure.
 * The structure is generic and will consist of a list of fields defined by their names and a mapping function.
 */
public interface OutputWriter<T> extends Closeable {
    OutputWriter write(List<T> rows);

    OutputWriter write(T row);

    void close();
}
