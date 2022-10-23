package edu.jke.emobility.adapter.station;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.ZonedDateTime;
import java.util.List;

public class StationAdapterMain {

    private static List<String> hostnames = List.of(
                                                "10.0.0.152",
//                                                "10.0.0.165",
//                                                "10.0.0.209",
//                                                "10.0.0.214",
//                                                "10.0.0.225",
//                                                "10.0.0.234",
//                                                "10.0.0.241",
                                                "10.0.0.248");


    private static Logger log = LoggerFactory.getLogger(StationAdapterMain.class);

    public static void main(String[] args) {
        for (String hostname : hostnames) {
            loadSessions(hostname);
        }
    }

    private static void loadSessions(String hostname) {
//        MockWebServer server = new MockWebServer();
//        server.start();
//        HttpUrl url = server.url("/api/chargingSession/");
//        server.enqueue(new MockResponse().setBody("thisisatoken"));
//        server.enqueue(new MockResponse().setBody("[{},{}]"));
//        hostname = url.host() + ":" + url.port();

        log.info("Load sessions from {}", hostname);
        new StationAdapter()
                .login(hostname, System.getenv("user"), System.getenv("password"))
                .loadSessions(hostname, ZonedDateTime.parse("2022-04-01T00:00:00Z"), ZonedDateTime.now());

//        server.shutdown();
    }

}
