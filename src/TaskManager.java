import java.util.ArrayList;
import java.util.HashMap;

public class TaskManager {
    static int TASK_COUNTER = 0;
    private final HashMap<Integer, Task> taskHashMap;
    private final HashMap<Integer, SubTask> subTaskHashMap;
    private final HashMap<Integer, Epic> epicHashMap;

    private static int generateId() {
        return TASK_COUNTER++;
    }

    public TaskManager() {
        taskHashMap = new HashMap<>();
        subTaskHashMap = new HashMap<>();
        epicHashMap = new HashMap<>();
    }

    public void createTask(Task task) {
        task.setId(generateId());
        taskHashMap.put(task.getId(), task);
    }

    public boolean updateTask(Task task) {
        if (taskHashMap.containsKey(task.getId())) {
            taskHashMap.put(task.getId(), task);
        } else {
            return false;
        }
        return true;
    }

    public Task getTaskById(int id) {
        return taskHashMap.get(id);
    }

    public ArrayList<Task> getAllTasks() {
        return new ArrayList<>(taskHashMap.values());
    }

    public void removeAllTasks() {
        taskHashMap.clear();
    }

    public Task removeTaskById(int id) {
        return taskHashMap.remove(id);
    }

    public void createEpic(Epic epic) {
        epic.setId(generateId());
        epicHashMap.put(epic.getId(), epic);
    }

    public boolean updateEpic(Epic epic) {
        if (epicHashMap.containsKey(epic.getId())) {
            epicHashMap.put(epic.getId(), epic);
        } else {
            return false;
        }
        return true;
    }

    public Epic getEpicById(int id) {
        return epicHashMap.get(id);
    }

    public ArrayList<Epic> getAllEpics() {
        return new ArrayList<>(epicHashMap.values());
    }

    public void removeAllEpics() {
        removeAllSubTasks();
        epicHashMap.clear();
    }

    public Epic removeEpicById(int id) {
        return epicHashMap.remove(id);
    }

    public ArrayList<SubTask> getAllSubTasksFromEpic(Epic epic) {
        ArrayList<SubTask> subTaskArrayList = new ArrayList<>();
        for (Integer subTaskId : epic.getSubTaskArrayList()) {
            subTaskArrayList.add(subTaskHashMap.get(subTaskId));
        }
        return subTaskArrayList;
    }

    public void createSubTask(SubTask subTask) {
        subTask.setId(generateId());
        linkSubTaskToEpic(subTask);
        subTaskHashMap.put(subTask.getId(), subTask);
    }

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

    public SubTask getSubTaskById(int id) {
        return subTaskHashMap.get(id);
    }

    public ArrayList<SubTask> getAllSubTasks() {
        return new ArrayList<>(subTaskHashMap.values());
    }

    public SubTask removeSubTaskById(int id) {
        SubTask subTask = subTaskHashMap.remove(id);
        epicHashMap.get(subTask.getEpicID()).removeSubTaskId(subTask);
        return subTask;
    }

    public void removeAllSubTasks() {
        subTaskHashMap.clear();
    }

    private void linkSubTaskToEpic(SubTask subTask) {
        Epic epic = epicHashMap.get(subTask.getEpicID());
        epic.addSubTaskId(subTask);
    }
}