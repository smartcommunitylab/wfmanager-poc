package com.smartcommunity.workflowpoc.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.smartcommunity.workflowpoc.domain.Task;
import com.smartcommunity.workflowpoc.domain.TaskStatus;
import com.smartcommunity.workflowpoc.repository.TaskRepository;

import jakarta.transaction.Transactional;

/**
 * Service for managing tasks in the data store.
 */
@Service
@Transactional
public class TaskStoreService {

    @Autowired
    private TaskRepository taskRepository;

    /**
     * Initiates a new task or updates an existing one to PENDING state.
     * 
     * @param task
     * @return
     */
    public Task initiateTask(Task task) {
        Task dbTask = task.getId() != null ? taskRepository.findById(task.getId()).orElse(null) : null;

        if (dbTask == null) {
            task.setId(java.util.UUID.randomUUID().toString());
            task.setCreatedAt(System.currentTimeMillis());
            task.setUpdatedAt(System.currentTimeMillis());
            task.setStatus(TaskStatus.PENDING.name());
            return taskRepository.save(task);            
        } else {
            task.setUpdatedAt(System.currentTimeMillis());
            task.setStatus(TaskStatus.PENDING.name());
            return taskRepository.save(task);
        }
    }

    public Task getTaskById(String id) {
        return taskRepository.findById(id).orElse(null);
    }

}
