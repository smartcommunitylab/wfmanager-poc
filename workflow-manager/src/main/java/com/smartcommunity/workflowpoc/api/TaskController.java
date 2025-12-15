package com.smartcommunity.workflowpoc.api;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.smartcommunity.workflowpoc.domain.Task;
import com.smartcommunity.workflowpoc.services.TaskStoreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * REST controller for managing workflows.
 */
@RestController
@RequestMapping("/api/task")
@CrossOrigin(origins = "*", maxAge = 3600, allowedHeaders = "*", exposedHeaders = "*")
public class TaskController {

    @Autowired
    private TaskStoreService taskService;


    /**
     * Retrieves a task by its ID.
     * 
     * @param id The ID of the task.
     * @return The task with the specified ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Task> getTask(@PathVariable String id) {
        return  ResponseEntity.ok(taskService.getTaskById(id) );
    }
    
}
