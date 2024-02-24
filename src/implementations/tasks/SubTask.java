package implementations.tasks;

import implementations.utility.State;

public class SubTask extends Task {
    private int epicID;

    public SubTask(Integer epicId, String name, String description, State state) {
        super(name, description, state);
        setEpicID(epicId);
    }

    public SubTask(Integer id, Integer epicId, String name, String description, State state) {
        super(id, name, description, state);
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

        return super.toString() + " tasks.SubTask{" +
                "epicID=" + epicID +
                '}';
    }
}