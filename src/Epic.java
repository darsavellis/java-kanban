import java.util.ArrayList;
import java.util.HashMap;

public class Epic extends Task {
    private final ArrayList<Integer> subTaskArrayList = new ArrayList<>();
    private final HashMap<State, Integer> stateStatistics = new HashMap<>();

    {
        stateStatistics.put(State.NEW, 0);
        stateStatistics.put(State.IN_PROGRESS, 0);
        stateStatistics.put(State.DONE, 0);
    }

    public Epic(int id, String name, String description) {
        super(id, name, description, State.NEW);
    }

    public Epic(String name, String description) {
        super(name, description, State.NEW);
    }

    public void addSubTaskId(SubTask subTask) {
        subTaskArrayList.add(subTask.getId());
        updateStatistics(subTask, "add");
    }

    public void removeSubTaskId(SubTask subTask) {
        subTaskArrayList.remove(Integer.valueOf(subTask.getId()));
        updateStatistics(subTask, "sub");
    }

    private void updateStatistics(SubTask subTask, String action) {
        Integer count = stateStatistics.get(subTask.getState());
        count = action.equals("add") ? count + 1 : count - 1;
        stateStatistics.put(subTask.getState(), count);
        updateState();
    }

    private void updateState() {
        if (stateStatistics.get(State.NEW) == subTaskArrayList.size()) {
            setState(State.NEW);
        } else if (stateStatistics.get(State.DONE) == subTaskArrayList.size()) {
            setState(State.DONE);
        } else {
            setState(State.IN_PROGRESS);
        }
    }

    public ArrayList<Integer> getSubTaskArrayList() {
        return subTaskArrayList;
    }

    @Override
    public String toString() {
        return super.toString() + " Epic{" +
                "subTasksArrayList=" + subTaskArrayList +
                ", stateStatistics=" + stateStatistics +
                '}';
    }
}