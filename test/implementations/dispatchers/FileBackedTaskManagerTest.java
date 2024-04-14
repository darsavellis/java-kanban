package implementations.dispatchers;

import implementations.tasks.Epic;
import implementations.tasks.SubTask;
import implementations.tasks.Task;
import implementations.utility.State;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;

public class FileBackedTaskManagerTest {
    private static final String HEAD = "id,type,name,status,description,epic";
    private static final String DELIMITER = "\n";
    private Path temporaryFile;
    private FileBackedTaskManager fileBackedTaskManager;

    @BeforeEach
    public void createTemporaryFile() throws IOException {
        temporaryFile = Files.createTempFile("data", "csv");
        fileBackedTaskManager = FileBackedTaskManager.loadFromFile(temporaryFile);
    }

    @AfterEach
    public void clearAll() throws IOException {
        Files.deleteIfExists(temporaryFile);
    }

    @Test
    public void loadAndReadAnEmptyFile() throws IOException {
        String dataFile = Files.readString(temporaryFile);
        Assertions.assertEquals("", dataFile);
    }

    @Test
    public void loadAndSaveInEmptyFile() throws IOException {
        String dataFile = Files.readString(temporaryFile);
        Assertions.assertEquals(dataFile, "");
        Assertions.assertArrayEquals(fileBackedTaskManager.getAllTasks().toArray(), new Task[]{});
    }

    @Test
    public void saveTasks() throws IOException {
        String dataFile = Files.readString(temporaryFile);

        Assertions.assertEquals("", dataFile);

        Task task = new Task("Task", "Description task", State.NEW);
        fileBackedTaskManager.createTask(task);
        dataFile = Files.readString(temporaryFile);
        String expectedString = String.format("%s%n%d%s%n%n", HEAD, task.getId(), ",TASK,Task,NEW,Description task");

        Assertions.assertEquals(expectedString, dataFile);

        Epic epic = new Epic("Epic", "Description epic");
        fileBackedTaskManager.createEpic(epic);
        dataFile = Files.readString(temporaryFile);
        expectedString = String.format("%s%n%d%s%n%d%s%n%n", HEAD, task.getId(), ",TASK,Task,NEW,Description task",
                epic.getId(), ",EPIC,Epic,NEW,Description epic");

        Assertions.assertEquals(expectedString, dataFile);

        SubTask subTask = new SubTask(epic.getId(), "SubTask", "Description subtask", State.NEW);
        fileBackedTaskManager.createSubTask(subTask);
        dataFile = Files.readString(temporaryFile);
        expectedString = String.format("%s%n%d%s%n%d%s%n%d%s%d%n%n", HEAD, task.getId(),
                ",TASK,Task,NEW,Description task", epic.getId(), ",EPIC,Epic,NEW,Description epic",
                subTask.getId(), ",SUBTASK,SubTask,NEW,Description subtask,", subTask.getEpicID());

        Assertions.assertEquals(expectedString, dataFile);
    }

    @Test
    public void historyCheck() throws IOException {
        Task task = new Task("Task", "Description task", State.NEW);
        fileBackedTaskManager.createTask(task);
        Epic epic = new Epic("Epic", "Description epic");
        fileBackedTaskManager.createEpic(epic);
        SubTask subTask = new SubTask(epic.getId(), "SubTask", "Description subtask", State.DONE);
        fileBackedTaskManager.createSubTask(subTask);

        fileBackedTaskManager.getTaskById(task.getId());
        fileBackedTaskManager.getEpicById(epic.getId());
        fileBackedTaskManager.getSubTaskById(subTask.getId());

        LinkedList<String> linesFromFile = new LinkedList<>(List.of(Files.readString(temporaryFile).split(DELIMITER)));

        Assertions.assertEquals("0,1,2", linesFromFile.getLast());

        fileBackedTaskManager.getTaskById(task.getId());
        linesFromFile = new LinkedList<>(List.of(Files.readString(temporaryFile).split(DELIMITER)));

        Assertions.assertEquals("1,2,0", linesFromFile.getLast());

        fileBackedTaskManager.getSubTaskById(subTask.getId());
        fileBackedTaskManager.getEpicById(epic.getId());
        linesFromFile = new LinkedList<>(List.of(Files.readString(temporaryFile).split(DELIMITER)));

        Assertions.assertEquals("0,2,1", linesFromFile.getLast());

        fileBackedTaskManager.removeAllSubTasks();
        linesFromFile = new LinkedList<>(List.of(Files.readString(temporaryFile).split(DELIMITER)));

        Assertions.assertEquals("0,1", linesFromFile.getLast());
    }

    @Test
    public void restoreDataFromNonEmptyFile() {
        fileBackedTaskManager = FileBackedTaskManager.loadFromFile(Paths.get("data.csv"));
        String expectedData = String.join(DELIMITER,
                "id,type,name,status,description,epic",
                "1,TASK,Task,NEW,Description task",
                "2,EPIC,Epic,DONE,Description epic",
                "3,SUBTASK,Sub Task,DONE,Description sub task,2", DELIMITER + "3,2,1");
        Assertions.assertEquals(fileBackedTaskManager.toString(), expectedData);

        Task task = fileBackedTaskManager.getTaskById(1);

        Assertions.assertEquals(1, task.getId());
        Assertions.assertEquals("Task", task.getName());
        Assertions.assertEquals("Description task", task.getDescription());
    }
}