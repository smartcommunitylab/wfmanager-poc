using Microsoft.AspNetCore.Mvc;
using workflow_manager_dotnet.Domain;
using workflow_manager_dotnet.Services;

namespace workflow_manager_dotnet.Controllers;

[ApiController]
[Route("api/workflow")]
public class WorkflowController : ControllerBase
{
    private readonly WorkflowService _workflowService;

    public WorkflowController(WorkflowService workflowService)
    {
        _workflowService = workflowService;
    }

    [HttpPost("")]
    public IActionResult StartWorkflow([FromBody] Workflow workflow)
    {
        _workflowService.StartWorkflow(workflow);
        return Ok();
    }

    [HttpGet("{id}")]
    public IActionResult GetWorkflow(string id)
    {
        var workflow = _workflowService.GetWorkflow(id);
        return Ok(workflow);
    }
}