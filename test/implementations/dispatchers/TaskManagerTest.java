package implementations.dispatchers;

import implementations.tasks.Epic;
import implementations.tasks.SubTask;
import implementations.tasks.Task;
import implementations.utility.State;
import interfaces.TaskManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public abstract class TaskManagerTest<T extends TaskManager> {
    protected T taskManager;
    protected Task task;
    protected Epic epic;
    protected SubTask firstSubTask;
    protected SubTask secondSubTask;
    protected SubTask thirdSubTask;

    @BeforeEach
    public void initializeTaskManager() {
        taskManager = getTaskManager();
    }

    public void initializeTasks() {
        task = new Task("Test createTask", "Test createTask description", State.NEW,
                LocalDateTime.of(2024, 3, 20, 18, 0),
                Duration.ofHours(1).toMinutes());
        epic = new Epic("Test createEpic", "Test createEpic description");

        taskManager.createTask(task);
        taskManager.createEpic(epic);

        firstSubTask = new SubTask(epic.getId(), "Test createFirstSubTask",
                "Test createFirstSubTask description", State.NEW,
                LocalDateTime.of(2024, 3, 20, 19, 0),
                Duration.ofHours(1).toMinutes());
        secondSubTask = new SubTask(epic.getId(), "Test createSecondSubTask",
                "Test createFirstSubTask description", State.NEW,
                LocalDateTime.of(2024, 3, 20, 20, 0),
                Duration.ofHours(1).toMinutes());
        thirdSubTask = new SubTask(epic.getId(), "Test createThirdSubTask",
                "Test createThirdSubTask description", State.NEW,
                LocalDateTime.of(2024, 3, 20, 21, 0),
                Duration.ofHours(1).toMinutes());

        taskManager.createSubTask(firstSubTask);
        taskManager.createSubTask(secondSubTask);
        taskManager.createSubTask(thirdSubTask);
    }

    @Nested
    protected class CommonTaskManagerTests {
        @BeforeEach
        public void initializeTaskManagerAndTasks() {
            TaskManagerTest.this.initializeTaskManager();
            TaskManagerTest.this.initializeTasks();
        }

        @Test
        public void epicShouldHaveCorrectStatuses() {
            assertEquals(epic.getState(), State.NEW);

            firstSubTask = new SubTask(firstSubTask.getId(), epic.getId(), "Test update firstSubTask",
                    "Test update firstSubTask description", State.DONE,
                    LocalDateTime.of(2024, 4, 20, 18, 0),
                    Duration.ofHours(1).toMinutes());
            secondSubTask = new SubTask(secondSubTask.getId(), epic.getId(), "Test update secondSubTask",
                    "Test update secondSubTask description", State.DONE,
                    LocalDateTime.of(2024, 4, 20, 19, 0),
                    Duration.ofHours(1).toMinutes());

            taskManager.updateSubTask(firstSubTask);
            taskManager.updateSubTask(secondSubTask);

            assertEquals(epic.getState(), State.IN_PROGRESS);

            thirdSubTask = new SubTask(thirdSubTask.getId(), epic.getId(), "Test update thirdSubTask",
                    "Test update thirdSubTask description", State.DONE,
                    LocalDateTime.of(2024, 4, 20, 20, 0),
                    Duration.ofHours(1).toMinutes());

            taskManager.updateSubTask(thirdSubTask);

            assertEquals(epic.getState(), State.DONE);

            firstSubTask = new SubTask(firstSubTask.getId(), epic.getId(), "Test update firstSubTask",
                    "Test update firstSubTask description", State.IN_PROGRESS,
                    LocalDateTime.of(2024, 4, 20, 18, 0),
                    Duration.ofHours(1).toMinutes());
            secondSubTask = new SubTask(secondSubTask.getId(), epic.getId(), "Test update secondSubTask",
                    "Test update secondSubTask description", State.IN_PROGRESS,
                    LocalDateTime.of(2024, 4, 20, 19, 0),
                    Duration.ofHours(1).toMinutes());
            thirdSubTask = new SubTask(thirdSubTask.getId(), epic.getId(), "Test update thirdSubTask",
                    "Test update thirdSubTask description", State.IN_PROGRESS,
                    LocalDateTime.of(2024, 4, 20, 20, 0),
                    Duration.ofHours(1).toMinutes());

            taskManager.updateSubTask(firstSubTask);
            assertEquals(epic.getState(), State.IN_PROGRESS);

            taskManager.updateSubTask(secondSubTask);
            assertEquals(epic.getState(), State.IN_PROGRESS);

            taskManager.updateSubTask(thirdSubTask);
            assertEquals(epic.getState(), State.IN_PROGRESS);
        }

        @Test
        public void subTasksShouldHaveEpicId() {
            List<SubTask> subTasks = taskManager.getAllSubTasks();

            assertTrue(subTasks.stream().map(SubTask::getEpicID).map(obj -> true).
                    reduce(Boolean::logicalAnd).orElse(false));

            SubTask subTaskWithNullEpicId =
                    new SubTask(null, "SubTask with null epic Id", "description", State.NEW);

            taskManager.createSubTask(subTaskWithNullEpicId);
            assertTrue(subTasks.stream().map(SubTask::getEpicID).map(obj -> true).
                    reduce(Boolean::logicalAnd).orElse(false));
        }

        @Test
        public void tasksShouldNotOverlap() {
            assertEquals(4, taskManager.getPrioritizedTasks().size());

            taskManager.removeAllTasks();
            taskManager.removeAllEpics();

            assertEquals(0, taskManager.getPrioritizedTasks().size());

            taskManager.createTask(task);
            taskManager.createEpic(epic);

            firstSubTask = new SubTask(firstSubTask.getId(), epic.getId(), "Create firstSubTask",
                    "Create firstSubTask description", State.NEW,
                    LocalDateTime.of(2024, 4, 20, 18, 0),
                    Duration.ofHours(1).toMinutes());

            taskManager.createSubTask(firstSubTask);

            assertEquals(2, taskManager.getPrioritizedTasks().size());
            assertEquals(1, taskManager.getAllSubTasks().size());

            secondSubTask = new SubTask(secondSubTask.getId(), epic.getId(), "Create secondSubTask with the same time",
                    "Create secondSubTask with the same time description", State.NEW,
                    LocalDateTime.of(2024, 4, 20, 18, 0),
                    Duration.ofHours(1).toMinutes());

            taskManager.createSubTask(secondSubTask);

            assertEquals(2, taskManager.getPrioritizedTasks().size());
            assertEquals(1, taskManager.getAllSubTasks().size());

            secondSubTask = new SubTask(secondSubTask.getId(), epic.getId(), "Create secondSubTask with the same time",
                    "Create secondSubTask with the same time description", State.NEW,
                    LocalDateTime.of(2024, 4, 20, 18, 59, 59),
                    Duration.ofHours(1).toMinutes());

            taskManager.createSubTask(secondSubTask);

            assertEquals(2, taskManager.getPrioritizedTasks().size());
            assertEquals(1, taskManager.getAllSubTasks().size());

            secondSubTask = new SubTask(secondSubTask.getId(), epic.getId(), "Create secondSubTask with the same time",
                    "Create secondSubTask with the same time description", State.NEW,
                    LocalDateTime.of(2024, 4, 20, 19, 0, 0),
                    Duration.ofHours(1).toMinutes());

            taskManager.createSubTask(secondSubTask);

            assertEquals(3, taskManager.getPrioritizedTasks().size());
            assertEquals(2, taskManager.getAllSubTasks().size());

            thirdSubTask = new SubTask(thirdSubTask.getId(), epic.getId(), "Test update thirdSubTask",
                    "Test update thirdSubTask description", State.NEW,
                    LocalDateTime.of(2024, 4, 20, 17, 1),
                    Duration.ofHours(1).toMinutes());

            assertEquals(3, taskManager.getPrioritizedTasks().size());
            assertEquals(2, taskManager.getAllSubTasks().size());

            thirdSubTask = new SubTask(thirdSubTask.getId(), epic.getId(), "Test update thirdSubTask",
                    "Test update thirdSubTask description", State.NEW,
                    LocalDateTime.of(2024, 4, 20, 16, 59, 59),
                    Duration.ofHours(1).toMinutes());

            taskManager.createSubTask(thirdSubTask);

            assertEquals(4, taskManager.getPrioritizedTasks().size());
            assertEquals(3, taskManager.getAllSubTasks().size());
        }
    }

    public abstract T getTaskManager();
}