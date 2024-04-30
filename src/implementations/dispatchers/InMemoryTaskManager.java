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
        prioritizedTasks = new TreeSet<>();
    }

    protected Optional<Task> putTaskInStorage(Task task) {
        if (task != null && validateTaskOnOverlapping(task)) {
            taskHashMap.put(task.getId(), task);
            return Optional.of(task);
        } else {
            return Optional.empty();
        }
    }

    @Override
    public Optional<Task> createTask(Task task) {
        task.setId(generateId());
        return putTaskInStorage(task);
    }

    @Override
    public boolean updateTask(Task task) {
        if (taskHashMap.containsKey(task.getId())) {
            Task removedTask = removeTaskById(task.getId());
            Optional<Task> optionalTask = putTaskInStorage(task);
            if (optionalTask.isEmpty()) {
                putTaskInStorage(removedTask);
                return false;
            }
            return true;
        } else {
            return false;
        }
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

    protected Optional<Epic> putEpicInStorage(Epic epic) {
        if (epic != null) {
            Epic replacedEpic = epicHashMap.put(epic.getId(), epic);
            if (Objects.nonNull(replacedEpic)) {
                epic.setSubTaskArrayList(replacedEpic.getSubTaskArrayList());
                epic.setStateStatistics(replacedEpic.getStateStatistics());
                epic.updateState();
                updateEpicTime(epic);
            } else {
                epic.initialize();
            }
            return Optional.of(epic);
        } else {
            return Optional.empty();
        }
    }

    @Override
    public Optional<Epic> createEpic(Epic epic) {
        epic.setId(generateId());
        return putEpicInStorage(epic);
    }

    @Override
    public boolean updateEpic(Epic epic) {
        if (epicHashMap.containsKey(epic.getId())) {
            Optional<Epic> optionalEpic = putEpicInStorage(epic);
            return optionalEpic.isPresent();
        } else {
            return false;
        }
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
            epicHashMap.get(id).getSubTaskArrayList().forEach(this::removeSubTaskById);
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

    protected Optional<SubTask> putSubTaskInStorage(SubTask subTask) {
        if (subTask != null && validateTaskOnOverlapping(subTask) && linkSubTaskToEpic(subTask)) {
            subTaskHashMap.put(subTask.getId(), subTask);
            updateEpicTime(epicHashMap.get(subTask.getEpicID()));
            return Optional.of(subTask);
        } else {
            return Optional.empty();
        }
    }

    @Override
    public Optional<SubTask> createSubTask(SubTask subTask) {
        subTask.setId(generateId());
        return putSubTaskInStorage(subTask);
    }

    @Override
    public boolean updateSubTask(SubTask subTask) {
        if (subTask != null && subTaskHashMap.containsKey(subTask.getId())) {
            SubTask removedSubTask = removeSubTaskById(subTask.getId());
            Optional<SubTask> optionalSubTask = putSubTaskInStorage(subTask);
            if (optionalSubTask.isEmpty()) {
                putSubTaskInStorage(removedSubTask);
                return false;
            }
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
            Epic epic = epicHashMap.get(subTask.getEpicID());
            removeSubTaskFromEpicList(epic, subTask);
            updateEpicTime(epic);
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
        getAllSubTasks().stream().map(SubTask::getEpicID).map(epicHashMap::get).forEach(this::updateEpicTime);
        subTaskHashMap.clear();
    }

    private boolean linkSubTaskToEpic(SubTask subTask) {
        Integer epicId = subTask.getEpicID();
        if (epicHashMap.containsKey(epicId)) {
            Epic epic = epicHashMap.get(epicId);
            if (Objects.isNull(epic.getSubTaskArrayList())) {
                epic.initialize();
            }
            addSubTaskToEpicList(epic, subTask);
            return true;
        } else {
            return false;
        }
    }

    private void addSubTaskToEpicList(Epic epic, SubTask subTask) {
        epic.getSubTaskArrayList().add(subTask.getId());
        updateStatistic(epic, subTask, "add");
    }

    private void removeSubTaskFromEpicList(Epic epic, SubTask subTask) {
        epic.getSubTaskArrayList().remove(subTask.getId());
        updateStatistic(epic, subTask, "sub");
    }

    private void updateStatistic(Epic epic, SubTask subTask, String action) {
        Integer count = epic.getStateStatistics().get(subTask.getState());
        count = action.equals("add") ? count + 1 : count - 1;
        epic.getStateStatistics().put(subTask.getState(), count);
        epic.updateState();
    }

    private void updateEpicTime(Epic epic) {
        updateEpicStartTime(epic);
        updateEpicEndTime(epic);
        updateEpicDuration(epic);
    }

    private void updateEpicStartTime(Epic epic) {
        getAllSubTasksFromEpic(epic).stream().filter(Task::isReadyForPrioritizing)
                .map(Task::getStartTime).min(LocalDateTime::compareTo).ifPresent(epic::setStartTime);
    }

    private void updateEpicEndTime(Epic epic) {
        getAllSubTasksFromEpic(epic).stream().filter(Task::isReadyForPrioritizing)
                .map(Task::getEndTime).max(LocalDateTime::compareTo).ifPresent(epic::setEndTime);
    }

    private void updateEpicDuration(Epic epic) {
        Optional<Long> sumOfDuration = getAllSubTasksFromEpic(epic).stream()
                .filter(Task::isReadyForPrioritizing).map(Task::getDuration).reduce(Long::sum);

        sumOfDuration.ifPresent(epic::setDuration);
    }

    public TreeSet<Task> getPrioritizedTasks() {
        return new TreeSet<>(prioritizedTasks);
    }

    public void removeFromPrioritized(Task task) {
        prioritizedTasks.remove(task);
    }

    public boolean validateTaskOnOverlapping(Task task) {
        if (task.isReadyForPrioritizing()) {
            int sizeBefore = prioritizedTasks.size();
            prioritizedTasks.add(task);
            return prioritizedTasks.size() != sizeBefore;
        } else {
            return true;
        }
    }

    public List<Task> getHistoryManager() {
        return historyManager.getHistory();
    }
}