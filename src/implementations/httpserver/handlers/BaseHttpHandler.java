package implementations.httpserver.handlers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import implementations.httpserver.handlers.adapters.DurationAdapter;
import implementations.httpserver.handlers.adapters.LocalDateTimeAdapter;
import implementations.httpserver.handlers.adapters.TaskAdapter;
import implementations.httpserver.handlers.exceptions.BadRequestParameters;
import implementations.tasks.Task;
import implementations.utility.ManagerSaveException;
import interfaces.TaskManager;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;

public abstract class BaseHttpHandler implements HttpHandler {
    private static final GsonBuilder GSON_BUILDER = new GsonBuilder();
    private static final Gson GSON;
    private final TaskManager taskManager;

    static {
        gsonBuilderInitialize();
        GSON = gsonInitialize();
    }

    public BaseHttpHandler(TaskManager taskManager) {
        this.taskManager = taskManager;
    }

    public void handle(HttpExchange httpExchange) throws IOException {
        LinkedList<String> paths = new LinkedList<>(List.of(httpExchange.getRequestURI().getPath().split("/")));
        String requestMethod = httpExchange.getRequestMethod();
        try {
            switch (requestMethod) {
                case "GET":
                    getMethodHandler(httpExchange, paths);
                    break;
                case "POST":
                    postMethodHandler(httpExchange, paths);
                    break;
                case "DELETE":
                    deleteMethodHandler(httpExchange, paths);
                    break;
            }
        } catch (BadRequestParameters badRequestParameters) {
            sendBadRequest(httpExchange);
        } catch (ManagerSaveException e) {
            sendInternalServerError(httpExchange);
        }
    }

    public void sendText(HttpExchange httpExchange, Integer statusCode, String text) throws IOException {
        byte[] response = text.getBytes(StandardCharsets.UTF_8);
        httpExchange.getResponseHeaders().add("Accept", "application/json;charset=utf-8");
        httpExchange.sendResponseHeaders(statusCode, response.length);
        try (OutputStream outputStream = httpExchange.getResponseBody()) {
            outputStream.write(response);
        }
    }

    public void sendNotFound(HttpExchange httpExchange) throws IOException {
        httpExchange.sendResponseHeaders(404, 0);
        httpExchange.close();
    }

    public void sendHasInteractions(HttpExchange httpExchange) throws IOException {
        httpExchange.sendResponseHeaders(406, 0);
        httpExchange.close();
    }

    public void sendInternalServerError(HttpExchange httpExchange) throws IOException {
        httpExchange.sendResponseHeaders(500, 0);
        httpExchange.close();
    }

    public void sendBadRequest(HttpExchange httpExchange) throws IOException {
        httpExchange.sendResponseHeaders(400, 0);
        httpExchange.close();
    }

    protected TaskManager getTaskManager() {
        return taskManager;
    }

    protected abstract void deleteMethodHandler(HttpExchange httpExchange, LinkedList<String> paths)
            throws IOException;

    protected abstract void postMethodHandler(HttpExchange httpExchange, LinkedList<String> paths)
            throws IOException, BadRequestParameters;

    protected abstract void getMethodHandler(HttpExchange httpExchange, LinkedList<String> paths)
            throws IOException;

    protected static GsonBuilder gsonBuilderInitialize() {
        return GSON_BUILDER
                .setPrettyPrinting()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .registerTypeAdapter(Duration.class, new DurationAdapter())
                .registerTypeAdapter(Task.class, new TaskAdapter());
    }

    protected static Gson gsonInitialize() {
        return gsonBuilderInitialize().create();
    }

    public static Gson getGson() {
        return GSON;
    }
}