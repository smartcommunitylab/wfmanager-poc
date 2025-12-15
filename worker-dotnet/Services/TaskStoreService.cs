using worker_dotnet.Data;
using worker_dotnet.Domain;

namespace worker_dotnet.Services;

public class TaskStoreService
{
    private readonly ApplicationDbContext _context;

    public TaskStoreService(ApplicationDbContext context)
    {
        _context = context;
    }

    public void StartTask(WorkflowTask task)
    {
        var dbTask = _context.Tasks.Find(task.Id);
        if (dbTask != null)
        {
            dbTask.Status = worker_dotnet.Domain.TaskStatus.IN_PROGRESS.ToString();
            dbTask.UpdatedAt = DateTimeOffset.UtcNow.ToUnixTimeMilliseconds();
            _context.SaveChanges();
        }
    }

    public void CompleteTask(WorkflowTask task)
    {
        var dbTask = _context.Tasks.Find(task.Id);
        if (dbTask != null)
        {
            dbTask.Status = worker_dotnet.Domain.TaskStatus.COMPLETED.ToString();
            dbTask.UpdatedAt = DateTimeOffset.UtcNow.ToUnixTimeMilliseconds();
            _context.SaveChanges();
        }
    }
}