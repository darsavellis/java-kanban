package implementations.httpserver.handlers;

import com.sun.net.httpserver.HttpExchange;
import implementations.httpserver.handlers.exceptions.BadRequestParameters;
import implementations.tasks.Epic;
import implementations.tasks.SubTask;
import interfaces.TaskManager;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class EpicHandler extends BaseHttpTaskHandler<Epic> {
    public EpicHandler(TaskManager taskManager) {
        super(taskManager, taskManager::createEpic, taskManager::updateEpic,
                taskManager::getEpicById, taskManager::getAllEpics, taskManager::removeEpicById, Epic.class);
    }

    @Override
    protected void getMethodHandler(HttpExchange httpExchange, LinkedList<String> paths) throws IOException {
        super.getMethodHandler(httpExchange, paths);
        if (paths.size() == 4 && paths.pollLast().equals("subtasks")) {
            Integer epicId = Integer.parseInt(paths.get(2));

            getEpicSubTasks(httpExchange, epicId);
        }
    }

    public void getEpicSubTasks(HttpExchange httpExchange, Integer epicId) throws IOException {
        Optional<Epic> epic = getTaskManager().getEpicById(epicId);
        if (epic.isPresent()) {
            List<SubTask> subTaskList = getTaskManager().getAllSubTasksFromEpic(epic.get());
            String subtasksJson = getGson().toJson(subTaskList);

            sendText(httpExchange, 200, subtasksJson);
        } else {
            sendNotFound(httpExchange);
        }
    }

    @Override
    protected void validate(Epic epic) throws BadRequestParameters {
        if ((Objects.isNull(epic.getName()) || Objects.isNull(epic.getDescription()))
                || Objects.nonNull(epic.getState()) || Objects.nonNull(epic.getDuration())
                || Objects.nonNull(epic.getStartTime())) {
            throw new BadRequestParameters("Bad request parameters for epic");
        }
    }
}