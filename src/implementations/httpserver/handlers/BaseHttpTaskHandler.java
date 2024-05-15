package implementations.httpserver.handlers;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.HttpExchange;
import implementations.httpserver.handlers.exceptions.BadRequestParameters;
import implementations.httpserver.handlers.typetokens.TaskListTypeToken;
import implementations.tasks.Task;
import interfaces.TaskManager;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

public abstract class BaseHttpTaskHandler<R extends Task> extends BaseHttpHandler {
    private final Function<R, Optional<R>> createTask;
    private final Function<R, Boolean> updateTask;
    private final Function<Integer, Optional<R>> getTaskById;
    private final Supplier<List<R>> getAllTasks;
    private final Function<Integer, R> removeTaskById;
    private final Class<R> className;

    public BaseHttpTaskHandler(
            TaskManager taskManager, Function<R, Optional<R>> createTask,
            Function<R, Boolean> updateTask, Function<Integer, Optional<R>> getTaskById,
            Supplier<List<R>> getAllTasks, Function<Integer, R> removeTaskById,
            Class<R> className) {
        super(taskManager);
        this.createTask = createTask;
        this.updateTask = updateTask;
        this.getTaskById = getTaskById;
        this.getAllTasks = getAllTasks;
        this.removeTaskById = removeTaskById;
        this.className = className;
    }


    protected void getMethodHandler(HttpExchange httpExchange, LinkedList<String> paths) throws IOException {
        if (paths.size() == 3 && !paths.getLast().isBlank()) {
            Integer taskId = Integer.parseInt(paths.getLast());

            getTaskByIdAndSendText(httpExchange, taskId);
        }
        if (paths.size() == 2) {
            getAllTasksAndSendText(httpExchange);
        }
    }

    protected void postMethodHandler(HttpExchange httpExchange, LinkedList<String> paths)
            throws IOException, BadRequestParameters {
        String requestBody = new String(httpExchange.getRequestBody().readAllBytes());
        JsonElement jsonElement = JsonParser.parseString(requestBody);
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        jsonObject.addProperty("type", className.getSimpleName().toLowerCase());
        R task = getGson().fromJson(jsonObject, className);

        validate(task);

        if (updateTask.apply(task)) {
            sendText(httpExchange, 201, "Task successfully updated");
        } else if (createTask.apply(task).isPresent()) {
            sendText(httpExchange, 201, "Task successfully created");
        } else {
            sendHasInteractions(httpExchange);
        }
    }

    protected void deleteMethodHandler(HttpExchange httpExchange, LinkedList<String> paths) throws IOException {
        String requestBody = new String(httpExchange.getRequestBody().readAllBytes());
        Task task = getGson().fromJson(requestBody, className);

        removeTaskById.apply(task.getId());
        sendText(httpExchange, 200, "Task successfully deleted");
    }

    private void getTaskByIdAndSendText(HttpExchange httpExchange, Integer taskId) throws IOException {
        Optional<R> task = getTaskById.apply(taskId);

        if (task.isPresent()) {
            String taskJson = getGson().toJson(task.get());
            sendText(httpExchange, 200, taskJson);
        } else {
            sendNotFound(httpExchange);
        }
    }

    private void getAllTasksAndSendText(HttpExchange httpExchange) throws IOException {
        List<R> taskList = getAllTasks.get();
        String taskListJson = getGson().toJson(taskList, new TaskListTypeToken().getType());

        if (taskList.isEmpty()) {
            sendText(httpExchange, 200, "Task list is empty");
        } else {
            sendText(httpExchange, 200, taskListJson);
        }
    }

    protected abstract void validate(R task) throws BadRequestParameters;
}