using RabbitMQ.Client;
using RabbitMQ.Client.Events;
using System.Text;
using System.Text.Json;
using workflow_manager_dotnet.Domain;

namespace workflow_manager_dotnet.Services;

public class RabbitMQService
{
    private readonly ILogger<RabbitMQService> _logger;
    private readonly IConfiguration _configuration;
    private readonly ConnectionFactory _factory;
    private const string TaskQueueName = "task_queue";
    private const string TaskCompleteQueueName = "task_completion_queue";

    public RabbitMQService(ILogger<RabbitMQService> logger, IConfiguration configuration)
    {
        _logger = logger;
        _configuration = configuration;

        _factory = new ConnectionFactory
        {
            HostName = _configuration["RabbitMQ:Host"] ?? "localhost",
            Port = int.Parse(_configuration["RabbitMQ:Port"] ?? "5672"),
            UserName = _configuration["RabbitMQ:Username"] ?? "guest",
            Password = _configuration["RabbitMQ:Password"] ?? "guest"
        };
    }

    public void Init(ITaskCompleteProcessor taskCompleteProcessor)
    {
        var receiveConnection = _factory.CreateConnection();
        var receiveChannel = receiveConnection.CreateModel();
        receiveChannel.QueueDeclare(TaskCompleteQueueName, true, false, false, null);
        _logger.LogInformation(" [*] Waiting for messages.");

        var consumer = new EventingBasicConsumer(receiveChannel);
        consumer.Received += (model, ea) =>
        {
            var body = ea.Body.ToArray();
            var message = Encoding.UTF8.GetString(body);
            _logger.LogInformation(" [x] Received '{Message}'", message);

            try
            {
                var task = JsonSerializer.Deserialize<WorkflowTask>(message);
                var success = "COMPLETED".Equals(task?.Status, StringComparison.OrdinalIgnoreCase);
                if (task != null)
                {
                    taskCompleteProcessor.ProcessTaskCompletion(task.Id!, success);
                }
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "Error processing task completion");
            }
            finally
            {
                _logger.LogInformation(" [x] Done");
                receiveChannel.BasicAck(ea.DeliveryTag, false);
            }
        };

        receiveChannel.BasicConsume(TaskCompleteQueueName, false, consumer);
    }

    public void SendTask(WorkflowTask task)
    {
        using var connection = _factory.CreateConnection();
        using var channel = connection.CreateModel();
        channel.QueueDeclare(TaskQueueName, true, false, false, null);

        var message = JsonSerializer.Serialize(task);
        var body = Encoding.UTF8.GetBytes(message);

        var properties = channel.CreateBasicProperties();
        properties.Persistent = true;

        channel.BasicPublish("", TaskQueueName, properties, body);
        _logger.LogInformation(" [x] Sent '{Message}'", message);
    }
}