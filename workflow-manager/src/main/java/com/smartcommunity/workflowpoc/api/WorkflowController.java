package com.smartcommunity.workflowpoc.api;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.smartcommunity.workflowpoc.domain.Workflow;
import com.smartcommunity.workflowpoc.services.WorkflowService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * REST controller for managing workflows.
 */
@RestController
@RequestMapping("/api/workflow")
public class WorkflowController {

    @Autowired
    private WorkflowService workflowService;

    /**
     * Starts a new workflow.
     * 
     * @param workflow The workflow to start.
     */
    @PostMapping("")
    public void startWorkflow(@RequestBody Workflow workflow) {
        workflowService.startWorkflow(workflow);
    }

    /**
     * Retrieves a workflow by its ID.
     * 
     * @param id The ID of the workflow.
     * @return The workflow with the specified ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Workflow> getWorkflow(@PathVariable String id) {
        return  ResponseEntity.ok(workflowService.getWorkflow(id)   );
    }
    
}
