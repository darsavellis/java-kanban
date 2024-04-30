package implementations.httpserver;

import com.google.gson.Gson;
import implementations.dispatchers.InMemoryTaskManager;
import implementations.httpserver.handlers.BaseHttpHandler;
import implementations.httpserver.handlers.typetokens.TaskListTypeToken;
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

public class HttpTaskManagerTasksTest {
    Supplier<TaskManager> taskManagerSupplier = InMemoryTaskManager::new;
    HttpTaskServer httpTaskServer = new HttpTaskServer(taskManagerSupplier);
    TaskManager taskManager = httpTaskServer.getTaskManager();
    Gson gson = BaseHttpHandler.getGson();

    public HttpTaskManagerTasksTest() throws IOException {
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
    public void testAddTask() throws IOException, InterruptedException {
        Task task = new Task("Test create task", "Description testing create task",
                State.NEW, LocalDateTime.now(), 5L);
        String taskJson = gson.toJson(task, Task.class);

        HttpClient client = HttpClient.newHttpClient();
        URI uri = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(taskJson)).uri(uri).build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode());

        List<Task> tasksFromManager = taskManager.getAllTasks();

        assertNotNull(tasksFromManager, "Задачи не возвращаются");
        assertEquals(1, tasksFromManager.size());
        assertEquals("Test create task", tasksFromManager.get(0).getName());
    }

    @Test
    public void testAddTaskOverlap() throws IOException, InterruptedException {
        Task firstTask = new Task("First task", "Description first task",
                State.NEW, LocalDateTime.now(), 5L);
        Task secondTaskWithOverlap = new Task("Second task", "Description second task",
                State.NEW, firstTask.getStartTime().plusMinutes(1), 5L);
        String firstTaskJson = gson.toJson(firstTask, Task.class);
        String secondTaskJson = gson.toJson(secondTaskWithOverlap, Task.class);

        HttpClient client = HttpClient.newHttpClient();
        URI uri = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(firstTaskJson)).uri(uri).build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode());

        request = HttpRequest.newBuilder().POST(HttpRequest.BodyPublishers.ofString(secondTaskJson)).uri(uri).build();

        response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(406, response.statusCode());
    }

    @Test
    public void testUpdateTask() throws IOException, InterruptedException {
        Task task = new Task("Task before updated", "Description task before updated",
                State.NEW, LocalDateTime.now(), 5L);
        String taskJson = gson.toJson(task, Task.class);

        HttpClient client = HttpClient.newHttpClient();
        URI uri = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(taskJson)).uri(uri).build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode());

        Integer createdTaskId = taskManager.getAllTasks().get(0).getId();

        Task updatedTask = new Task(createdTaskId, "Task after updated",
                "Description task after updated", State.DONE, LocalDateTime.now(), 5L);
        taskJson = gson.toJson(updatedTask, Task.class);

        request = HttpRequest.newBuilder().POST(HttpRequest.BodyPublishers.ofString(taskJson)).uri(uri).build();

        response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode());

        List<Task> tasksFromManager = taskManager.getAllTasks();

        assertNotNull(tasksFromManager, "Задачи не возвращаются");
        assertEquals(1, tasksFromManager.size());
        assertEquals("Task after updated", tasksFromManager.get(0).getName());
    }

    @Test
    public void testUpdateTaskWithOverlap() throws IOException, InterruptedException {
        Task firstTask = new Task("First task", "Description first task",
                State.NEW, LocalDateTime.now(), 5L);
        Task secondTask = new Task("Second task", "Description second task",
                State.NEW, LocalDateTime.now().plusHours(1), 5L);
        String firstTaskJson = gson.toJson(firstTask, Task.class);
        String secondTaskJson = gson.toJson(secondTask, Task.class);

        HttpClient client = HttpClient.newHttpClient();
        URI uri = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(firstTaskJson)).uri(uri).build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode());

        request = HttpRequest.newBuilder().POST(HttpRequest.BodyPublishers.ofString(secondTaskJson)).uri(uri).build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode());

        Task updatedFirstTask = new Task(firstTask.getId(), "Updated first task",
                "Description updated first task", State.DONE,
                firstTask.getStartTime().plusHours(1), 5L);
        String updatedFirstTaskJson = gson.toJson(updatedFirstTask, Task.class);

        request = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(updatedFirstTaskJson)).uri(uri).build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        List<Task> tasksFromManager = taskManager.getAllTasks();

        assertEquals(406, response.statusCode());
        assertNotEquals(updatedFirstTask.getName(), tasksFromManager.get(0).getName());
        assertNotEquals(updatedFirstTask.getName(), tasksFromManager.get(1).getName());
    }

    @Test
    public void testGetAllTasks() throws IOException, InterruptedException {
        Task firstTask = new Task("First task", "Description first task",
                State.NEW, LocalDateTime.now(), 10L);
        Task secondTask = new Task("Second task", "Description second task",
                State.NEW, LocalDateTime.now().plusHours(1), 10L);
        Task thirdTask = new Task("Third task", "Description third task",
                State.NEW, LocalDateTime.now().plusHours(2), 10L);

        taskManager.createTask(firstTask);
        taskManager.createTask(secondTask);
        taskManager.createTask(thirdTask);

        HttpClient client = HttpClient.newHttpClient();
        URI uri = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest.newBuilder()
                .GET().uri(uri).build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        List<Task> tasksFromJson = gson.fromJson(response.body(), new TaskListTypeToken().getType());
        List<Task> tasksFromManager = taskManager.getAllTasks();

        assertEquals(tasksFromJson, tasksFromManager);
    }

    @Test
    public void testGetTaskById() throws IOException, InterruptedException {
        Task firstTask = new Task("First task", "Description first task",
                State.NEW, LocalDateTime.now(), 10L);
        Task secondTask = new Task("Second task", "Description second task",
                State.NEW, LocalDateTime.now().plusHours(1), 10L);
        Task thirdTask = new Task("Third task", "Description third task",
                State.NEW, LocalDateTime.now().plusHours(2), 10L);

        taskManager.createTask(firstTask);
        taskManager.createTask(secondTask);
        taskManager.createTask(thirdTask);

        HttpClient client = HttpClient.newHttpClient();
        URI uri = URI.create("http://localhost:8080/tasks/" + secondTask.getId().toString());
        HttpRequest request = HttpRequest.newBuilder()
                .GET().uri(uri).build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        Task secondTaskFromJson = gson.fromJson(response.body(), Task.class);

        assertEquals(secondTaskFromJson, secondTask);
    }

    @Test
    public void testGetTaskByIdWhenTaskNotFound() throws IOException, InterruptedException {
        Task firstTask = new Task("First task", "Description first task",
                State.NEW, LocalDateTime.now(), 10L);

        taskManager.createTask(firstTask);

        HttpClient client = HttpClient.newHttpClient();
        URI uri = URI.create(String.format("http://localhost:8080/tasks/%d", firstTask.getId() + 1));
        HttpRequest request = HttpRequest.newBuilder()
                .GET().uri(uri).build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        Task secondTaskFromJson = gson.fromJson(response.body(), Task.class);

        assertNotEquals(secondTaskFromJson, firstTask);
        assertEquals(404, response.statusCode());
    }

    @Test
    public void testDeleteTask() throws IOException, InterruptedException {
        Task firstTask = new Task("First task", "Description first task",
                State.NEW, LocalDateTime.now(), 10L);
        Task secondTask = new Task("Second task", "Description second task",
                State.NEW, LocalDateTime.now().plusHours(1), 10L);
        Task thirdTask = new Task("Third task", "Description third task",
                State.NEW, LocalDateTime.now().plusHours(2), 10L);

        taskManager.createTask(firstTask);
        taskManager.createTask(secondTask);
        taskManager.createTask(thirdTask);

        String firstTaskJson = gson.toJson(firstTask, Task.class);

        assertEquals(3, taskManager.getAllTasks().size());

        HttpClient client = HttpClient.newHttpClient();
        URI uri = URI.create("http://localhost:8080/tasks/");
        HttpRequest request = HttpRequest.newBuilder()
                .method("DELETE", HttpRequest.BodyPublishers.ofString(firstTaskJson)).uri(uri).build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertEquals(2, taskManager.getAllTasks().size());

        String secondTaskJson = gson.toJson(secondTask, Task.class);

        request = HttpRequest.newBuilder()
                .method("DELETE", HttpRequest.BodyPublishers.ofString(secondTaskJson)).uri(uri).build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertEquals(1, taskManager.getAllTasks().size());

        String thirdTaskJson = gson.toJson(thirdTask, Task.class);

        request = HttpRequest.newBuilder()
                .method("DELETE", HttpRequest.BodyPublishers.ofString(thirdTaskJson)).uri(uri).build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertEquals(0, taskManager.getAllTasks().size());
    }
}