package implementations.httpserver.handlers;

import implementations.httpserver.handlers.exceptions.BadRequestParameters;
import implementations.tasks.Task;
import interfaces.TaskManager;

import java.util.Objects;

public class TaskHandler extends BaseHttpTaskHandler<Task> {
    public TaskHandler(TaskManager taskManager) {
        super(taskManager, taskManager::createTask, taskManager::updateTask,
                taskManager::getTaskById, taskManager::getAllTasks, taskManager::removeTaskById, Task.class);
    }

    @Override
    protected void validate(Task task) throws BadRequestParameters {
        if (Objects.isNull(task.getName()) || Objects.isNull(task.getDescription())
                || Objects.isNull(task.getState())) {
            throw new BadRequestParameters("Bad request parameters for task");
        }
    }
}