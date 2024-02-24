package implementations.task_classes;

import implementations.utility.State;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TaskTest {
    @Test
    public void shouldTwoTasksEqualsIfThemIdIsEquals() {
        Task taskOne = new Task(0, "Task one", "Description one", State.NEW);
        Task taskTwo = new Task(0, "Task two", "Description two", State.IN_PROGRESS);
        assertEquals(taskOne, taskTwo);
    }
}