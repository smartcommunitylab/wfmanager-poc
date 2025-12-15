using Microsoft.Extensions.Hosting;
using worker_dotnet.Services;

public class WorkerBackgroundService : BackgroundService
{
    private readonly WorkerService _workerService;

    public WorkerBackgroundService(WorkerService workerService)
    {
        _workerService = workerService;
    }

    protected override Task ExecuteAsync(CancellationToken stoppingToken)
    {
        _workerService.StartListening();
        return Task.CompletedTask;
    }
}