package implementations.httpserver;

import com.google.gson.Gson;
import implementations.dispatchers.InMemoryTaskManager;
import implementations.httpserver.handlers.BaseHttpHandler;
import implementations.httpserver.handlers.typetokens.EpicListTypeToken;
import implementations.httpserver.handlers.typetokens.SubTaskListTypeToken;
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

import static org.junit.jupiter.api.Assertions.*;

public class HttpTaskManagerEpicTest {
    Supplier<TaskManager> taskManagerSupplier = InMemoryTaskManager::new;
    HttpTaskServer httpTaskServer = new HttpTaskServer(taskManagerSupplier);
    TaskManager taskManager = httpTaskServer.getTaskManager();
    Gson gson = BaseHttpHandler.getGson();

    public HttpTaskManagerEpicTest() throws IOException {
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
    public void testCreateEpic() throws IOException, InterruptedException {
        Epic epic = new Epic("Test create epic", "Description testing create task");
        String epicJson = gson.toJson(epic, Epic.class);

        HttpClient client = HttpClient.newHttpClient();
        URI uri = URI.create("http://localhost:8080/epics");
        HttpRequest request = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(epicJson)).uri(uri).build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode());

        List<Epic> epicsFromManager = taskManager.getAllEpics();

        assertNotNull(epicsFromManager, "Epics don't return");
        assertEquals(1, epicsFromManager.size());
        assertEquals("Test create epic", epicsFromManager.get(0).getName());
        assertEquals(201, response.statusCode());
    }

    @Test
    public void testGetAllEpics() throws IOException, InterruptedException {
        Epic firstEpic = new Epic("First epic", "Description first epic");
        Epic secondEpic = new Epic("Second epic", "Description second epic");
        Epic thirdEpic = new Epic("Third epic", "Description third epic");

        taskManager.createEpic(firstEpic);
        taskManager.createEpic(secondEpic);
        taskManager.createEpic(thirdEpic);

        HttpClient client = HttpClient.newHttpClient();
        URI uri = URI.create("http://localhost:8080/epics");
        HttpRequest request = HttpRequest.newBuilder()
                .GET().uri(uri).build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        List<Epic> epicsFromJson = gson.fromJson(response.body(), new EpicListTypeToken().getType());
        List<Epic> epicsFromManager = taskManager.getAllEpics();

        assertEquals(epicsFromJson, epicsFromManager);
        assertEquals(200, response.statusCode());
    }

    @Test
    public void testGetEpicById() throws IOException, InterruptedException {
        Epic firstEpic = new Epic("First epic", "Description first epic");
        Epic secondEpic = new Epic("Second epic", "Description second epic");
        Epic thirdEpic = new Epic("Third epic", "Description third epic");

        taskManager.createEpic(firstEpic);
        taskManager.createEpic(secondEpic);
        taskManager.createEpic(thirdEpic);

        HttpClient client = HttpClient.newHttpClient();
        URI uri = URI.create("http://localhost:8080/epics/" + secondEpic.getId().toString());
        HttpRequest request = HttpRequest.newBuilder()
                .GET().uri(uri).build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        Task secondEpicFromJson = gson.fromJson(response.body(), Epic.class);

        assertEquals(secondEpicFromJson, secondEpic);
        assertEquals(200, response.statusCode());
    }

    @Test
    public void testGetTaskByIdWhenTaskNotFound() throws IOException, InterruptedException {
        Epic firstEpic = new Epic("First epic", "Description first epic");

        taskManager.createEpic(firstEpic);

        HttpClient client = HttpClient.newHttpClient();
        URI uri = URI.create(String.format("http://localhost:8080/epics/%d", firstEpic.getId() + 1));
        HttpRequest request = HttpRequest.newBuilder()
                .GET().uri(uri).build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        Task secondEpicFromJson = gson.fromJson(response.body(), Epic.class);

        assertNotEquals(secondEpicFromJson, firstEpic);
        assertEquals(404, response.statusCode());
    }

    @Test
    public void testGetEpicSubtasks() throws IOException, InterruptedException {
        Epic epic = new Epic("Epic", "Description epic");

        taskManager.createEpic(epic);

        SubTask firstTask = new SubTask(epic.getId(), "First subTask", "Description first subTask",
                State.NEW, LocalDateTime.now(), 10L);
        SubTask secondTask = new SubTask(epic.getId(), "Second subTask", "Description second subTask",
                State.NEW, LocalDateTime.now().plusHours(1), 10L);
        SubTask thirdTask = new SubTask(epic.getId(), "Third subTask", "Description third subTask",
                State.NEW, LocalDateTime.now().plusHours(2), 10L);

        taskManager.createSubTask(firstTask);
        taskManager.createSubTask(secondTask);
        taskManager.createSubTask(thirdTask);

        HttpClient client = HttpClient.newHttpClient();
        URI uri = URI.create(String.format("http://localhost:8080/epics/%d/subtasks", epic.getId()));
        HttpRequest request = HttpRequest.newBuilder()
                .GET().uri(uri).build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        List<SubTask> subTasksFromEpicFromJson = gson.fromJson(response.body(), new SubTaskListTypeToken().getType());
        List<SubTask> subTasksFromEpicFromManager = taskManager.getAllSubTasksFromEpic(epic);

        assertEquals(subTasksFromEpicFromManager, subTasksFromEpicFromJson);
        assertEquals(200, response.statusCode());
    }

    @Test
    public void testGetEpicSubtasksWhenEpicNotFound() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI uri = URI.create(String.format("http://localhost:8080/epics/%d/subtasks", 100));
        HttpRequest request = HttpRequest.newBuilder()
                .GET().uri(uri).build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode());
    }

    @Test
    public void testDeleteEpic() throws IOException, InterruptedException {
        Epic firstEpic = new Epic("First epic", "Description firstEpic");
        Epic secondEpic = new Epic("Second epic", "Description secondEpic");
        Epic thridEpic = new Epic("Third epic", "Description thirdEpic");

        taskManager.createEpic(firstEpic);
        taskManager.createEpic(secondEpic);
        taskManager.createEpic(thridEpic);

        String firstEpicJson = gson.toJson(firstEpic, Epic.class);

        assertEquals(3, taskManager.getAllEpics().size());

        HttpClient client = HttpClient.newHttpClient();
        URI uri = URI.create("http://localhost:8080/epics/");
        HttpRequest request = HttpRequest.newBuilder()
                .method("DELETE", HttpRequest.BodyPublishers.ofString(firstEpicJson)).uri(uri).build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertEquals(2, taskManager.getAllEpics().size());

        String secondEpicJson = gson.toJson(secondEpic, Epic.class);

        request = HttpRequest.newBuilder()
                .method("DELETE", HttpRequest.BodyPublishers.ofString(secondEpicJson)).uri(uri).build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertEquals(1, taskManager.getAllEpics().size());

        String thirdEpicJson = gson.toJson(thridEpic, Epic.class);

        request = HttpRequest.newBuilder()
                .method("DELETE", HttpRequest.BodyPublishers.ofString(thirdEpicJson)).uri(uri).build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertEquals(0, taskManager.getAllEpics().size());
    }
}