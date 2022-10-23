package edu.jke.emobility.adapter.writer;

import edu.jke.emobility.usecase.session.OutputWriter;
import edu.jke.emobility.usecase.session.WriterFactory;

import java.util.List;
import java.util.function.Function;

public class CsvWriterFactory implements WriterFactory {

    @Override
    public <T> OutputWriter<T> createWriter(String baseName, List<String> fieldList, Function<T, List<String>> fieldMapper) {
        return new CsvOutputWriter<>(baseName, fieldList, fieldMapper);
    }

}
