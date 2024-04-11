package implementations.dispatchers;

import implementations.tasks.Task;
import interfaces.HistoryManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class InMemoryHistoryManager implements HistoryManager {
    private final HashMap<Integer, Node> taskIndexes = new HashMap<>();
    private Node first;
    private Node last;
    private int size;

    @Override
    public void addTask(Task task) {
        if (task != null) {
            linkLast(task);
        }
    }

    private void updateSize() {
        size = taskIndexes.size();
    }

    private void linkLast(Task task) {
        Node element = new Node(task);
        if (taskIndexes.containsKey(task.getId())) {
            removeNode(task.getId());
        }
        taskIndexes.put(task.getId(), element);
        if (size == 0) {
            first = last = element;
        } else {
            last.setNext(element);
            element.setPrevious(last);
            last = element;
        }
        updateSize();
    }

    @Override
    public void remove(Integer id) {
        removeNode(id);
    }

    private void removeNode(Integer id) {
        Node element = taskIndexes.get(id);
        if (size == 1) {
            first = last = null;
        } else if (size > 1) {
            Node next = element.getNext();
            Node previous = element.getPrevious();
            removeCurrentFromNext(next, previous);
            removeCurrentFromPrevious(next, previous);
        }
        taskIndexes.remove(id);
        updateSize();
    }

    private void removeCurrentFromNext(Node next, Node previous) {
        if (next == null) {
            previous.setNext(null);
            last = previous;
        } else {
            if (previous == null) {
                next.setPrevious(null);
                first = next;
            } else {
                next.setPrevious(previous);
            }
        }
    }

    private void removeCurrentFromPrevious(Node next, Node previous) {
        if (previous == null) {
            next.setPrevious(null);
            first = next;
        } else {
            if (next == null) {
                previous.setNext(null);
                last = previous;
            } else {
                previous.setNext(next);
            }
        }
    }

    private ArrayList<Task> getTasks() {
        ArrayList<Task> taskArrayList = new ArrayList<>();
        if (size != 0) {
            Node iterator = first;
            while (iterator != null) {
                taskArrayList.add(iterator.getValue());
                iterator = iterator.getNext();
            }
        }
        return taskArrayList;
    }

    @Override
    public List<Task> getHistory() {
        return getTasks();
    }

    private static class Node {
        private Task value;
        private Node next;
        private Node previous;

        public Node(Task value) {
            setValue(value);
        }

        public Task getValue() {
            return value;
        }

        private void setValue(Task value) {
            this.value = value;
        }

        public Node getNext() {
            return next;
        }

        public void setNext(Node next) {
            this.next = next;
        }

        public Node getPrevious() {
            return previous;
        }

        public void setPrevious(Node previous) {
            this.previous = previous;
        }
    }
}