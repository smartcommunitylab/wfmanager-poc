package com.smartcommunity.workflowpoc.domain;

import java.util.Collections;
import java.util.List;

public class Workflow {

	private String id;

	private String name;

	private List<Task> tasks = Collections.emptyList();

	private boolean parallel = false;

	public Workflow() {
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<Task> getTasks() {
		return tasks;
	}

	public void setTasks(List<Task> tasks) {
		this.tasks = tasks;
	}

	public boolean isParallel() {
		return parallel;
	}

	public void setParallel(boolean parallel) {
		this.parallel = parallel;
	}

}
