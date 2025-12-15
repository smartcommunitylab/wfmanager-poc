using workflow_manager_dotnet.Data;
using workflow_manager_dotnet.Domain;

namespace workflow_manager_dotnet.Services;

public class TaskStoreService
{
    private readonly ApplicationDbContext _context;

    public TaskStoreService(ApplicationDbContext context)
    {
        _context = context;
    }

    public WorkflowTask InitiateTask(WorkflowTask task)
    {
        WorkflowTask? dbTask = null;
        if (!string.IsNullOrEmpty(task.Id))
        {
            dbTask = _context.Tasks.Find(task.Id);
        }

        if (dbTask == null)
        {
            task.Id = Guid.NewGuid().ToString();
            task.CreatedAt = DateTimeOffset.UtcNow.ToUnixTimeMilliseconds();
            task.UpdatedAt = task.CreatedAt;
            task.Status = workflow_manager_dotnet.Domain.TaskStatus.PENDING.ToString();
            _context.Tasks.Add(task);
        }
        else
        {
            task.UpdatedAt = DateTimeOffset.UtcNow.ToUnixTimeMilliseconds();
            task.Status = workflow_manager_dotnet.Domain.TaskStatus.PENDING.ToString();
            _context.Entry(dbTask).CurrentValues.SetValues(task);
        }

        _context.SaveChanges();
        return task;
    }

    public WorkflowTask? GetTaskById(string id)
    {
        return _context.Tasks.Find(id);
    }
}