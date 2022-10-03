package edu.jke.emobility.adapter.util;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;

import java.io.IOException;
import java.time.Duration;

public class DurationDeserializer extends JsonDeserializer<Duration> {

    @Override
    public Duration deserialize(JsonParser parser, DeserializationContext context) throws IOException, JacksonException {
        String str = parser.getText();
        String[] daySplit = str.split("\\.");
        if (daySplit.length > 2 || daySplit.length == 0) {
            throw InvalidFormatException.from(parser, Duration.class, str);
        }

        String[] hourSplit = daySplit[daySplit.length - 1].split(":");
        Duration hourDuration = Duration.parse( switch(hourSplit.length) {
            case 1 -> String.format("PT%sS", (Object[])hourSplit);
            case 2 -> String.format("PT%sM%sS", (Object[])hourSplit);
            case 3 -> String.format("PT%sH%sM%sS", (Object[])hourSplit);
            default -> throw InvalidFormatException.from(parser, Duration.class, str);
        });

        if (daySplit.length > 1) {
            Duration dayDuration = Duration.ofDays(Integer.parseInt(daySplit[0]));
            return dayDuration.plus(hourDuration);
        } else return hourDuration;
    }

}
