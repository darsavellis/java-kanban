package implementations.tasks;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EpicTest {
    @Test
    public void shouldTwoEpicEqualsIfThemIdIsEquals() {
        Epic epicOne = new Epic(7, "Epic one", "Description one");
        Epic epicTwo = new Epic(7, "Epic two", "Description two");
        assertEquals(epicOne, epicTwo);
    }
}