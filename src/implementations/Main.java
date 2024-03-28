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

        Integer firstTaskId = taskManager.createTask(firstTask);
        Integer secondTaskId = taskManager.createTask(secondTask);
        Integer epicId = taskManager.createEpic(epic);
        Integer emptyEpicId = taskManager.createEpic(emptyEpic);

        SubTask firstSubTask = new SubTask(
                epicId,
                "Посуда",
                "Помыть посуду на кухне",
                State.NEW
        );

        SubTask secondSubTask = new SubTask(
                epicId,
                "Пол",
                "Пропылесосить пол в комнате",
                State.NEW
        );

        SubTask thirdSubTask = new SubTask(
                epicId,
                "Полки",
                "Протереть пыль на полках и сложить книги",
                State.NEW
        );

        Integer firstSubTaskId = taskManager.createSubTask(firstSubTask);
        Integer secondSubTaskId = taskManager.createSubTask(secondSubTask);
        Integer thirdSubTaskId = taskManager.createSubTask(thirdSubTask);

        printHistory(taskManager);

        System.out.println("Делаем запрос к задаче: \n" + taskManager.getTaskById(firstTaskId));
        System.out.println("Делаем запрос к задаче: \n" + taskManager.getTaskById(secondTaskId));
        System.out.println("Делаем запрос к подзадаче: \n" + taskManager.getSubTaskById(firstSubTaskId));
        System.out.println("Делаем запрос к подзадаче: \n" + taskManager.getSubTaskById(secondSubTaskId));
        System.out.println("Делаем запрос к подзадаче: \n" + taskManager.getSubTaskById(thirdSubTaskId));
        System.out.println("Делаем запрос к эпику: \n" + taskManager.getEpicById(epicId));
        System.out.println("Делаем запрос к эпику: \n" + taskManager.getEpicById(emptyEpicId));

        printHistory(taskManager);

        System.out.println("Делаем запрос к задаче: \n" + taskManager.getTaskById(firstTaskId));
        System.out.println("Делаем запрос к задаче: \n" + taskManager.getTaskById(secondTaskId));

        printHistory(taskManager);

        for (int i = 0; i < 5; i++) {
            System.out.println("Делаем запрос к задаче: \n" + taskManager.getTaskById(firstTaskId));
        }

        printHistory(taskManager);

        System.out.println("Удалим задачу " + taskManager.removeTaskById(secondTaskId));
        System.out.println("Удалим пустой эпик " + taskManager.removeEpicById(emptyEpicId));

        printHistory(taskManager);

        System.out.println("Удалим епик " + taskManager.removeEpicById(epicId));

        printHistory(taskManager);
    }

    private static void printHistory(TaskManager taskManager) {
        List<Task> historyManager = taskManager.getHistoryManager();

        System.out.println();
        System.out.println("История запросов:");
        for (Task task : historyManager) {
            System.out.println(task);
        }
        System.out.println();
    }
}