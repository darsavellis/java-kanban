package interfaces;

import implementations.task_classes.Epic;
import implementations.task_classes.SubTask;
import implementations.task_classes.Task;

import java.util.ArrayList;
import java.util.List;

public interface TaskManager {
    void createTask(Task task);

    boolean updateTask(Task task);

    Task getTaskById(int id);

    ArrayList<Task> getAllTasks();

    void removeAllTasks();

    Task removeTaskById(int id);

    void createEpic(Epic epic);

    boolean updateEpic(Epic epic);

    Epic getEpicById(int id);

    ArrayList<Epic> getAllEpics();

    void removeAllEpics();

    Epic removeEpicById(int id);

    ArrayList<SubTask> getAllSubTasksFromEpic(Epic epic);

    void createSubTask(SubTask subTask);

    boolean updateSubTask(SubTask subTask);

    SubTask getSubTaskById(int id);

    ArrayList<SubTask> getAllSubTasks();

    SubTask removeSubTaskById(int id);

    void removeAllSubTasks();

    public List<Task> getHistoryManager();
}
