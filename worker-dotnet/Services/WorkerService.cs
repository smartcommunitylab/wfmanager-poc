using Microsoft.Extensions.Logging;
using RabbitMQ.Client;
using RabbitMQ.Client.Events;
using System.Text;
using System.Text.Json;
using Microsoft.Extensions.Configuration;
using worker_dotnet.Domain;

namespace worker_dotnet.Services;

public class WorkerService
{
    private readonly ILogger<WorkerService> _logger;
    private readonly IConfiguration _configuration;
    private readonly TaskStoreService _taskStoreService;
    private readonly ConnectionFactory _factory;
    private const string TaskQueueName = "task_queue";
    private const string TaskCompleteQueueName = "task_completion_queue";

    public WorkerService(ILogger<WorkerService> logger, IConfiguration configuration, TaskStoreService taskStoreService)
    {
        _logger = logger;
        _configuration = configuration;
        _taskStoreService = taskStoreService;

        _factory = new ConnectionFactory
        {
            HostName = _configuration["RabbitMQ:Host"] ?? "localhost",
            Port = int.Parse(_configuration["RabbitMQ:Port"] ?? "5672"),
            UserName = _configuration["RabbitMQ:Username"] ?? "guest",
            Password = _configuration["RabbitMQ:Password"] ?? "guest"
        };
    }

    public void StartListening()
    {
        var connection = _factory.CreateConnection();
        var channel = connection.CreateModel();

        channel.QueueDeclare(TaskQueueName, true, false, false, null);
        _logger.LogInformation(" [*] Waiting for task messages.");
        channel.BasicQos(0, 1, false); // Fair dispatch

        var consumer = new EventingBasicConsumer(channel);
        consumer.Received += (model, ea) =>
        {
            var body = ea.Body.ToArray();
            var message = Encoding.UTF8.GetString(body);
            _logger.LogInformation(" [x] Received '{Message}'", message);

            try
            {
                var task = JsonSerializer.Deserialize<WorkflowTask>(message);
                if (task != null)
                {
                    ProcessTask(task);
                }
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "Error processing task");
            }
            finally
            {
                _logger.LogInformation(" [x] Done");
                channel.BasicAck(ea.DeliveryTag, false);
            }
        };

        channel.BasicConsume(TaskQueueName, false, consumer);
    }

    private void ProcessTask(WorkflowTask task)
    {
        try
        {
            _taskStoreService.StartTask(task);
            _logger.LogInformation("Processing task {TaskId} of workflow {WorkflowId}", task.Id, task.WorkflowId);
            Thread.Sleep(2000); // Simulate time-consuming task
            _taskStoreService.CompleteTask(task);
            SendTaskComplete(task);
            _logger.LogInformation("Task {TaskId} of workflow {WorkflowId} completed.", task.Id, task.WorkflowId);
        }
        catch (Exception e)
        {
            _logger.LogError(e, "Error processing task {TaskId}", task.Id);
        }
    }

    private void SendTaskComplete(WorkflowTask task)
    {
        using var connection = _factory.CreateConnection();
        using var channel = connection.CreateModel();
        channel.QueueDeclare(TaskCompleteQueueName, true, false, false, null);

        var message = JsonSerializer.Serialize(task);
        var body = Encoding.UTF8.GetBytes(message);

        var properties = channel.CreateBasicProperties();
        properties.Persistent = true;

        channel.BasicPublish("", TaskCompleteQueueName, properties, body);
        _logger.LogInformation(" [x] Sent '{Message}'", message);
    }
}