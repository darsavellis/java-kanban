package interfaces;

import implementations.tasks.Epic;
import implementations.tasks.SubTask;
import implementations.tasks.Task;

import java.util.ArrayList;
import java.util.List;

public interface TaskManager {
    Integer createTask(Task task);

    boolean updateTask(Task task);

    Task getTaskById(int id);

    ArrayList<Task> getAllTasks();

    void removeAllTasks();

    Task removeTaskById(Integer id);

    Integer createEpic(Epic epic);

    boolean updateEpic(Epic epic);

    Epic getEpicById(Integer id);

    List<Epic> getAllEpics();

    void removeAllEpics();

    Epic removeEpicById(Integer id);

    List<SubTask> getAllSubTasksFromEpic(Epic epic);

    Integer createSubTask(SubTask subTask);

    boolean updateSubTask(SubTask subTask);

    SubTask getSubTaskById(Integer id);

    List<SubTask> getAllSubTasks();

    SubTask removeSubTaskById(Integer id);

    void removeAllSubTasks();

    public List<Task> getHistoryManager();
}