package implementations.httpserver.handlers.adapters;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import implementations.tasks.Epic;
import implementations.tasks.SubTask;
import implementations.tasks.Task;
import implementations.utility.State;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class TaskAdapter extends TypeAdapter<Task> {
    DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    @Override
    public void write(JsonWriter jsonWriter, Task task) throws IOException {
        jsonWriter.beginObject();
        jsonWriter.name("id");
        jsonWriter.value(task.getId());
        if (task instanceof SubTask) {
            jsonWriter.name("epicID");
            jsonWriter.value(((SubTask) task).getEpicID());
        }
        jsonWriter.name("name");
        jsonWriter.value(task.getName());
        jsonWriter.name("description");
        jsonWriter.value(task.getDescription());
        jsonWriter.name("state");
        if (Objects.nonNull(task.getState())) {
            jsonWriter.value(task.getState().toString());
        } else {
            jsonWriter.nullValue();
        }
        jsonWriter.name("startTime");
        if (Objects.nonNull(task.getStartTime())) {
            jsonWriter.value(task.getStartTime().format(dateTimeFormatter));
        } else {
            jsonWriter.nullValue();
        }
        jsonWriter.name("duration");
        jsonWriter.value(task.getDuration());
        jsonWriter.name("type");
        if (task instanceof Epic) {
            jsonWriter.value("epic");
        } else if (task instanceof SubTask) {
            jsonWriter.value("subtask");
        } else {
            jsonWriter.value("task");
        }
        jsonWriter.endObject();
    }

    @Override
    public Task read(JsonReader jsonReader) throws IOException {
        Integer id = null;
        Integer epicID = null;
        String name = null;
        String description = null;
        State state = null;
        LocalDateTime startTime = null;
        Long duration = null;
        String type = "";
        jsonReader.beginObject();
        while (jsonReader.hasNext()) {
            String fieldName = jsonReader.nextName();
            String value = jsonReader.nextString();
            switch (fieldName) {
                case "epicID":
                    epicID = Integer.valueOf(value);
                    break;
                case "id":
                    id = Integer.valueOf(value);
                    break;
                case "name":
                    name = value;
                    break;
                case "description":
                    description = value;
                    break;
                case "state":
                    state = State.valueOf(value);
                    break;
                case "startTime":
                    startTime = LocalDateTime.parse(value, dateTimeFormatter);
                    break;
                case "duration":
                    duration = Long.valueOf(value);
                    break;
                case "type":
                    type = value;
                    break;
            }
        }

        jsonReader.endObject();

        switch (type) {
            case "task":
                return new Task(id, name, description, state, startTime, duration);
            case "subtask":
                return new SubTask(id, epicID, name, description, state, startTime, duration);
            default:
                return new Epic(id, name, description);
        }
    }
}
