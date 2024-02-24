package implementations.dispatchers;

import implementations.task_classes.Epic;
import implementations.task_classes.SubTask;
import implementations.task_classes.Task;
import implementations.utility.Managers;
import interfaces.HistoryManager;
import interfaces.TaskManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class InMemoryTaskManager implements TaskManager {
    static int TASK_COUNTER = 0;
    private final HashMap<Integer, Task> taskHashMap;
    private final HashMap<Integer, SubTask> subTaskHashMap;
    private final HashMap<Integer, Epic> epicHashMap;

    private final HistoryManager historyManager = Managers.getDefaultHistory();

    private static int generateId() {
        return TASK_COUNTER++;
    }

    public InMemoryTaskManager() {
        taskHashMap = new HashMap<>();
        subTaskHashMap = new HashMap<>();
        epicHashMap = new HashMap<>();
    }

    @Override
    public void createTask(Task task) {
        task.setId(generateId());
        taskHashMap.put(task.getId(), task);
    }

    @Override
    public boolean updateTask(Task task) {
        if (taskHashMap.containsKey(task.getId())) {
            taskHashMap.put(task.getId(), task);
        } else {
            return false;
        }
        return true;
    }

    @Override
    public Task getTaskById(int id) {
        Task task = taskHashMap.get(id);
        historyManager.addTask(task);
        return task;
    }

    @Override
    public ArrayList<Task> getAllTasks() {
        return new ArrayList<>(taskHashMap.values());
    }

    @Override
    public void removeAllTasks() {
        taskHashMap.clear();
    }

    @Override
    public Task removeTaskById(int id) {
        return taskHashMap.remove(id);
    }

    @Override
    public void createEpic(Epic epic) {
        epic.setId(generateId());
        epicHashMap.put(epic.getId(), epic);
    }

    @Override
    public boolean updateEpic(Epic epic) {
        if (epicHashMap.containsKey(epic.getId())) {
            epicHashMap.put(epic.getId(), epic);
        } else {
            return false;
        }
        return true;
    }

    @Override
    public Epic getEpicById(int id) {
        Epic epic = epicHashMap.get(id);
        historyManager.addTask(epic);
        return epic;
    }

    @Override
    public ArrayList<Epic> getAllEpics() {
        return new ArrayList<>(epicHashMap.values());
    }

    @Override
    public void removeAllEpics() {
        removeAllSubTasks();
        epicHashMap.clear();
    }

    @Override
    public Epic removeEpicById(int id) {
        return epicHashMap.remove(id);
    }

    @Override
    public ArrayList<SubTask> getAllSubTasksFromEpic(Epic epic) {
        ArrayList<SubTask> subTaskArrayList = new ArrayList<>();
        for (Integer subTaskId : epic.getSubTaskArrayList()) {
            subTaskArrayList.add(subTaskHashMap.get(subTaskId));
        }
        return subTaskArrayList;
    }

    @Override
    public void createSubTask(SubTask subTask) {
        subTask.setId(generateId());
        linkSubTaskToEpic(subTask);
        subTaskHashMap.put(subTask.getId(), subTask);
    }

    @Override
    public boolean updateSubTask(SubTask subTask) {
        if (subTaskHashMap.containsKey(subTask.getId())) {
            removeSubTaskById(subTask.getId());
            linkSubTaskToEpic(subTask);
            subTaskHashMap.put(subTask.getId(), subTask);
        } else {
            return false;
        }
        return true;
    }

    @Override
    public SubTask getSubTaskById(int id) {
        SubTask subTask = subTaskHashMap.get(id);
        historyManager.addTask(subTask);
        return subTask;
    }

    @Override
    public ArrayList<SubTask> getAllSubTasks() {
        return new ArrayList<>(subTaskHashMap.values());
    }

    @Override
    public SubTask removeSubTaskById(int id) {
        SubTask subTask = subTaskHashMap.remove(id);
        epicHashMap.get(subTask.getEpicID()).removeSubTaskId(subTask);
        return subTask;
    }

    @Override
    public void removeAllSubTasks() {
        subTaskHashMap.clear();
    }

    private void linkSubTaskToEpic(SubTask subTask) {
        Epic epic = epicHashMap.get(subTask.getEpicID());
        epic.addSubTaskId(subTask);
    }

    public List<Task> getHistoryManager() {
        return historyManager.getHistory();
    }
}