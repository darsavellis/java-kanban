package implementations.dispatchers;

import implementations.tasks.Epic;
import implementations.tasks.SubTask;
import implementations.tasks.Task;
import implementations.utility.Managers;
import interfaces.HistoryManager;
import interfaces.TaskManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class InMemoryTaskManager implements TaskManager {
    protected static int TASK_COUNTER;
    private final HashMap<Integer, Task> taskHashMap;
    private final HashMap<Integer, SubTask> subTaskHashMap;
    private final HashMap<Integer, Epic> epicHashMap;

    protected final HistoryManager historyManager = Managers.getDefaultHistory();

    private static int generateId() {
        return TASK_COUNTER++;
    }

    public InMemoryTaskManager() {
        taskHashMap = new HashMap<>();
        subTaskHashMap = new HashMap<>();
        epicHashMap = new HashMap<>();
    }

    protected Integer putInTaskHashMap(Task task) {
        if (task != null) {
            taskHashMap.put(task.getId(), task);
            return task.getId();
        } else {
            return null;
        }
    }

    @Override
    public Integer createTask(Task task) {
        task.setId(generateId());
        return putInTaskHashMap(task);
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
        if (taskHashMap.containsKey(id)) {
            Task task = taskHashMap.get(id);
            historyManager.addTask(task);
            return task;
        } else {
            return null;
        }
    }

    @Override
    public ArrayList<Task> getAllTasks() {
        return new ArrayList<>(taskHashMap.values());
    }

    @Override
    public void removeAllTasks() {
        for (Integer id : taskHashMap.keySet()) {
            removeTaskById(id);
        }
        taskHashMap.clear();
    }

    @Override
    public Task removeTaskById(Integer id) {
        if (taskHashMap.containsKey(id)) {
            historyManager.remove(id);
            return taskHashMap.remove(id);
        } else {
            return null;
        }
    }

    protected Integer putInEpicHashMap(Epic epic) {
        if (epic != null) {
            epicHashMap.put(epic.getId(), epic);
            return epic.getId();
        } else {
            return null;
        }
    }

    @Override
    public Integer createEpic(Epic epic) {
        epic.setId(generateId());
        return putInEpicHashMap(epic);
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
    public Epic getEpicById(Integer id) {
        if (epicHashMap.containsKey(id)) {
            Epic epic = epicHashMap.get(id);
            historyManager.addTask(epic);
            return epic;
        } else {
            return null;
        }
    }

    @Override
    public ArrayList<Epic> getAllEpics() {
        return new ArrayList<>(epicHashMap.values());
    }

    @Override
    public void removeAllEpics() {
        removeAllSubTasks();
        for (Integer epicId : epicHashMap.keySet()) {
            removeEpicById(epicId);
        }
        epicHashMap.clear();
    }

    @Override
    public Epic removeEpicById(Integer id) {
        if (epicHashMap.containsKey(id)) {
            historyManager.remove(id);
            for (Integer subTaskId : epicHashMap.get(id).getSubTaskArrayList()) {
                removeSubTaskById(subTaskId);
            }
            return epicHashMap.remove(id);
        } else {
            return null;
        }
    }

    @Override
    public ArrayList<SubTask> getAllSubTasksFromEpic(Epic epic) {
        if (epic != null) {
            ArrayList<SubTask> subTaskArrayList = new ArrayList<>();
            for (Integer subTaskId : epic.getSubTaskArrayList()) {
                subTaskArrayList.add(subTaskHashMap.get(subTaskId));
            }
            return subTaskArrayList;
        } else {
            return null;
        }
    }

    protected Integer putInSubTaskHashMap(SubTask subTask) {
        if (linkSubTaskToEpic(subTask)) {
            subTaskHashMap.put(subTask.getId(), subTask);
            return subTask.getId();
        } else {
            return null;
        }
    }

    @Override
    public Integer createSubTask(SubTask subTask) {
        subTask.setId(generateId());
        return putInSubTaskHashMap(subTask);
    }

    @Override
    public boolean updateSubTask(SubTask subTask) {
        if (subTask != null && subTaskHashMap.containsKey(subTask.getId())) {
            removeSubTaskById(subTask.getId());
            linkSubTaskToEpic(subTask);
            subTaskHashMap.put(subTask.getId(), subTask);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public SubTask getSubTaskById(Integer id) {
        if (id != null) {
            SubTask subTask = subTaskHashMap.get(id);
            historyManager.addTask(subTask);
            return subTask;
        } else {
            return null;
        }
    }

    @Override
    public ArrayList<SubTask> getAllSubTasks() {
        return new ArrayList<>(subTaskHashMap.values());
    }

    @Override
    public SubTask removeSubTaskById(Integer id) {
        if (subTaskHashMap.containsKey(id)) {
            SubTask subTask = subTaskHashMap.remove(id);
            epicHashMap.get(subTask.getEpicID()).removeSubTaskId(subTask);
            historyManager.remove(id);
            return subTask;
        } else {
            return null;
        }
    }

    @Override
    public void removeAllSubTasks() {
        for (Integer id : subTaskHashMap.keySet()) {
            removeSubTaskById(id);
        }
        subTaskHashMap.clear();
    }

    private boolean linkSubTaskToEpic(SubTask subTask) {
        int epicId = subTask.getEpicID();
        if (epicHashMap.containsKey(epicId)) {
            Epic epic = epicHashMap.get(epicId);
            epic.addSubTaskId(subTask);
            return true;
        } else {
            return false;
        }
    }

    public List<Task> getHistoryManager() {
        return historyManager.getHistory();
    }
}