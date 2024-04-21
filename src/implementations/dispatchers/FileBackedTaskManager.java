package implementations.dispatchers;

import implementations.Main;
import implementations.tasks.Epic;
import implementations.tasks.SubTask;
import implementations.tasks.Task;
import implementations.utility.ManagerSaveException;
import implementations.utility.State;
import implementations.utility.TaskTypes;
import interfaces.HistoryManager;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class FileBackedTaskManager extends InMemoryTaskManager {
    private static final String DELIMITER_NEW_LINE = "\n";
    private static final String DELIMITER_COMMA = ",";
    private final Path dataFile;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    public static FileBackedTaskManager loadFromFile(Path dataFile) {
        FileBackedTaskManager fileBackedTaskManager = new FileBackedTaskManager(dataFile);
        LinkedList<String> readLinesFromFile = new LinkedList<>();
        try (BufferedReader bufferedReader = new BufferedReader(
                new FileReader(dataFile.toString(), StandardCharsets.UTF_8))) {
            if (bufferedReader.ready()) {
                bufferedReader.readLine();
            }
            while (bufferedReader.ready()) {
                readLinesFromFile.add(bufferedReader.readLine());
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }

        String historyLine = readLinesFromFile.pollLast();

        readLinesFromFile.stream().filter(x -> !x.isEmpty()).forEach(fileBackedTaskManager::taskFromString);

        if (Objects.nonNull(historyLine)) {
            historyFromString(historyLine).stream()
                    .peek(fileBackedTaskManager::getTaskById)
                    .peek(fileBackedTaskManager::getEpicById)
                    .forEach(fileBackedTaskManager::getSubTaskById);
        }
        return fileBackedTaskManager;
    }

    static String historyToString(HistoryManager historyManager) {
        return historyManager.getHistory().stream()
                .map(Task::getId)
                .map(String::valueOf)
                .collect(Collectors.joining(DELIMITER_COMMA));
    }

    static List<Integer> historyFromString(String value) {
        return Arrays.stream(value.split(DELIMITER_COMMA))
                .map(Integer::parseInt)
                .collect(Collectors.toList());
    }

    private FileBackedTaskManager(Path dataFile) {
        this.dataFile = dataFile;
    }

    private void save() {
        try (BufferedWriter bufferedWriter = new BufferedWriter(
                new FileWriter(dataFile.toString(), StandardCharsets.UTF_8))) {
            bufferedWriter.write(toString());
        } catch (IOException e) {
            throw new ManagerSaveException();
        }
    }

    public void taskFromString(String row) {
        String[] arguments = row.split(DELIMITER_COMMA);
        TaskTypes type = TaskTypes.valueOf(arguments[1]);
        TASK_COUNTER = Math.max(TASK_COUNTER, Integer.parseInt(arguments[0]) + 1);

        switch (type) {
            case TASK:
                putTaskInStorage(new Task(Integer.parseInt(arguments[0]), arguments[2], arguments[4],
                        State.valueOf(arguments[3]), parseStringToDate(arguments[5]), Long.parseLong(arguments[6])));
                break;
            case EPIC:
                putEpicInStorage(new Epic(Integer.parseInt(arguments[0]), arguments[2], arguments[4]));
                break;
            case SUBTASK:
                putSubTaskInStorage(new SubTask(
                        Integer.valueOf(arguments[0]), Integer.valueOf(arguments[5]), arguments[2], arguments[4],
                        State.valueOf(arguments[3]), parseStringToDate(arguments[6]), Long.parseLong(arguments[7])));
                break;
        }
    }

    public String toString(Task task) {
        return String.format("%d,%s,%s,%s,%s,%s,%s", task.getId(), TaskTypes.TASK, task.getName(), task.getState(),
                task.getDescription(), parseDateToString(task), task.getDuration());
    }

    public String toString(Epic epic) {
        return String.format("%d,%s,%s,%s,%s,%s,%s", epic.getId(), TaskTypes.EPIC, epic.getName(), epic.getState(),
                epic.getDescription(), parseDateToString(epic), epic.getDuration());
    }

    public String toString(SubTask subTask) {
        return String.format("%d,%s,%s,%s,%s,%d,%s,%s", subTask.getId(), TaskTypes.SUBTASK, subTask.getName(),
                subTask.getState(), subTask.getDescription(), subTask.getEpicID(),
                parseDateToString(subTask), subTask.getDuration());
    }

    @Override
    public String toString() {
        List<String> elements = new ArrayList<>(Collections.singletonList("id,type,name,status,description,epic"));

        getAllTasks().stream().map(this::toString).forEach(elements::add);
        getAllEpics().stream().map(this::toString).forEach(elements::add);
        getAllSubTasks().stream().map(this::toString).forEach(elements::add);

        return String.format("%s%n%n%s", String.join(DELIMITER_NEW_LINE, elements), historyToString(historyManager));
    }

    public String parseDateToString(Task task) {
        if (task.getStartTime() != null) {
            return task.getStartTime().format(formatter);
        } else {
            return "null";
        }
    }

    public LocalDateTime parseStringToDate(String date) {
        if (!date.equals("null")) {
            return LocalDateTime.parse(date, formatter);
        } else {
            return null;
        }
    }

    @Override
    public Integer createTask(Task task) {
        Integer taskId = super.createTask(task);
        save();
        return taskId;
    }

    @Override
    public boolean updateTask(Task task) {
        boolean isTaskUpdated = super.updateTask(task);
        save();
        return isTaskUpdated;
    }

    @Override
    public Optional<Task> getTaskById(int id) {
        Optional<Task> task = super.getTaskById(id);
        save();
        return task;
    }

    @Override
    public void removeAllTasks() {
        super.removeAllTasks();
        save();
    }

    @Override
    public Task removeTaskById(Integer id) {
        Task task = super.removeTaskById(id);
        save();
        return task;
    }

    @Override
    public Integer createEpic(Epic epic) {
        Integer epicId = super.createEpic(epic);
        save();
        return epicId;
    }

    @Override
    public boolean updateEpic(Epic epic) {
        boolean isEpicUpdated = super.updateEpic(epic);
        save();
        return isEpicUpdated;
    }

    @Override
    public Optional<Epic> getEpicById(Integer id) {
        Optional<Epic> epic = super.getEpicById(id);
        save();
        return epic;
    }

    @Override
    public void removeAllEpics() {
        super.removeAllEpics();
        save();
    }

    @Override
    public Epic removeEpicById(Integer id) {
        Epic epic = super.removeEpicById(id);
        save();
        return epic;
    }

    @Override
    public Integer createSubTask(SubTask subTask) {
        Integer subTaskId = super.createSubTask(subTask);
        save();
        return subTaskId;
    }

    @Override
    public boolean updateSubTask(SubTask subTask) {
        boolean isTaskUpdated = super.updateSubTask(subTask);
        save();
        return isTaskUpdated;
    }

    @Override
    public SubTask removeSubTaskById(Integer id) {
        SubTask subTask = super.removeSubTaskById(id);
        save();
        return subTask;
    }

    @Override
    public Optional<SubTask> getSubTaskById(Integer id) {
        Optional<SubTask> subTask = super.getSubTaskById(id);
        save();
        return subTask;
    }

    @Override
    public void removeAllSubTasks() {
        super.removeAllSubTasks();
        save();
    }

    public static void main(String[] args) {
        FileBackedTaskManager taskManager = FileBackedTaskManager.loadFromFile(Paths.get("additionFile.csv"));

        Random random = new Random();

        int randomYear = random.nextInt(2024);

        Task firstTask = new Task(
                "Сходить в магазин",
                "Сходить в магнит и купить хлеб с маслом",
                State.NEW,
                LocalDateTime.of(randomYear + 4, 4, 10, 18, 0),
                1L
        );
        Task secondTask = new Task(
                "Практикум",
                "Доделать ТЗ 8 спринта и отправить на ревью",
                State.NEW,
                LocalDateTime.of(randomYear + 3, 4, 11, 18, 0),
                1L
        );
        Epic firstEpic = new Epic(
                "Дела по хозяйству",
                "Разные дела по дому"
        );

        taskManager.createTask(firstTask);
        taskManager.createTask(secondTask);
        taskManager.createEpic(firstEpic);

        SubTask firstSubTask = new SubTask(
                firstEpic.getId(),
                "Посуда",
                "Помыть посуду на кухне",
                State.NEW,
                LocalDateTime.of(randomYear + 2, 4, 12, 18, 0),
                1L
        );

        SubTask secondSubTask = new SubTask(
                firstEpic.getId(),
                "Пол",
                "Пропылесосить пол в комнате",
                State.NEW,
                LocalDateTime.of(randomYear + 1, 4, 13, 18, 0),
                1L
        );

        SubTask thirdSubTask = new SubTask(
                firstEpic.getId(),
                "Полки",
                "Протереть пыль на полках и сложить книги",
                State.NEW,
                LocalDateTime.of(randomYear, 4, 14, 18, 0),
                1L
        );

        taskManager.createSubTask(firstSubTask);
        taskManager.createSubTask(secondSubTask);
        taskManager.createSubTask(thirdSubTask);

        String textForTaskQuery = "Делаем запрос к задаче: \n";
        String textForSubTaskQuery = "Делаем запрос к подзадаче: \n";
        String textForEpicQuery = "Делаем запрос к эпику: \n";

        System.out.println(textForTaskQuery + taskManager.getTaskById(firstTask.getId()));
        System.out.println(textForTaskQuery + taskManager.getTaskById(secondTask.getId()));
        System.out.println(textForSubTaskQuery + taskManager.getSubTaskById(firstSubTask.getId()));
        System.out.println(textForSubTaskQuery + taskManager.getSubTaskById(secondSubTask.getId()));
        System.out.println(textForSubTaskQuery + taskManager.getSubTaskById(thirdSubTask.getId()));
        System.out.println(textForEpicQuery + taskManager.getEpicById(firstEpic.getId()));

        System.out.println(textForTaskQuery + taskManager.getTaskById(firstTask.getId()));
        System.out.println(textForTaskQuery + taskManager.getTaskById(secondTask.getId()));

        System.out.println();

        taskManager.getAllTasks().forEach(System.out::println);
        taskManager.getAllEpics().forEach(System.out::println);
        taskManager.getAllSubTasks().forEach(System.out::println);

        Main.printHistory(taskManager);

        System.out.println("Задачи по приоритету:");
        taskManager.getPrioritizedTasks().forEach(element -> System.out.println(element.toString()));
    }
}