package implementations.tasks;

import implementations.utility.State;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Epic extends Task {
    private transient ArrayList<Integer> subTaskArrayList;
    private transient HashMap<State, Integer> stateStatistics;
    private transient LocalDateTime endTime;

    public Epic(Integer id, String name, String description) {
        super(id, name, description, null, null, null);
    }

    public Epic(String name, String description) {
        super(name, description, null, null, null);
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    @Override
    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void initialize() {
        setSubTaskArrayList(new ArrayList<>());
        setStateStatistics(new HashMap<>(Map.of(State.NEW, 0, State.IN_PROGRESS, 0, State.DONE, 0)));
    }

    public void setSubTaskArrayList(ArrayList<Integer> subTaskArrayList) {
        this.subTaskArrayList = subTaskArrayList;
    }

    public void setStateStatistics(HashMap<State, Integer> stateStatistics) {
        this.stateStatistics = stateStatistics;
    }

    public ArrayList<Integer> getSubTaskArrayList() {
        return subTaskArrayList;
    }

    public HashMap<State, Integer> getStateStatistics() {
        return stateStatistics;
    }

    public void updateState() {
        if (stateStatistics.get(State.NEW) == subTaskArrayList.size()) {
            setState(State.NEW);
        } else if (stateStatistics.get(State.DONE) == subTaskArrayList.size()) {
            setState(State.DONE);
        } else {
            setState(State.IN_PROGRESS);
        }
    }

    @Override
    public String toString() {
        return super.toString() + " tasks.Epic{" +
                "subTasksArrayList=" + subTaskArrayList +
                ", stateStatistics=" + stateStatistics +
                '}';
    }
}