package edu.jke.emobility.adapter.writer;

import edu.jke.emobility.usecase.error.ServiceException;
import edu.jke.emobility.usecase.session.OutputWriter;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.function.Function;

public class CsvOutputWriter<T> implements OutputWriter<T> {

    private final String fileSuffix = ".csv";
    private final String fieldDelimiter = ";";
    private final String lineDelimiter = "\n";
    private final Function<T, List<String>> fieldMapper;

    private BufferedWriter writer;

    public CsvOutputWriter(String baseName, List<String> fieldList, Function<T, List<String>> fieldMapper) {
        try {
            this.writer = new BufferedWriter(new FileWriter(baseName + fileSuffix));
            writeFields(fieldList);
            this.fieldMapper = fieldMapper;
        } catch (IOException ex) {
            throw new ServiceException(ex);
        }
    }

    @Override
    public OutputWriter write(List<T> rows) {
        rows.forEach(this::write);
        return this;
    }

    @Override
    public OutputWriter write(T row) {
        try {
            List<String> fields = fieldMapper.apply(row);
            writeFields(fields);
        } catch (IOException e) {
            throw new ServiceException(e);
        }
        return this;
    }

    private void writeFields(List<String> fields) throws IOException {
        writer.write(String.join(fieldDelimiter, fields));
        writer.write(lineDelimiter);
    }

    @Override
    public void close() {
        try {
            writer.close();
        } catch (IOException e) {
            throw new ServiceException(e);
        }
    }

}
