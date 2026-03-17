package jakarta.tutorial.taskcreator;

import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.Set;

@Named("taskCreatorBean")
@SessionScoped
public class TaskCreatorBean implements Serializable {
    private static final long serialVersionUID = 1L;

    @Inject
    TaskService taskService;

    private String taskMessages = "";
    private String taskType = "IMMEDIATE";
    private String taskName = "";
    private String periodicTask = "";

    public String getTaskMessages() {
        return taskService.getInfoField();
    }

    public void setTaskMessages(String msg) {
        this.taskMessages = msg;
    }

    public String getTaskType() {
        return taskType;
    }

    public void setTaskType(String t) {
        this.taskType = t;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String n) {
        this.taskName = n;
    }

    public String getPeriodicTask() {
        return periodicTask;
    }

    public void setPeriodicTask(String t) {
        this.periodicTask = t;
    }

    public Set<String> getPeriodicTasks() {
        return taskService.getPeriodicTasks();
    }

    public void submitTask() {
        if (!taskService.getPeriodicTasks().contains(taskName)) {
            Task task = new Task(taskName, taskType);
            taskService.submitTask(task, taskType);
            taskType = "IMMEDIATE";
            taskName = "";
        }
    }

    public void cancelTask() {
        taskService.cancelPeriodicTask(periodicTask);
    }

    public void clearInfoField() {
        taskService.clearInfoField();
    }
}
