package com.smartcommunity.worker.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.smartcommunity.worker.domain.Task;

public interface TaskRepository extends JpaRepository<Task, String> {

}
