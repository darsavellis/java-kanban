package interfaces;

import implementations.task_classes.Task;

import java.util.List;

public interface HistoryManager {
    void addTask(Task task);

    public List<Task> getHistory();
}