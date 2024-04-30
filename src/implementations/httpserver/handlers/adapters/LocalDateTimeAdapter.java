package implementations.httpserver.handlers.adapters;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class LocalDateTimeAdapter extends TypeAdapter<LocalDateTime> {
    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    @Override
    public void write(final JsonWriter jsonWriter, final LocalDateTime localDateTime) throws IOException {
        if (Objects.nonNull(localDateTime)) {
            jsonWriter.value(localDateTime.format(dateTimeFormatter));
        } else {
            jsonWriter.nullValue();
        }
    }

    @Override
    public LocalDateTime read(final JsonReader jsonReader) throws IOException {
        String nextString = jsonReader.nextString();
        if (Objects.nonNull(nextString)) {
            return LocalDateTime.parse(nextString, dateTimeFormatter);
        } else {
            return null;
        }
    }
}