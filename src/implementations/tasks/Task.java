package implementations.tasks;

import implementations.utility.State;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;

public class Task implements Comparable<Task> {
    private Integer id;
    private String name;
    private String description;
    private State state;
    private LocalDateTime startTime;
    private Duration duration;

    public Task(String name, String description, State state, LocalDateTime startTime, Long duration) {
        this(null, name, description, state, startTime, duration);
    }

    public Task(String name, String description, State state) {
        this(null, name, description, state, null, null);
    }

    public Task(Integer id, String name, String description, State state, LocalDateTime startTime, Long duration) {
        setId(id);
        setName(name);
        setDescription(description);
        setState(state);
        setStartTime(startTime);
        setDuration(duration);
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

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
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
        if (isReadyForPrioritizing()) {
            return startTime.plus(duration);
        }
        return null;
    }

    public Long getDuration() {
        if (Objects.nonNull(duration)) {
            return duration.toMinutes();
        } else {
            return null;
        }
    }

    public void setDuration(Long duration) {
        if (duration != null) {
            this.duration = Duration.ofMinutes(duration);
        }
    }

    public boolean isReadyForPrioritizing() {
        return startTime != null && duration != null;
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

    @Override
    public int compareTo(Task secondTask) {
        LocalDateTime firstStart = getStartTime();
        LocalDateTime firstEnd = getEndTime();
        LocalDateTime secondStart = secondTask.getStartTime();
        LocalDateTime secondEnd = secondTask.getEndTime();
        if ((firstStart.isAfter(secondEnd)) || firstStart.isEqual(secondEnd)
                && firstStart.isAfter(secondStart)) {
            return 1;
        } else if ((secondStart.isAfter(firstEnd)) || secondStart.isEqual(firstEnd)
                && secondStart.isEqual(firstEnd)) {
            return -1;
        } else {
            return 0;
        }
    }
}