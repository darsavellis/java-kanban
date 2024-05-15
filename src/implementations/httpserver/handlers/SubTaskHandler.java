package implementations.httpserver.handlers;

import implementations.httpserver.handlers.exceptions.BadRequestParameters;
import implementations.tasks.SubTask;
import interfaces.TaskManager;
import java.util.Objects;

public class SubTaskHandler extends BaseHttpTaskHandler<SubTask> {
    public SubTaskHandler(TaskManager taskManager) {
        super(taskManager, taskManager::createSubTask, taskManager::updateSubTask, taskManager::getSubTaskById,
                taskManager::getAllSubTasks, taskManager::removeSubTaskById, SubTask.class);
    }

    @Override
    protected void validate(SubTask subTask) throws BadRequestParameters {
        if (Objects.isNull(subTask.getEpicID()) || Objects.isNull(subTask.getName())
                || Objects.isNull(subTask.getDescription()) || Objects.isNull(subTask.getState())
                || Objects.isNull(subTask.getDuration()) != Objects.isNull(subTask.getStartTime())) {
            throw new BadRequestParameters("Bad request parameters for subtask");
        }
    }
}