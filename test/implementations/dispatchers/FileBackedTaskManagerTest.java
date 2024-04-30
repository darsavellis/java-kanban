package implementations.dispatchers;

import implementations.tasks.Epic;
import implementations.tasks.SubTask;
import implementations.tasks.Task;
import implementations.utility.State;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FileBackedTaskManagerTest extends TaskManagerTest<FileBackedTaskManager> {
    private static final String HEAD = "id,type,name,status,description,epic";
    private static final String DELIMITER = "\n";
    private static final String DELIMITER_COMMA = ",";
    private static final String EMPTY_STRING = "";
    private Path temporaryFile;
    private String dataFile;

    @Override
    public FileBackedTaskManager getTaskManager() {
        try {
            temporaryFile = Files.createTempFile("data", "csv");
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
        return FileBackedTaskManager.loadFromFile(temporaryFile);
    }

    @AfterEach
    public void clearAll() throws IOException {
        Files.deleteIfExists(temporaryFile);
    }

    @Test
    public void loadAndReadAnEmptyFile() {
        Assertions.assertDoesNotThrow(() -> dataFile = Files.readString(temporaryFile),
                "Ошибка чтения файла");

        Assertions.assertEquals(EMPTY_STRING, dataFile);
    }

    @Test
    public void loadAndSaveInEmptyFile() {
        Assertions.assertDoesNotThrow(() -> dataFile = Files.readString(temporaryFile),
                "Ошибка чтения файла");

        Assertions.assertEquals(EMPTY_STRING, dataFile);

        Assertions.assertArrayEquals(taskManager.getAllTasks().toArray(), new Task[]{});
    }

    @Test
    public void saveTasks() {
        Assertions.assertDoesNotThrow(() -> dataFile = Files.readString(temporaryFile),
                "Ошибка чтения файла");

        Assertions.assertEquals(dataFile, EMPTY_STRING);

        task = new Task("Task", "Description task", State.NEW);
        taskManager.createTask(task);

        Assertions.assertDoesNotThrow(() -> dataFile = Files.readString(temporaryFile),
                "Ошибка чтения файла");

        String expectedString = String.format("%s%n%d%s%n%n", HEAD, task.getId(), ",TASK,Task,NEW,Description task,null,null");

        Assertions.assertEquals(expectedString, dataFile);

        epic = new Epic("Epic", "Description epic");
        taskManager.createEpic(epic);

        Assertions.assertDoesNotThrow(() -> dataFile = Files.readString(temporaryFile),
                "Ошибка чтения файла");

        expectedString = String.format("%s%n%d%s%n%d%s%n%n", HEAD, task.getId(), ",TASK,Task,NEW,Description task,null,null",
                epic.getId(), ",EPIC,Epic,null,Description epic,null,null");

        Assertions.assertEquals(expectedString, dataFile);

        firstSubTask = new SubTask(epic.getId(), "SubTask", "Description subtask", State.NEW);
        taskManager.createSubTask(firstSubTask);

        Assertions.assertDoesNotThrow(() -> dataFile = Files.readString(temporaryFile),
                "Ошибка чтения файла");

        expectedString = String.format("%s%n%d%s%n%d%s%n%d%s%d%s%n%n", HEAD, task.getId(),
                ",TASK,Task,NEW,Description task,null,null", epic.getId(), ",EPIC,Epic,NEW,Description epic,null,null",
                firstSubTask.getId(), ",SUBTASK,SubTask,NEW,Description subtask,", firstSubTask.getEpicID(), ",null,null");

        Assertions.assertEquals(expectedString, dataFile);
    }

    @Test
    public void historyCheck() throws IOException {
        initializeTasks();

        taskManager.getTaskById(task.getId());
        taskManager.getEpicById(epic.getId());
        taskManager.getSubTaskById(firstSubTask.getId());

        LinkedList<String> linesFromFile = new LinkedList<>(List.of(Files.readString(temporaryFile).split(DELIMITER)));

        Assertions.assertEquals(
                Stream.of(task.getId(), epic.getId(), firstSubTask.getId()).map(String::valueOf).
                        collect(Collectors.joining(DELIMITER_COMMA)),
                linesFromFile.getLast());

        taskManager.getTaskById(task.getId());
        linesFromFile = new LinkedList<>(List.of(Files.readString(temporaryFile).split(DELIMITER)));

        Assertions.assertEquals(
                Stream.of(epic.getId(), firstSubTask.getId(), task.getId()).map(String::valueOf).
                        collect(Collectors.joining(DELIMITER_COMMA)),
                linesFromFile.getLast());

        taskManager.getSubTaskById(firstSubTask.getId());
        taskManager.getEpicById(epic.getId());
        linesFromFile = new LinkedList<>(List.of(Files.readString(temporaryFile).split(DELIMITER)));

        Assertions.assertEquals(
                Stream.of(task.getId(), firstSubTask.getId(), epic.getId()).map(String::valueOf).
                        collect(Collectors.joining(DELIMITER_COMMA)),
                linesFromFile.getLast());

        taskManager.removeAllSubTasks();
        linesFromFile = new LinkedList<>(List.of(Files.readString(temporaryFile).split(DELIMITER)));

        Assertions.assertEquals(
                Stream.of(task.getId(), epic.getId()).map(String::valueOf).
                        collect(Collectors.joining(DELIMITER_COMMA)),
                linesFromFile.getLast());
    }

    @Test
    public void restoreDataFromNonEmptyFile() {
        Path dataFile = Paths.get("data.csv");
        String expectedData = String.join(DELIMITER,
                HEAD,
                "1,TASK,Task,NEW,Description task,null,null",
                "2,EPIC,Epic,DONE,Description epic,null,null",
                "3,SUBTASK,Sub Task,DONE,Description sub task,2,null,null", DELIMITER + "3,2,1");

        Assertions.assertDoesNotThrow(() -> Files.writeString(dataFile, expectedData),
                "Ошибка записи в файл");

        taskManager = FileBackedTaskManager.loadFromFile(dataFile);

        Assertions.assertEquals(taskManager.toString(), expectedData);

        Optional<Task> optionalTask = taskManager.getTaskById(1);
        optionalTask.ifPresent(value -> task = value);

        Assertions.assertEquals(1, task.getId());
        Assertions.assertEquals("Task", task.getName());
        Assertions.assertEquals("Description task", task.getDescription());
    }
}