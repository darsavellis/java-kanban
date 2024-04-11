package implementations.dispatchers;

import implementations.Main;
import implementations.tasks.Epic;
import implementations.tasks.SubTask;
import implementations.tasks.Task;
import implementations.utility.ManagerSaveException;
import implementations.utility.State;
import implementations.utility.TaskTypes;
import interfaces.HistoryManager;
import interfaces.TaskManager;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class FileBackedTaskManager extends InMemoryTaskManager {
    private static final String DELIMITER_NEW_LINE = "\n";
    private static final String DELIMITER_COMMA = ",";
    private final Path dataFile;

    public static FileBackedTaskManager loadFromFile(Path dataFile) {
        FileBackedTaskManager fileBackedTaskManager = new FileBackedTaskManager(dataFile);
        LinkedList<String> lines = new LinkedList<>();
        try (BufferedReader bufferedReader = new BufferedReader(
                new FileReader(dataFile.toString(), StandardCharsets.UTF_8))) {
            if (bufferedReader.ready()) {
                bufferedReader.readLine();
            }
            while (bufferedReader.ready()) {
                lines.add(bufferedReader.readLine());
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }

        String historyLine = lines.pollLast();

        for (String line : lines) {
            if (!line.isEmpty()) {
                fileBackedTaskManager.taskFromString(line);
            }
        }

        if (Objects.nonNull(historyLine)) {
            for (Integer element : historyFromString(historyLine)) {
                fileBackedTaskManager.getTaskById(element);
                fileBackedTaskManager.getEpicById(element);
                fileBackedTaskManager.getSubTaskById(element);
            }
        }
        return fileBackedTaskManager;
    }

    static String historyToString(HistoryManager historyManager) {
        List<String> historyList = new ArrayList<>();

        for (Task task : historyManager.getHistory()) {
            historyList.add(String.valueOf(task.getId()));
        }
        return String.join(DELIMITER_COMMA, historyList);
    }

    static List<Integer> historyFromString(String value) {
        List<Integer> history = new ArrayList<>();
        for (String element : value.split(",")) {
            history.add(Integer.parseInt(element));
        }
        return history;
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
        String[] arguments = row.split(",");
        TaskTypes type = TaskTypes.valueOf(arguments[1]);
        TASK_COUNTER = Math.max(TASK_COUNTER, Integer.parseInt(arguments[0]) + 1);

        switch (type) {
            case TASK:
                putInTaskHashMap(new Task(Integer.parseInt(arguments[0]), arguments[2], arguments[4],
                        State.valueOf(arguments[3])));
                break;
            case EPIC:
                putInEpicHashMap(new Epic(Integer.parseInt(arguments[0]), arguments[2], arguments[4]));
                break;
            case SUBTASK:
                putInSubTaskHashMap(new SubTask(Integer.valueOf(arguments[0]), Integer.valueOf(arguments[5]),
                        arguments[2], arguments[4], State.valueOf(arguments[3])));
                break;
        }
    }

    public String toString(Task task) {
        return String.format("%d,%s,%s,%s,%s", task.getId(), TaskTypes.TASK, task.getName(), task.getState(),
                task.getDescription());
    }

    public String toString(Epic epic) {
        return String.format("%d,%s,%s,%s,%s", epic.getId(), TaskTypes.EPIC, epic.getName(), epic.getState(),
                epic.getDescription());
    }

    public String toString(SubTask subTask) {
        return String.format("%d,%s,%s,%s,%s,%d", subTask.getId(), TaskTypes.SUBTASK, subTask.getName(),
                subTask.getState(), subTask.getDescription(), subTask.getEpicID());
    }

    @Override
    public String toString() {
        List<String> elements = new ArrayList<>();
        String head = "id,type,name,status,description,epic";
        elements.add(head);
        for (Task task : getAllTasks()) {
            elements.add(toString(task));
        }
        for (Epic epic : getAllEpics()) {
            elements.add(toString(epic));
        }
        for (SubTask subTask : getAllSubTasks()) {
            elements.add(toString(subTask));
        }
        return String.format("%s%n%n%s", String.join(DELIMITER_NEW_LINE, elements), historyToString(historyManager));
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
    public Task getTaskById(int id) {
        Task task = super.getTaskById(id);
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
    public Epic getEpicById(Integer id) {
        Epic epic = super.getEpicById(id);
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
    public SubTask getSubTaskById(Integer id) {
        SubTask subTask = super.getSubTaskById(id);
        save();
        return subTask;
    }

    @Override
    public void removeAllSubTasks() {
        super.removeAllSubTasks();
        save();
    }

    public static void main(String[] args) {
        TaskManager taskManager = FileBackedTaskManager.loadFromFile(Paths.get("additionFile.csv"));

        Task firstTask = new Task(
                "Сходить в магазин",
                "Сходить в магнит и купить хлеб с маслом",
                State.NEW);
        Task secondTask = new Task(
                "Практикум",
                "Доделать ТЗ 6 спринта и отправить на ревью",
                State.NEW
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
                "Поуда",
                "Помыть посуду на кухне",
                State.NEW
        );

        SubTask secondSubTask = new SubTask(
                firstEpic.getId(),
                "Пол",
                "Пропылесосить пол в комнате",
                State.NEW
        );

        SubTask thirdSubTask = new SubTask(
                firstEpic.getId(),
                "Полки",
                "Протереть пыль на полках и сложить книги",
                State.NEW
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

        System.out.println("Список задач:");
        for (Task task : taskManager.getAllTasks()) {
            System.out.println(task);
        }
        for (Epic epic : taskManager.getAllEpics()) {
            System.out.println(epic);
        }
        for (SubTask subTask : taskManager.getAllSubTasks()) {
            System.out.println(subTask);
        }

        Main.printHistory(taskManager);
    }
}