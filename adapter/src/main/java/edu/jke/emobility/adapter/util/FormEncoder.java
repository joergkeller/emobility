package edu.jke.emobility.adapter.util;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.AbstractMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class FormEncoder {

    private Map<String,String> fields = new LinkedHashMap<>();

    public FormEncoder addField(String key, String value) {
        fields.put(key, value);
        return this;
    }

    public FormEncoder addField(String key, Number value) {
        fields.put(key, String.valueOf(value));
        return this;
    }

    public FormEncoder addField(String key, LocalDateTime value) {
        fields.put(key, DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(value));
        return this;
    }

    public FormEncoder addField(String key, ZonedDateTime value) {
        ZonedDateTime utc = value.withZoneSameInstant(ZoneId.of("UTC"));
        fields.put(key, DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(utc) + "Z");
        return this;
    }

    private Map.Entry<String,String> urlEncode(Map.Entry<String,String> entry) {
        return new AbstractMap.SimpleImmutableEntry<String, String>(urlEncode(entry.getKey()), urlEncode(entry.getValue()));
    }

    private String urlEncode(String raw) {
        return URLEncoder.encode(raw, StandardCharsets.UTF_8);
    }

    public String asUrlEncoded() {
        return fields.entrySet().stream()
                .map(this::urlEncode)
                .map(e -> e.getKey() + "=" + e.getValue())
                .collect(Collectors.joining("&"));
    }

    public byte[] asUrlEncodedBytes() {
        return asUrlEncoded().getBytes(StandardCharsets.UTF_8);
    }
}
