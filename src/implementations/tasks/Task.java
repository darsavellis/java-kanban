package implementations.tasks;

import implementations.utility.State;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;

public class Task {
    private int id;
    private String name;
    private String description;
    private State state;
    private LocalDateTime startTime;
    private Duration duration;
    private boolean isReadyForPrioritizing;

    public Task(String name, String description, State state, LocalDateTime startTime, Long duration) {
        this(0, name, description, state, startTime, duration);
    }

    public Task(String name, String description, State state) {
        this(0, name, description, state, null, null);
    }

    public Task(int id, String name, String description, State state, LocalDateTime startTime, Long duration) {
        setId(id);
        setName(name);
        setDescription(description);
        setState(state);
        setStartTime(startTime);
        setDuration(duration);
        validateStartTimeAndDuration();
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    protected void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    protected void setDescription(String description) {
        this.description = description;
    }

    public State getState() {
        return state;
    }

    protected void setState(State state) {
        this.state = state;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        if (isReadyForPrioritizing) {
            return startTime.plus(duration);
        }
        return null;
    }

    public long getDuration() {
        if (isReadyForPrioritizing) {
            return duration.toMinutes();
        } else {
            return 0;
        }
    }

    public void setDuration(Long duration) {
        if (duration != null) {
            this.duration = Duration.ofMinutes(duration);
        }
    }

    public void validateStartTimeAndDuration() {
        if (startTime != null && duration != null) {
            isReadyForPrioritizing = true;
        }
    }

    public boolean isReadyForPrioritizing() {
        return isReadyForPrioritizing;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Task task = (Task) o;
        return Objects.equals(id, task.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Task{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", state=" + state +
                ", startTime=" + startTime +
                ", duration=" + duration +
                '}';
    }
}