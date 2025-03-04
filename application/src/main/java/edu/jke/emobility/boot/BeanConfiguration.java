package edu.jke.emobility.boot;

import edu.jke.emobility.adapter.station.StationAdapter;
import edu.jke.emobility.adapter.writer.CsvWriterFactory;
import edu.jke.emobility.domain.tariff.ConstantTariffSplitter;
import edu.jke.emobility.domain.tariff.InterpolationTariffSplitter;
import edu.jke.emobility.domain.tariff.TariffSetting;
import edu.jke.emobility.domain.tariff.TariffSplitter;
import edu.jke.emobility.usecase.session.RequestLoadSessionsUC;
import edu.jke.emobility.usecase.session.StationEndpoint;
import edu.jke.emobility.usecase.session.WriterFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource("classpath:secrets.properties")
public class BeanConfiguration {

    @Bean
    RequestLoadSessionsUC loadSessionsUseCase(StationEndpoint stationAdapter, TariffSplitter tariffSplitter, WriterFactory writerFactory) {
        return new RequestLoadSessionsUC(stationAdapter, tariffSplitter, writerFactory);
    }

    @Bean
    TariffSplitter tariffSplitter(TariffSetting tariff) {
        return new InterpolationTariffSplitter(tariff);
    }

    @Bean
    TariffSetting tariffSetting() {
        return new TariffSetting();
    }

    @Bean
    WriterFactory writerFactory() {
        return new CsvWriterFactory();
    }

    @Bean
    @ConfigurationProperties(prefix = "adapter.charger")
    StationEndpoint stationAdapter() {
        return new StationAdapter();
    }

}
