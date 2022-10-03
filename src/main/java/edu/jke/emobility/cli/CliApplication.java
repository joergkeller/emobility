package edu.jke.emobility.cli;

import edu.jke.emobility.adapter.station.StationAdapter;
import edu.jke.emobility.adapter.writer.CsvWriterFactory;
import edu.jke.emobility.domain.error.ApplicationException;
import edu.jke.emobility.usecase.session.RequestLoadSessionsUC;
import edu.jke.emobility.usecase.session.WriterFactory;

import java.time.LocalDateTime;
import java.util.List;

public class CliApplication {

    private final StationAdapter stationAdapter = new StationAdapter();
    private final WriterFactory writerFactory = new CsvWriterFactory();

    private final RequestLoadSessionsUC requestLoadSessionsUC = new RequestLoadSessionsUC(stationAdapter, writerFactory);

    public static void main(String... args) throws ApplicationException {
        new CliApplication().run();
    }

    public CliApplication() {
        stationAdapter.setUsername(System.getenv("user"));
        stationAdapter.setPassword(System.getenv("password"));
    }

    private void run() throws ApplicationException {
        List<String> stationNames = List.of(
                "platz4.local",
                "platz5.local",
                "platz6.local",
                "platz10.local",
                "platz13.local",
                "platz16.local",
                "platz20.local",
                "platz24.local");
        LocalDateTime from = LocalDateTime.parse("2022-04-01T00:00:00");
//        LocalDateTime to = LocalDateTime.parse("2022-05-18T12:00:00");

//        LocalDateTime from = LocalDateTime.parse("2022-05-18T12:00:00");
//        LocalDateTime to = LocalDateTime.parse("2022-08-19T12:00:00");

//        LocalDateTime from = LocalDateTime.parse("2022-08-19T12:00:00");
        LocalDateTime to = LocalDateTime.now();

        RequestLoadSessionsUC.Request request = requestLoadSessionsUC.validate(stationNames, from, to);
        requestLoadSessionsUC.invoke(request);
    }

}
