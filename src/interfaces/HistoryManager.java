package interfaces;

import implementations.tasks.Task;

import java.util.List;

public interface HistoryManager {
    void addTask(Task task);

    public List<Task> getHistory();
}