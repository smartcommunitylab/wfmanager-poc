namespace workflow_manager_dotnet.Services;

public interface ITaskCompleteProcessor
{
    void ProcessTaskCompletion(string taskId, bool success);
}