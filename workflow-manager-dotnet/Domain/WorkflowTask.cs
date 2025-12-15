namespace workflow_manager_dotnet.Domain;

public class WorkflowTask
{
    public string? Id { get; set; }
    public string? Type { get; set; }
    public long CreatedAt { get; set; }
    public long UpdatedAt { get; set; }
    public string? Status { get; set; }
    public string? WorkflowId { get; set; }
}