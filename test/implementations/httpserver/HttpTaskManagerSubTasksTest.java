package implementations.httpserver;

import com.google.gson.Gson;
import implementations.dispatchers.InMemoryTaskManager;
import implementations.httpserver.handlers.BaseHttpHandler;
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
import static org.junit.jupiter.api.Assertions.assertEquals;

public class HttpTaskManagerSubTasksTest {
    Supplier<TaskManager> taskManagerSupplier = InMemoryTaskManager::new;
    HttpTaskServer httpTaskServer = new HttpTaskServer(taskManagerSupplier);
    TaskManager taskManager = httpTaskServer.getTaskManager();
    Gson gson = BaseHttpHandler.getGson();

    public HttpTaskManagerSubTasksTest() throws IOException {
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
    public void testAddSubTask() throws IOException, InterruptedException {
        Epic epic = new Epic("Epic", "Description epic");
        taskManager.createEpic(epic);

        SubTask subTask = new SubTask(epic.getId(), "Test create subTask",
                "Description testing create subTask", State.NEW, LocalDateTime.now(), 10L);
        String subTaskJson = gson.toJson(subTask, SubTask.class);

        HttpClient client = HttpClient.newHttpClient();
        URI uri = URI.create("http://localhost:8080/subtasks");
        HttpRequest request = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(subTaskJson)).uri(uri).build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode());

        List<SubTask> subTasksFromManager = taskManager.getAllSubTasks();

        assertNotNull(subTasksFromManager, "Subtasks don't return");
        assertEquals(1, subTasksFromManager.size());
        assertEquals("Test create subTask", subTasksFromManager.get(0).getName());
    }

    @Test
    public void testAddSubTaskOverlap() throws IOException, InterruptedException {
        Epic epic = new Epic("Epic", "Description epic");
        taskManager.createEpic(epic);
        Task firstSubTask = new SubTask(epic.getId(), "First SubTask",
                "Description first SubTask", State.NEW, LocalDateTime.now(), 10L);
        Task secondSubTaskWithOverlap = new SubTask(epic.getId(), "Second task",
                "Description second task", State.NEW, firstSubTask.getStartTime().plusMinutes(1),
                10L);
        String firstSubTaskJson = gson.toJson(firstSubTask, SubTask.class);
        String secondSubTaskJson = gson.toJson(secondSubTaskWithOverlap, SubTask.class);

        HttpClient client = HttpClient.newHttpClient();
        URI uri = URI.create("http://localhost:8080/subtasks");
        HttpRequest request = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(firstSubTaskJson)).uri(uri).build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode());

        request = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(secondSubTaskJson)).uri(uri).build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(406, response.statusCode());
    }

    @Test
    public void testUpdateSubTask() throws IOException, InterruptedException {
        Epic epic = new Epic("Epic", "Description epic");
        taskManager.createEpic(epic);

        SubTask subTask = new SubTask(epic.getId(), "SubTask before updated",
                "Description subTask before updated", State.NEW, LocalDateTime.now(), 10L);
        String subTaskJson = gson.toJson(subTask, SubTask.class);

        HttpClient client = HttpClient.newHttpClient();
        URI uri = URI.create("http://localhost:8080/subtasks");
        HttpRequest request = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(subTaskJson)).uri(uri).build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode());

        Integer createdSubTaskId = taskManager.getAllSubTasks().get(0).getId();

        Task updatedSubTask = new SubTask(createdSubTaskId, epic.getId(), "SubTask after updated",
                "Description subTask after updated", State.DONE, LocalDateTime.now(), 10L);
        subTaskJson = gson.toJson(updatedSubTask, SubTask.class);

        request = HttpRequest.newBuilder().POST(HttpRequest.BodyPublishers.ofString(subTaskJson)).uri(uri).build();

        response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode());

        List<SubTask> tasksFromManager = taskManager.getAllSubTasks();

        assertNotNull(tasksFromManager, "Subtasks don't return");
        assertEquals(1, tasksFromManager.size());
        assertEquals("SubTask after updated", tasksFromManager.get(0).getName());
    }

    @Test
    public void testUpdateSubTaskWithOverlap() throws IOException, InterruptedException {
        Epic epic = new Epic("Epic", "Description epic");
        taskManager.createEpic(epic);

        SubTask firstSubTask = new SubTask(epic.getId(), "First subTask", "Description first subTask",
                State.NEW, LocalDateTime.now(), 10L);
        SubTask secondSubTask = new SubTask(epic.getId(), "Second subTask",
                "Description second subTask", State.NEW, LocalDateTime.now().plusHours(1), 10L);
        String firstSubTaskJson = gson.toJson(firstSubTask, SubTask.class);
        String secondSubTaskJson = gson.toJson(secondSubTask, SubTask.class);

        HttpClient client = HttpClient.newHttpClient();
        URI uri = URI.create("http://localhost:8080/subtasks");
        HttpRequest request = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(firstSubTaskJson)).uri(uri).build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode());

        request = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(secondSubTaskJson)).uri(uri).build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode());

        Task updatedFirstSubTask = new SubTask(firstSubTask.getId(), epic.getId(), "Updated first subTask",
                "Description updated first subTask", State.DONE,
                firstSubTask.getStartTime().plusHours(1), 10L);
        String updatedFirstSubTaskJson = gson.toJson(updatedFirstSubTask, SubTask.class);

        request = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(updatedFirstSubTaskJson)).uri(uri).build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        List<SubTask> tasksFromManager = taskManager.getAllSubTasks();

        assertEquals(406, response.statusCode());
        assertNotEquals(updatedFirstSubTask.getName(), tasksFromManager.get(0).getName());
        assertNotEquals(updatedFirstSubTask.getName(), tasksFromManager.get(1).getName());
    }

    @Test
    public void testGetAllSubTasks() throws IOException, InterruptedException {
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
        URI uri = URI.create("http://localhost:8080/subtasks");
        HttpRequest request = HttpRequest.newBuilder()
                .GET().uri(uri).build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        List<SubTask> subTasksFromJson = gson.fromJson(response.body(), new SubTaskListTypeToken().getType());
        List<SubTask> subTasksFromManager = taskManager.getAllSubTasks();

        assertEquals(subTasksFromJson, subTasksFromManager);
    }

    @Test
    public void testGetSubTaskById() throws IOException, InterruptedException {
        Epic epic = new Epic("Epic", "Description epic");
        taskManager.createEpic(epic);

        SubTask firstSubTask = new SubTask(epic.getId(), "First subTask", "Description first subTask",
                State.NEW, LocalDateTime.now(), 10L);
        SubTask secondSubTask = new SubTask(epic.getId(), "Second subTask",
                "Description second subTask", State.NEW, LocalDateTime.now().plusHours(1), 10L);
        SubTask thirdSubTask = new SubTask(epic.getId(), "Third subTask", "Description third subTask",
                State.NEW, LocalDateTime.now().plusHours(2), 10L);

        taskManager.createSubTask(firstSubTask);
        taskManager.createSubTask(secondSubTask);
        taskManager.createSubTask(thirdSubTask);

        HttpClient client = HttpClient.newHttpClient();
        URI uri = URI.create("http://localhost:8080/subtasks/" + secondSubTask.getId().toString());
        HttpRequest request = HttpRequest.newBuilder()
                .GET().uri(uri).build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        Task secondSubTaskFromJson = gson.fromJson(response.body(), SubTask.class);

        assertEquals(secondSubTaskFromJson, secondSubTask);
    }

    @Test
    public void testGetSubTaskByIdWhenTaskNotFound() throws IOException, InterruptedException {
        Epic epic = new Epic("Epic", "Description epic");
        taskManager.createEpic(epic);

        SubTask firstSubTask = new SubTask(epic.getId(), "First subTask", "Description first subTask",
                State.NEW, LocalDateTime.now(), 10L);

        taskManager.createSubTask(firstSubTask);

        HttpClient client = HttpClient.newHttpClient();
        URI uri = URI.create(String.format("http://localhost:8080/subtasks/%d", firstSubTask.getId() + 1));
        HttpRequest request = HttpRequest.newBuilder()
                .GET().uri(uri).build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        Task secondSubTaskFromJson = gson.fromJson(response.body(), SubTask.class);

        assertNotEquals(secondSubTaskFromJson, firstSubTask);
        assertEquals(404, response.statusCode());
    }

    @Test
    public void testDeleteSubTask() throws IOException, InterruptedException {
        Epic epic = new Epic("Epic", "Description epic");
        taskManager.createEpic(epic);

        SubTask firstSubTask = new SubTask(epic.getId(), "First subTask", "Description first subTask",
                State.NEW, LocalDateTime.now(), 10L);
        SubTask secondSubTask = new SubTask(epic.getId(), "Second subTask",
                "Description second subTask", State.NEW, LocalDateTime.now().plusHours(1), 10L);
        SubTask thirdSubTask = new SubTask(epic.getId(), "Third subTask", "Description third subTask",
                State.NEW, LocalDateTime.now().plusHours(2), 10L);

        taskManager.createSubTask(firstSubTask);
        taskManager.createSubTask(secondSubTask);
        taskManager.createSubTask(thirdSubTask);

        String firstSubTaskJson = gson.toJson(firstSubTask, SubTask.class);

        assertEquals(3, taskManager.getAllSubTasks().size());

        HttpClient client = HttpClient.newHttpClient();
        URI uri = URI.create("http://localhost:8080/subtasks/");
        HttpRequest request = HttpRequest.newBuilder()
                .method("DELETE", HttpRequest.BodyPublishers.ofString(firstSubTaskJson)).uri(uri).build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertEquals(2, taskManager.getAllSubTasks().size());

        String secondSubTaskJson = gson.toJson(secondSubTask, SubTask.class);

        request = HttpRequest.newBuilder()
                .method("DELETE", HttpRequest.BodyPublishers.ofString(secondSubTaskJson)).uri(uri).build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertEquals(1, taskManager.getAllSubTasks().size());

        String thirdSubTaskJson = gson.toJson(thirdSubTask, SubTask.class);

        request = HttpRequest.newBuilder()
                .method("DELETE", HttpRequest.BodyPublishers.ofString(thirdSubTaskJson)).uri(uri).build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertEquals(0, taskManager.getAllSubTasks().size());
    }
}