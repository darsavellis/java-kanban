package implementations.dispatchers;

import implementations.tasks.Task;
import interfaces.HistoryManager;

import java.util.LinkedList;
import java.util.List;

public class InMemoryHistoryManager implements HistoryManager {
    private List<Task> historyQueue = new LinkedList<>();

    @Override
    public void addTask(Task task) {
        historyQueue.add(task);
        limitSizeOfQueue();
    }

    private void limitSizeOfQueue() {
        if (historyQueue.size() > 10) {
            historyQueue.remove(0);
        }
    }

    @Override
    public List<Task> getHistory() {
        return historyQueue;
    }
}