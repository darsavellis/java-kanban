package implementations.httpserver.handlers;

import com.sun.net.httpserver.HttpExchange;
import implementations.httpserver.handlers.typetokens.TaskListTypeToken;
import implementations.tasks.Task;
import interfaces.TaskManager;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class HistoryHandler extends BaseHttpHandler {
    public HistoryHandler(TaskManager taskManager) {
        super(taskManager);
    }

    @Override
    protected void deleteMethodHandler(HttpExchange httpExchange, LinkedList<String> paths) throws IOException {
        sendNotFound(httpExchange);
    }

    @Override
    protected void postMethodHandler(HttpExchange httpExchange, LinkedList<String> paths) throws IOException {
        sendNotFound(httpExchange);
    }

    @Override
    protected void getMethodHandler(HttpExchange httpExchange, LinkedList<String> paths) throws IOException {
        List<Task> history = getTaskManager().getHistoryManager();
        String historyJson = getGson().toJson(history, new TaskListTypeToken().getType());

        sendText(httpExchange, 200, historyJson);
    }
}
