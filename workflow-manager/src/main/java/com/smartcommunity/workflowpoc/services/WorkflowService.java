package com.smartcommunity.workflowpoc.services;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.smartcommunity.workflowpoc.domain.Task;
import com.smartcommunity.workflowpoc.domain.TaskStatus;
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
	 * @param workflow The workflow to start.
	 */
	public Workflow startWorkflow(Workflow workflow) {
		workflows.put(workflow.getId(), workflow);
		if (workflow.getTasks() != null && !workflow.getTasks().isEmpty()) {
			workflow.getTasks().forEach(task -> {
				task.setWorkflowId(workflow.getId());
				task = taskStoreService.store(task);
			});

			if (workflow.isParallel()) {
				// start all tasks
				workflow.getTasks().forEach(task -> doTask(task));
			}
			else {
				// start first
				doTask(workflow.getTasks().get(0));
			}
		}

		return workflow;
	}

	/**
	 * Processes task completion notifications.
	 * @param taskId The ID of the completed task.
	 * @param success Whether the task was completed successfully.
	 */
	@Override
	public void processTaskCompletion(String taskId, boolean success) {

		Workflow workflow = workflows.values()
			.stream()
			.filter(wf -> wf.getTasks().stream().anyMatch(t -> t.getId().equals(taskId)))
			.findFirst()
			.orElse(null);

		if (workflow == null) {
			logger.warn("No workflow found for completed task {}", taskId);
			return;
		}

		// get task
		int idx = workflow.getTasks().stream().map(Task::getId).toList().indexOf(taskId);
		if (idx == -1) {
			logger.warn("No task {} found in workflow {}", taskId, workflow.getId());
			return;
		}

		Task task = workflow.getTasks().get(idx);

		// process next on success
		if (success) {
			logger.info("Task {} of workflow {} completed successfully", taskId, workflow.getId());

			// proceed to next task if any
			if (idx + 1 < workflow.getTasks().size()) {
				try {
					Task next = workflow.getTasks().get(idx + 1);
					if (next != null && next.getStatus().equals(TaskStatus.PENDING.name())) {
						doTask(next);
					}
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			}

		}
		else

		{
			// mark task as failed
			task.setStatus("FAILED");
			logger.info("Task {} of workflow {} marked as FAILED", taskId, workflow.getId());

		}

	}

	private void doTask(Task task) {
		try {
			logger.info("Starting task {} of workflow {}", task.getId(), task.getWorkflowId());
			messagingService.sendTask(task);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public Workflow getWorkflow(String id) {
		Workflow workflow = workflows.get(id);
		if (workflow == null) {
			return null;
		}
		List<Task> tasks = new LinkedList<>();
		for (Task task : workflow.getTasks()) {
			if (task.getId() != null) {
				tasks.add(taskStoreService.getTaskById(task.getId()));
			}
			else {
				tasks.add(task);
			}
		}
		workflow.setTasks(tasks);
		return workflow;

	}

	public List<Workflow> listWorkflows() {
		return new LinkedList<>(workflows.values());
	}

}
