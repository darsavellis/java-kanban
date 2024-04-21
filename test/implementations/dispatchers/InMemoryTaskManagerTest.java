package implementations.dispatchers;

import implementations.tasks.Epic;
import implementations.tasks.SubTask;
import implementations.tasks.Task;
import implementations.utility.Managers;
import implementations.utility.State;
import interfaces.TaskManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static java.util.Collections.singletonList;

class InMemoryTaskManagerTest extends TaskManagerTest<TaskManager> {
    private final Comparator<Task> comparatorById = Comparator.comparing(Task::getId);

    @BeforeEach
    public void initializeTasks() {
        super.initializeTasks();
    }

    @Override
    public TaskManager getTaskManager() {
        return Managers.getDefault();
    }

    @Test
    public void createTask() {
        final Optional<Task> optionalTask = taskManager.getTaskById(taskId);

        if (optionalTask.isPresent()) {
            assertNotNull(optionalTask.get(), "Task not found");
            assertEquals(task, optionalTask.get(), "Task is not equals");
        }

        final List<Task> tasks = taskManager.getAllTasks();

        assertNotNull(task, "Tasks not returned");
        assertEquals(1, tasks.size(), "Invalid number of tasks");
        assertEquals(task, tasks.get(0), "Tasks does not match");
    }

    @Test
    public void createEpic() {
        final Optional<Epic> optionalEpic = taskManager.getEpicById(epicId);

        if (optionalEpic.isPresent()) {
            assertNotNull(optionalEpic.get(), "Task not found");
            assertEquals(epic, optionalEpic.get(), "Task is not equals");
        }

        final List<Epic> epics = taskManager.getAllEpics();

        assertNotNull(epic, "Epics not returned");
        assertEquals(1, epics.size(), "Invalid number of epics");
        assertEquals(epic, epics.get(0), "Epics does not match");
    }

    @Test
    public void createSubTasks() {
        final Optional<SubTask> optionalSubTask = taskManager.getSubTaskById(firstSubTaskId);

        if (optionalSubTask.isPresent()) {
            assertNotNull(optionalSubTask.get(), "Task not found");
            assertEquals(firstSubTask, optionalSubTask.get(), "Task is not equals");
        }

        final List<SubTask> subTasks = taskManager.getAllSubTasks();

        assertNotNull(firstSubTask, "SubTasks not returned");
        assertEquals(3, subTasks.size(), "Invalid number of SubTasks");
        assertTrue(subTasks.contains(firstSubTask), "SubTasks does not match");
    }


    @Test
    public void createTasksWithIllegalArguments() {
        SubTask subTask = new SubTask(50, "Test createSubTask",
                "Test createSubTask description", State.NEW);
        final Integer subTaskId = taskManager.createSubTask(subTask);
        subTask.setEpicID(subTaskId);

        final Optional<SubTask> optionalSubTask = taskManager.getSubTaskById(subTaskId);

        optionalSubTask.ifPresent(value -> assertNotEquals(value, null,
                "Instance of subTask must be null"));
    }


    @Test
    public void getTasks() {
        List<Task> allTasks = taskManager.getAllTasks();
        List<Epic> allEpics = taskManager.getAllEpics();
        List<SubTask> allSubTasks = taskManager.getAllSubTasks();

        assertEquals(singletonList(task), allTasks);
        assertEquals(singletonList(epic), allEpics);
        assertArrayEquals(Stream.of(firstSubTask, secondSubTask, thirdSubTask).sorted(comparatorById).toArray(),
                allSubTasks.stream().sorted(comparatorById).toArray());
    }

    @Test
    public void historyManagerTest() {
        taskManager.getTaskById(taskId);
        taskManager.getEpicById(epicId);
        taskManager.getSubTaskById(firstSubTaskId);

        List<Task> history = taskManager.getHistoryManager();
        assertEquals(3, history.size());
        assertEquals(task, history.get(0));
        assertEquals(epic, history.get(1));
        assertEquals(firstSubTask, history.get(2));

        Task updatedTask = new Task(taskId, "Test updateTask", "Test updateTask description",
                State.DONE, null, null);
        Epic updatedEpic = new Epic(epicId, "Test updateEpic", "Test updateEpic description");
        SubTask updatedSubTask = new SubTask(firstSubTaskId, 1, "Test updateSubTask",
                "Test updateSubTask description", State.DONE, null, null);

        taskManager.updateTask(updatedTask);
        taskManager.updateEpic(updatedEpic);
        taskManager.updateSubTask(updatedSubTask);

        taskManager.getTaskById(taskId);
        taskManager.getEpicById(epicId);
        taskManager.getSubTaskById(firstSubTaskId);

        assertEquals(3, history.size());
        assertEquals(task, history.get(0));
        assertEquals(epic, history.get(1));
        assertEquals(firstSubTask, history.get(2));
        assertNotSame(task.getState(), updatedTask.getState());
        assertNotSame(epic.getState(), updatedEpic.getState());
        assertNotSame(firstSubTask.getState(), updatedSubTask.getState());
    }

    @Test
    public void removeTaskFromManagerAndFromHistory() {
        Optional<Task> optionalTask = taskManager.getTaskById(taskId);

        optionalTask.ifPresent(value -> assertEquals(value, task));

        List<Task> historyManager = taskManager.getHistoryManager();

        assertArrayEquals(historyManager.toArray(), taskManager.getAllTasks().toArray());
        assertEquals(taskManager.getAllTasks().size(), historyManager.size());

        taskManager.removeAllTasks();
        historyManager = taskManager.getHistoryManager();

        assertEquals(taskManager.getAllTasks().size(), historyManager.size());
    }

    @Test
    public void removeEpicFromManagerAndFromHistory() {
        Optional<Epic> optionalEpic = taskManager.getEpicById(epicId);

        optionalEpic.ifPresent(value -> assertEquals(value, epic));

        List<Task> historyManager = taskManager.getHistoryManager();

        assertArrayEquals(historyManager.toArray(), taskManager.getAllEpics().toArray());
        assertEquals(taskManager.getAllEpics().size(), historyManager.size());

        assertArrayEquals(taskManager.getAllSubTasksFromEpic(epic).stream().sorted(comparatorById).toArray(),
                taskManager.getAllSubTasks().stream().sorted(comparatorById).toArray());

        taskManager.removeAllEpics();
        historyManager = taskManager.getHistoryManager();

        assertEquals(taskManager.getAllEpics().size(), historyManager.size());
    }

    @Test
    public void removeSubTaskFromManagerAndFromHistory() {
        Optional<SubTask> firstOptionalSubTask = taskManager.getSubTaskById(firstSubTaskId);
        Optional<SubTask> secondOptionalSubTask = taskManager.getSubTaskById(secondSubTaskId);
        Optional<SubTask> thirdOptionalSubTask = taskManager.getSubTaskById(thirdSubTaskId);

        firstOptionalSubTask.ifPresent(value -> assertEquals(value, firstSubTask));
        secondOptionalSubTask.ifPresent(value -> assertEquals(value, secondSubTask));
        thirdOptionalSubTask.ifPresent(value -> assertEquals(value, thirdSubTask));


        List<Task> historyManager = taskManager.getHistoryManager();

        assertArrayEquals(historyManager.stream().sorted(comparatorById).toArray(),
                taskManager.getAllSubTasks().stream().sorted(comparatorById).toArray());

        assertEquals(taskManager.getAllSubTasks().size(), historyManager.size());

        taskManager.removeAllSubTasks();
        historyManager = taskManager.getHistoryManager();

        assertEquals(taskManager.getAllSubTasks().size(), historyManager.size());
    }

    @Test
    public void endlessHistoryManagerTest() {
        taskManager.getTaskById(taskId);
        taskManager.getEpicById(epicId);
        taskManager.getSubTaskById(firstSubTaskId);

        List<Task> historyManager = taskManager.getHistoryManager();

        assertEquals(3, historyManager.size());
        assertEquals(historyManager.get(0), task);
        assertEquals(historyManager.get(1), epic);
        assertEquals(historyManager.get(2), firstSubTask);

        Optional<Epic> optionalEpic = taskManager.getEpicById(epicId);
        Optional<Task> optionalTask = taskManager.getTaskById(taskId);

        optionalEpic.ifPresent(value -> epic = value);
        optionalTask.ifPresent(value -> task = value);

        historyManager = taskManager.getHistoryManager();

        assertEquals(3, historyManager.size());
        assertEquals(historyManager.get(0), firstSubTask);
        assertEquals(historyManager.get(1), epic);
        assertEquals(historyManager.get(2), task);
    }
}