package implementations.httpserver.handlers;

import com.sun.net.httpserver.HttpExchange;
import implementations.httpserver.handlers.typetokens.TaskTreeSetTypeToken;
import implementations.tasks.Task;
import interfaces.TaskManager;

import java.io.IOException;
import java.util.LinkedList;
import java.util.TreeSet;

public class PrioritizedHandler extends BaseHttpHandler {
    public PrioritizedHandler(TaskManager taskManager) {
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
        TreeSet<Task> prioritized = getTaskManager().getPrioritizedTasks();
        String prioritizedJson = getGson().toJson(prioritized, new TaskTreeSetTypeToken().getType());

        sendText(httpExchange, 200, prioritizedJson);
    }
}