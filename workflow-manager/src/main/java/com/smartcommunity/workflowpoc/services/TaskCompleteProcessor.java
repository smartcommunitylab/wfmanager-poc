package com.smartcommunity.workflowpoc.services;

/**
 * Interface for processing task completion notifications.
 */
public interface TaskCompleteProcessor {

    void processTaskCompletion(String taskId, boolean success);
}
