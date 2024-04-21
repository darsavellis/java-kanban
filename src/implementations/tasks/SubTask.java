package implementations.tasks;

import implementations.utility.State;

import java.time.LocalDateTime;

public class SubTask extends Task {
    private int epicID;

    public SubTask(Integer epicId, String name, String description, State state, LocalDateTime startTime,
                   Long duration) {
        super(name, description, state, startTime, duration);
        setEpicID(epicId);
    }

    public SubTask(Integer epicId, String name, String description, State state) {
        super(name, description, state, null, null);
        setEpicID(epicId);
    }

    public SubTask(Integer id, Integer epicId, String name, String description, State state, LocalDateTime startTime,
                   Long duration) {
        super(id, name, description, state, startTime, duration);
        setEpicID(epicId);
    }

    public int getEpicID() {
        return epicID;
    }

    public void setEpicID(Integer epicID) {
        if (epicID != null) {
            this.epicID = epicID;
        }
    }

    @Override
    public String toString() {
        return super.toString() + "SubTask{" +
                "epicID=" + epicID +
                '}';
    }
}