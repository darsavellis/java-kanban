package implementations.dispatchers;

import implementations.task_classes.Epic;
import implementations.task_classes.SubTask;
import implementations.task_classes.Task;
import implementations.utility.Managers;
import implementations.utility.State;
import interfaces.TaskManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static java.util.Collections.singletonList;

class InMemoryTaskManagerTest {
    private TaskManager taskManager;
    private Task task;
    private Epic epic;
    private SubTask subTask;
    private Integer taskId;
    private Integer epicId;
    private Integer subTaskId;

    @BeforeEach
    public void createTaskManager() {
        taskManager = Managers.getDefault();
        task = new Task("Test createTask", "Test createTask description", State.NEW);
        epic = new Epic("Test createEpic", "Test createEpic description");

        taskId = taskManager.createTask(task);
        epicId = taskManager.createEpic(epic);

        subTask = new SubTask(epicId, "Test createSubTask", "Test createSubTask description", State.NEW);

        subTaskId = taskManager.createSubTask(subTask);

    }

    @Test
    public void createTask() {
        final Task savedTask = taskManager.getTaskById(taskId);

        assertNotNull(savedTask, "Task not found");
        assertEquals(task, savedTask, "Task is not equals");

        final List<Task> tasks = taskManager.getAllTasks();

        assertNotNull(task, "Tasks not returned");
        assertEquals(1, tasks.size(), "Invalid number of tasks");
        assertEquals(task, tasks.get(0), "Tasks does not match");
    }

    @Test
    public void createEpic() {
        final Epic savedEpic = taskManager.getEpicById(epicId);

        assertNotNull(savedEpic, "Task not found");
        assertEquals(epic, savedEpic, "Task is not equals");

        final List<Epic> epics = taskManager.getAllEpics();

        assertNotNull(epic, "Epics not returned");
        assertEquals(1, epics.size(), "Invalid number of epics");
        assertEquals(epic, epics.get(0), "Epics does not match");
    }

    @Test
    public void createSubTasks() {
        final SubTask savedSubTask = taskManager.getSubTaskById(subTaskId);

        assertNotNull(savedSubTask, "Task not found");
        assertEquals(subTask, savedSubTask, "Task is not equals");

        final List<SubTask> subTasks = taskManager.getAllSubTasks();

        assertNotNull(subTask, "SubTasks not returned");
        assertEquals(1, subTasks.size(), "Invalid number of SubTasks");
        assertEquals(subTask, subTasks.get(0), "SubTasks does not match");
    }


    @Test
    public void createTasksWithIllegalArguments() {
        SubTask subTask = new SubTask(10, "Test createSubTask", "Test createSubTask description", State.NEW);
        final Integer subTaskId = taskManager.createSubTask(subTask);
        subTask.setEpicID(subTaskId);

        final SubTask savedSubTask = taskManager.getSubTaskById(subTaskId);
        assertNull(savedSubTask, "Instance of subTask must be null");
    }

    @Test
    public void createEpicsWithIllegalArguments() {
        Epic epic = new Epic("Test createEpic", "Test createEpic description");
        final Integer epicId = taskManager.createEpic(epic);
//        We can pass to addSubTaskId only subTask object
//        epic.addSubTaskId(epicId);
    }

    @Test
    public void getTasks() {
        List<Task> allTasks = taskManager.getAllTasks();
        List<Epic> allEpics = taskManager.getAllEpics();
        List<SubTask> allSubTasks = taskManager.getAllSubTasks();
        assertEquals(singletonList(task), allTasks);
        assertEquals(singletonList(epic), allEpics);
        assertEquals(singletonList(subTask), allSubTasks);
    }

    @Test
    public void historyManagerTest() {
        taskManager.getTaskById(taskId);
        taskManager.getEpicById(epicId);
        taskManager.getSubTaskById(subTaskId);

        List<Task> history = taskManager.getHistoryManager();
        assertEquals(3, history.size());
        assertEquals(task, history.get(0));
        assertEquals(epic, history.get(1));
        assertEquals(subTask, history.get(2));

        Task updatedTask = new Task(taskId, "Test updateTask", "Test updateTask description", State.DONE);
        Epic updatedEpic = new Epic(epicId, "Test updateEpic", "Test updateEpic description");
        SubTask updatedSubTask = new SubTask(subTaskId, 1, "Test updateSubTask", "Test updateSubTask description", State.DONE);

        taskManager.updateTask(updatedTask);
        taskManager.updateEpic(updatedEpic);
        taskManager.updateSubTask(updatedSubTask);

        assertEquals(3, history.size());
        assertEquals(task, history.get(0));
        assertEquals(epic, history.get(1));
        assertEquals(subTask, history.get(2));
        assertNotSame(task.getState(), updatedTask.getState());
        assertNotSame(epic.getState(), updatedEpic.getState());
        assertNotSame(subTask.getState(), updatedSubTask.getState());
    }
}