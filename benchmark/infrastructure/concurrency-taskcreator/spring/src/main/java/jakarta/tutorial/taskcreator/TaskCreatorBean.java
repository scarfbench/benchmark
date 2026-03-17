package jakarta.tutorial.taskcreator;

import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;

import java.io.Serializable;
import java.util.Set;

@Component("taskCreatorBean")
@SessionScope
public class TaskCreatorBean implements Serializable {
    private final TaskService taskService;

    private String taskMessages = "";
    private String taskType = "IMMEDIATE";
    private String taskName = "";
    private String periodicTask = "";

    public TaskCreatorBean(TaskService taskService) { this.taskService = taskService; }

    public String getTaskMessages() { return taskService.getInfoField(); }
    public void setTaskMessages(String msg) { this.taskMessages = msg; }

    public String getTaskType() { return taskType; }
    public void setTaskType(String t) { this.taskType = t; }

    public String getTaskName() { return taskName; }
    public void setTaskName(String n) { this.taskName = n; }

    public String getPeriodicTask() { return periodicTask; }
    public void setPeriodicTask(String t) { this.periodicTask = t; }

    public Set<String> getPeriodicTasks() { return taskService.getPeriodicTasks(); }

    public void submitTask() {
        if (!taskService.getPeriodicTasks().contains(taskName)) {
            Task task = taskService.newTask(taskName, taskType);
            // Prefer delegating creation to service so poster is injected; see note below.
            taskService.submitTask(task, taskType);
            taskType = "IMMEDIATE";
            taskName = "";
        }
    }

    public void cancelTask() { taskService.cancelPeriodicTask(periodicTask); }
    public void clearInfoField() { taskService.clearInfoField(); }
}