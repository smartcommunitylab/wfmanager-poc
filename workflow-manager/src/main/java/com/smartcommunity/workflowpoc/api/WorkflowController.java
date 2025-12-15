package com.smartcommunity.workflowpoc.api;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.smartcommunity.workflowpoc.domain.Workflow;
import com.smartcommunity.workflowpoc.services.WorkflowService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * REST controller for managing workflows.
 */
@RestController
@RequestMapping("/api/workflow")
@CrossOrigin(origins = "*", maxAge = 3600, allowedHeaders = "*", exposedHeaders = "*")
public class WorkflowController {

	@Autowired
	private WorkflowService workflowService;

	/**
	 * Lists all workflows.
	 */

	@GetMapping("")
	public Page<Workflow> listWorkflows(Pageable pageable) {
		return new PageImpl<>(workflowService.listWorkflows(), pageable, workflowService.listWorkflows().size());
	}

	/**
	 * Starts a new workflow.
	 * @param workflow The workflow to start.
	 */
	@PostMapping("")
	public Workflow startWorkflow(@RequestBody Workflow workflow) {
		return workflowService.startWorkflow(workflow);
	}

	/**
	 * Retrieves a workflow by its ID.
	 * @param id The ID of the workflow.
	 * @return The workflow with the specified ID.
	 */
	@GetMapping("/{id}")
	public ResponseEntity<Workflow> getWorkflow(@PathVariable String id) {
		Workflow workflow = workflowService.getWorkflow(id);
		if (workflow == null) {
			return ResponseEntity.notFound().build();
		}
		return ResponseEntity.ok(workflow);
	}

}
