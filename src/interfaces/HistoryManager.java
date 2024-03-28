package interfaces;

import implementations.tasks.Task;

import java.util.List;

public interface HistoryManager {
    void addTask(Task task);

    void remove(Integer id);

    List<Task> getHistory();
}