namespace workflow_manager_dotnet.Domain;

public class Workflow
{
    public string? Id { get; set; }
    public string? Name { get; set; }
    public List<WorkflowTask>? Tasks { get; set; }
}