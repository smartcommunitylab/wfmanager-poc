using workflow_manager_dotnet.Domain;

namespace workflow_manager_dotnet.Services;

public class WorkflowService : ITaskCompleteProcessor
{
    private readonly ILogger<WorkflowService> _logger;
    private readonly RabbitMQService _messagingService;
    private readonly TaskStoreService _taskStoreService;
    private readonly Dictionary<string, Workflow> _workflows = new();

    public WorkflowService(ILogger<WorkflowService> logger, RabbitMQService messagingService, TaskStoreService taskStoreService)
    {
        _logger = logger;
        _messagingService = messagingService;
        _taskStoreService = taskStoreService;
    }

    public void StartWorkflow(Workflow workflow)
    {
        _workflows[workflow.Id!] = workflow;
        if (workflow.Tasks != null && workflow.Tasks.Any())
        {
            DoTask(workflow.Tasks[0], workflow.Id!);
        }
    }

    public void ProcessTaskCompletion(string taskId, bool success)
    {
        foreach (var workflow in _workflows.Values)
        {
            for (int i = 0; i < workflow.Tasks!.Count; i++)
            {
                if (workflow.Tasks[i].Id == taskId)
                {
                    if (success)
                    {
                        _logger.LogInformation("Task {TaskId} of workflow {WorkflowId} completed successfully", taskId, workflow.Id);
                        if (i + 1 < workflow.Tasks.Count)
                        {
                            DoTask(workflow.Tasks[i + 1], workflow.Id!);
                        }
                    }
                    else
                    {
                        workflow.Tasks[i].Status = "FAILED";
                    }
                    return;
                }
            }
        }
    }

    private void DoTask(WorkflowTask task, string workflowId)
    {
        try
        {
            task.WorkflowId = workflowId;
            _logger.LogInformation("Starting task {TaskId} of workflow {WorkflowId}", task.Id, workflowId);
            task = _taskStoreService.InitiateTask(task);
            _messagingService.SendTask(task);
        }
        catch (Exception e)
        {
            _logger.LogError(e, "Error starting task {TaskId}", task.Id);
        }
    }

    public Workflow GetWorkflow(string id)
    {
        var workflow = _workflows[id];
        var tasks = new List<WorkflowTask>();
        foreach (var task in workflow.Tasks!)
        {
            if (!string.IsNullOrEmpty(task.Id))
            {
                var dbTask = _taskStoreService.GetTaskById(task.Id!);
                if (dbTask != null)
                {
                    tasks.Add(dbTask);
                }
            }
            else
            {
                tasks.Add(task);
            }
        }
        workflow.Tasks = tasks;
        return workflow;
    }
}