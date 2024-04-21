package implementations.dispatchers;

import implementations.tasks.Epic;
import implementations.tasks.SubTask;
import implementations.tasks.Task;
import implementations.utility.Managers;
import interfaces.HistoryManager;
import interfaces.TaskManager;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class InMemoryTaskManager implements TaskManager {
    public static int TASK_COUNTER;
    private final HashMap<Integer, Task> taskHashMap;
    private final HashMap<Integer, SubTask> subTaskHashMap;
    private final HashMap<Integer, Epic> epicHashMap;
    private final TreeSet<Task> prioritizedTasks;
    protected final HistoryManager historyManager = Managers.getDefaultHistory();

    private static int generateId() {
        return TASK_COUNTER++;
    }

    public InMemoryTaskManager() {
        taskHashMap = new HashMap<>();
        subTaskHashMap = new HashMap<>();
        epicHashMap = new HashMap<>();
        prioritizedTasks = new TreeSet<>(Comparator.comparing(Task::getStartTime));
    }

    protected Integer putTaskInStorage(Task task) {
        if (task != null && validateTaskOnOverlapping(task)) {
            taskHashMap.put(task.getId(), task);
            addTaskToPrioritizingSet(task);
            return task.getId();
        } else {
            return null;
        }
    }

    @Override
    public Integer createTask(Task task) {
        task.setId(generateId());
        return putTaskInStorage(task);
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
    public Optional<Task> getTaskById(int id) {
        Optional<Task> task = Optional.ofNullable(taskHashMap.get(id));
        task.ifPresent(historyManager::addTask);
        return task;
    }

    @Override
    public ArrayList<Task> getAllTasks() {
        return new ArrayList<>(taskHashMap.values());
    }

    @Override
    public void removeAllTasks() {
        new ArrayList<>(taskHashMap.keySet()).forEach(this::removeTaskById);
        taskHashMap.clear();
    }

    @Override
    public Task removeTaskById(Integer id) {
        if (taskHashMap.containsKey(id)) {
            Task task = taskHashMap.remove(id);
            historyManager.remove(id);
            removeFromPrioritized(task);
            return task;
        } else {
            return null;
        }
    }

    protected Integer putEpicInStorage(Epic epic) {
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
        return putEpicInStorage(epic);
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
    public Optional<Epic> getEpicById(Integer id) {
        Optional<Epic> epic = Optional.ofNullable(epicHashMap.get(id));
        epic.ifPresent(historyManager::addTask);
        return epic;
    }

    @Override
    public ArrayList<Epic> getAllEpics() {
        return new ArrayList<>(epicHashMap.values());
    }

    @Override
    public void removeAllEpics() {
        removeAllSubTasks();
        new ArrayList<>(epicHashMap.keySet()).forEach(this::removeEpicById);
        epicHashMap.clear();
    }

    @Override
    public Epic removeEpicById(Integer id) {
        if (epicHashMap.containsKey(id)) {
            historyManager.remove(id);
            epicHashMap.get(id).getSubTaskArrayList().forEach(this::removeEpicById);
            return epicHashMap.remove(id);
        } else {
            return null;
        }
    }

    @Override
    public List<SubTask> getAllSubTasksFromEpic(Epic epic) {
        if (epic != null) {
            return epic.getSubTaskArrayList().stream().map(subTaskHashMap::get).collect(Collectors.toList());
        } else {
            return null;
        }
    }

    protected Integer putSubTaskInStorage(SubTask subTask) {
        if (subTask != null && validateTaskOnOverlapping(subTask) && linkSubTaskToEpic(subTask)) {
            subTaskHashMap.put(subTask.getId(), subTask);
            addTaskToPrioritizingSet(subTask);
            updateEpicTime(subTask);
            return subTask.getId();
        } else {
            return null;
        }
    }

    @Override
    public Integer createSubTask(SubTask subTask) {
        subTask.setId(generateId());
        return putSubTaskInStorage(subTask);
    }

    @Override
    public boolean updateSubTask(SubTask subTask) {
        if (subTask != null && subTaskHashMap.containsKey(subTask.getId())) {
            removeSubTaskById(subTask.getId());
            putSubTaskInStorage(subTask);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public Optional<SubTask> getSubTaskById(Integer id) {
        Optional<SubTask> subTask = Optional.ofNullable(subTaskHashMap.get(id));
        subTask.ifPresent(historyManager::addTask);
        return subTask;
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
            updateEpicTime(subTask);
            removeFromPrioritized(subTask);
            historyManager.remove(id);
            return subTask;
        } else {
            return null;
        }
    }

    @Override
    public void removeAllSubTasks() {
        new ArrayList<>(subTaskHashMap.keySet()).forEach(this::removeSubTaskById);
        getAllSubTasks().forEach(this::updateEpicTime);
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

    public TreeSet<Task> getPrioritizedTasks() {
        return new TreeSet<>(prioritizedTasks);
    }

    public void removeFromPrioritized(Task task) {
        prioritizedTasks.remove(task);
    }

    public void addTaskToPrioritizingSet(Task task) {
        if (task.isReadyForPrioritizing()) {
            prioritizedTasks.add(task);
        }
    }

    public boolean validateTaskOnOverlapping(Task task) {
        if (task.isReadyForPrioritizing()) {
            return getPrioritizedTasks().stream().allMatch(element -> twoTaskDoesNotOverlap(element, task));
        } else {
            return true;
        }
    }

    public boolean twoTaskDoesNotOverlap(Task firstTask, Task secondTask) {
        return firstTask.getStartTime().isAfter(secondTask.getEndTime()) ||
                secondTask.getStartTime().isAfter(firstTask.getEndTime()) ||
                firstTask.getStartTime().isEqual(secondTask.getEndTime()) ||
                secondTask.getStartTime().isEqual(firstTask.getEndTime());
    }

    private void updateEpicTime(SubTask subTask) {
        Epic epic = epicHashMap.get(subTask.getEpicID());
        updateEpicStartTime(epic);
        updateEpicEndTime(epic);
        updateEpicDuration(epic);
        epic.validateStartTimeAndDuration();
    }

    private void updateEpicStartTime(Epic epic) {
        getAllSubTasksFromEpic(epic).stream().filter(Task::isReadyForPrioritizing).
                map(Task::getStartTime).min(LocalDateTime::compareTo).ifPresent(epic::setStartTime);
    }

    private void updateEpicEndTime(Epic epic) {
        getAllSubTasksFromEpic(epic).stream().filter(Task::isReadyForPrioritizing).
                map(Task::getEndTime).max(LocalDateTime::compareTo).ifPresent(epic::setEndTime);
    }

    private void updateEpicDuration(Epic epic) {
        Optional<Long> sumOfDuration = getAllSubTasksFromEpic(epic).stream().
                filter(Task::isReadyForPrioritizing).
                map(Task::getDuration).
                reduce(Long::sum);
        sumOfDuration.ifPresent(epic::setDuration);
    }

    public List<Task> getHistoryManager() {
        return historyManager.getHistory();
    }
}