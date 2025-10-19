package edu.jke.emobility.boot;

import edu.jke.emobility.usecase.session.RequestLoadSessionsUC;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.LocalDateTime;
import java.util.List;

@SpringBootApplication
@ConfigurationProperties(prefix = "main")
public class SpringConsoleApplication implements CommandLineRunner {

    private static Logger log = LoggerFactory.getLogger(SpringConsoleApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(SpringConsoleApplication.class, args);
    }

    @Autowired
    private RequestLoadSessionsUC loadSessions;

    private LocalDateTime startTimestamp;
    private LocalDateTime endTimestamp = LocalDateTime.now();
    private List<String> chargerNames;

    public void setStartTimestamp(LocalDateTime timestamp) {
        this.startTimestamp = timestamp;
    }

    public void setEndTimestamp(LocalDateTime timestamp) {
        this.endTimestamp = timestamp;
    }

    public void setChargerNames(List<String> chargerNames) {
        this.chargerNames = chargerNames;
    }

    @Override
    public void run(String... args) throws Exception {
        log.info("Loading charger sessions from {}", chargerNames);
        RequestLoadSessionsUC.Request request = loadSessions.validate(chargerNames, startTimestamp, endTimestamp);
        loadSessions.loadSessions(request);
    }

}
