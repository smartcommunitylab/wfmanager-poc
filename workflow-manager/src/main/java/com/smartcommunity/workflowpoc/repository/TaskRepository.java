package com.smartcommunity.workflowpoc.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.smartcommunity.workflowpoc.domain.Task;

public interface TaskRepository extends JpaRepository<Task, String> {

}
