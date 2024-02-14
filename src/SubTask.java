public class SubTask extends Task {
    private int epicID;

    public SubTask(int epicId, String name, String description, State state) {
        super(name, description, state);
        setEpicID(epicId);
    }

    public SubTask(int id, int epicId, String name, String description, State state) {
        super(id, name, description, state);
        setEpicID(epicId);
    }

    public int getEpicID() {
        return epicID;
    }

    public void setEpicID(int epicID) {
        this.epicID = epicID;
    }

    @Override
    public String toString() {

        return super.toString() + " SubTask{" +
                "epicID=" + epicID +
                '}';
    }
}