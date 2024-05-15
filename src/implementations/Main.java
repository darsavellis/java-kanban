package implementations;

import implementations.dispatchers.InMemoryTaskManager;
import implementations.tasks.Epic;
import implementations.tasks.SubTask;
import implementations.tasks.Task;
import implementations.utility.State;
import interfaces.TaskManager;

import java.util.List;

public class Main {
    public static void main(String[] args) {
        Task firstTask = new Task(
                "Сходить в магазин",
                "Сходить в магнит и купить хлеб с маслом",
                State.NEW);
        Task secondTask = new Task(
                "Практикум",
                "Доделать ТЗ 6 спринта и отправить на ревью",
                State.NEW
        );
        Epic epic = new Epic(
                "Дела по хозяйству",
                "Разные дела по дому"
        );
        Epic emptyEpic = new Epic(
                "Разное",
                "Разные задачи - долги по работе"
        );

        TaskManager taskManager = new InMemoryTaskManager();

        taskManager.createTask(firstTask);
        taskManager.createTask(secondTask);
        taskManager.createEpic(epic);
        taskManager.createEpic(emptyEpic);

        SubTask firstSubTask = new SubTask(
                epic.getId(),
                "Посуда",
                "Помыть посуду на кухне",
                State.NEW
        );

        SubTask secondSubTask = new SubTask(
                epic.getId(),
                "Пол",
                "Пропылесосить пол в комнате",
                State.NEW
        );

        SubTask thirdSubTask = new SubTask(
                epic.getId(),
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

        printHistory(taskManager);

        System.out.println(textForTaskQuery + taskManager.getTaskById(firstTask.getId()));
        System.out.println(textForTaskQuery + taskManager.getTaskById(secondTask.getId()));
        System.out.println(textForSubTaskQuery + taskManager.getSubTaskById(firstSubTask.getId()));
        System.out.println(textForSubTaskQuery + taskManager.getSubTaskById(secondSubTask.getId()));
        System.out.println(textForSubTaskQuery + taskManager.getSubTaskById(thirdSubTask.getId()));
        System.out.println(textForEpicQuery + taskManager.getEpicById(epic.getId()));
        System.out.println(textForEpicQuery + taskManager.getEpicById(emptyEpic.getId()));

        printHistory(taskManager);

        System.out.println(textForTaskQuery + taskManager.getTaskById(firstTask.getId()));
        System.out.println(textForTaskQuery + taskManager.getTaskById(secondTask.getId()));

        printHistory(taskManager);

        for (int i = 0; i < 5; i++) {
            System.out.println(textForTaskQuery + taskManager.getTaskById(firstTask.getId()));
        }

        printHistory(taskManager);

        System.out.println("Удалим задачу " + taskManager.removeTaskById(secondTask.getId()));
        System.out.println("Удалим пустой эпик " + taskManager.removeEpicById(emptyEpic.getId()));

        printHistory(taskManager);

        System.out.println("Удалим епик " + taskManager.removeEpicById(epic.getId()));

        printHistory(taskManager);
    }

    public static void printHistory(TaskManager taskManager) {
        List<Task> historyManager = taskManager.getHistoryManager();

        System.out.println();
        System.out.println("История запросов:");
        for (Task task : historyManager) {
            System.out.println(task);
        }
        System.out.println();
    }
}