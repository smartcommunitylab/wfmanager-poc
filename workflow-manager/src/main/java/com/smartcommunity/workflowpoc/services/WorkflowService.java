package com.smartcommunity.workflowpoc.services;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.smartcommunity.workflowpoc.domain.Task;
import com.smartcommunity.workflowpoc.domain.Workflow;

import jakarta.annotation.PostConstruct;

/**
 * Service for managing workflows and processing task completions.
 */
@Service
public class WorkflowService implements TaskCompleteProcessor {

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(WorkflowService.class);

    @Autowired
    private MessagingService messagingService;

    @Autowired
    private TaskStoreService taskStoreService;

    private Map<String, Workflow> workflows = new HashMap<>();

    @PostConstruct
    public void init() throws Exception {
        messagingService.init(this);
    }

    /**
     * Starts a new workflow by processing its tasks sequentially.
     * 
     * @param workflow The workflow to start.
     */
    public void startWorkflow(Workflow workflow) {
        workflows.put(workflow.getId(), workflow);
        if (workflow.getTasks() != null && !workflow.getTasks().isEmpty()) {
            doTask(workflow.getTasks().get(0), workflow.getId());
        }
    }

    /**
     * Processes task completion notifications.
     * 
     * @param taskId The ID of the completed task.
     * @param success Whether the task was completed successfully.
     */
    @Override
    public void processTaskCompletion(String taskId, boolean success) {
        //search workflows for taskId
        for (Workflow workflow : workflows.values()) {
            for (int i = 0; i < workflow.getTasks().size(); i++) {
                if (workflow.getTasks().get(i).getId().equals(taskId)) {
                    if (success) {
                        logger.info("Task {} of workflow {} completed successfully", taskId, workflow.getId());
                        //proceed to next task if any
                        if (i + 1 < workflow.getTasks().size()) {
                            try {
                                doTask(workflow.getTasks().get(i + 1), workflow.getId());
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    } else {
                        //mark task as failed
                        workflow.getTasks().get(i).setStatus("FAILED");
                    }
                    return;
                }
            }
        }
    }

    private void doTask(Task task, String workflowId) {
        try {
            task.setWorkflowId(workflowId);
            logger.info("Starting task {} of workflow {}", task.getId(), workflowId);
            task = taskStoreService.initiateTask(task);
            messagingService.sendTask(task);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Workflow getWorkflow(String id) {
        Workflow workflow = workflows.get(id);
        List<Task> tasks = new LinkedList<>();
        for (Task task : workflows.get(id).getTasks()) {
            if (task.getId() != null) {
                tasks.add(taskStoreService.getTaskById(task.getId()));
            } else {
                tasks.add(task);
            }
        }
        workflow.setTasks(tasks);
        return workflow;

    }
}
