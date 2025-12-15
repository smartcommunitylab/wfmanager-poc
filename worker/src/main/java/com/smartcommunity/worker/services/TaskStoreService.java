package com.smartcommunity.worker.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.smartcommunity.worker.domain.Task;
import com.smartcommunity.worker.domain.TaskStatus;
import com.smartcommunity.worker.repository.TaskRepository;

/**
 * Service for managing tasks in the data store.
 */
@Service
@Transactional
public class TaskStoreService {

    @Autowired
    private TaskRepository taskRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Task startTask(Task task) {
        Task dbTask = taskRepository.findById(task.getId()).orElse(null);

        if (dbTask!= null) {
            task.setUpdatedAt(System.currentTimeMillis());
            task.setStatus(TaskStatus.IN_PROGRESS.name());
            return taskRepository.save(task);            
        } else {
            return null;
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Task completeTask(Task task) {
        Task dbTask = taskRepository.findById(task.getId()).orElse(null);

        if (dbTask!= null) {
            task.setUpdatedAt(System.currentTimeMillis());
            task.setStatus(TaskStatus.COMPLETED.name());
            return taskRepository.save(task);            
        } else {
            return null;
        }
    }
}
