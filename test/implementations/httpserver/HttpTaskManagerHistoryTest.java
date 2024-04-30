package implementations.httpserver;

import com.google.gson.*;
import implementations.dispatchers.InMemoryTaskManager;
import implementations.httpserver.handlers.BaseHttpHandler;
import implementations.httpserver.handlers.typetokens.TaskListTypeToken;
import implementations.tasks.Epic;
import implementations.tasks.SubTask;
import implementations.tasks.Task;
import implementations.utility.State;
import interfaces.TaskManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class HttpTaskManagerHistoryTest {
    Supplier<TaskManager> taskManagerSupplier = InMemoryTaskManager::new;
    HttpTaskServer httpTaskServer = new HttpTaskServer(taskManagerSupplier);
    TaskManager taskManager = httpTaskServer.getTaskManager();
    Gson gson = BaseHttpHandler.getGson();

    public HttpTaskManagerHistoryTest() throws IOException {
    }

    @BeforeEach
    public void setUp() {
        taskManager.removeAllTasks();
        taskManager.removeAllSubTasks();
        taskManager.removeAllEpics();
        httpTaskServer.start();
    }

    @AfterEach
    public void shutDown() {
        httpTaskServer.stop(0);
    }

    @Test
    public void testHistoryFromHttpServer() throws IOException, InterruptedException {
        Epic epic = new Epic("Epic", "Description epic");
        taskManager.createEpic(epic);

        SubTask firstSubTask = new SubTask(epic.getId(), "First subTask", "Description first subTask",
                State.NEW, LocalDateTime.now(), 10L);
        SubTask secondSubTask = new SubTask(epic.getId(), "Second subTask",
                "Description second subTask", State.NEW, LocalDateTime.now().plusHours(1), 10L);

        taskManager.createSubTask(firstSubTask);
        taskManager.createSubTask(secondSubTask);

        Task firstTask = new Task("First task", "Description first task",
                State.NEW, LocalDateTime.now().plusHours(2), 10L);
        Task secondTask = new Task("Second task", "Description second task",
                State.NEW, LocalDateTime.now().plusHours(3), 10L);
        Task thirdTask = new Task("Third task", "Description third task",
                State.NEW, LocalDateTime.now().plusHours(4), 10L);

        taskManager.createTask(firstTask);
        taskManager.createTask(secondTask);
        taskManager.createTask(thirdTask);

        taskManager.getTaskById(thirdTask.getId());
        taskManager.getTaskById(secondTask.getId());
        taskManager.getTaskById(firstTask.getId());
        taskManager.getSubTaskById(secondSubTask.getId());
        taskManager.getSubTaskById(firstSubTask.getId());
        taskManager.getEpicById(epic.getId());

        HttpClient client = HttpClient.newHttpClient();
        URI uri = URI.create("http://localhost:8080/history");
        HttpRequest request = HttpRequest.newBuilder()
                .GET().uri(uri).build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());

        List<Task> taskListFromJson = gson.fromJson(response.body(), new TaskListTypeToken().getType());
        List<Task> taskListFromManager = taskManager.getHistoryManager();
        assertEquals(taskListFromManager, taskListFromJson);
    }
}