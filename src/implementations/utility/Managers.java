package implementations.utility;


import implementations.dispatchers.InMemoryHistoryManager;
import implementations.dispatchers.InMemoryTaskManager;
import interfaces.HistoryManager;
import interfaces.TaskManager;

public class Managers {
    public static HistoryManager getDefaultHistory() {
        return new InMemoryHistoryManager();
    }

    public static TaskManager getDefault() {
        return new InMemoryTaskManager();
    }
}