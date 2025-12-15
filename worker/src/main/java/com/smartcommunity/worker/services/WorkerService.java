package com.smartcommunity.worker.services;

import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import com.rabbitmq.client.MessageProperties;
import com.smartcommunity.worker.domain.Task;

import jakarta.annotation.PostConstruct;
import tools.jackson.databind.ObjectMapper;

/**
 * Service for sending messages to a RabbitMQ queue.
 */
@Service
public class WorkerService {

	private static final Logger logger = LoggerFactory.getLogger(WorkerService.class);

	private static final String TASK_QUEUE_NAME = "task_queue";

	private static final String TASK_COMPLETE_QUEUE_NAME = "task_completion_queue";

	@Value("${spring.rabbitmq.host}")
	private String host;

	@Value("${spring.rabbitmq.port}")
	private String port;

	@Value("${spring.rabbitmq.username}")
	private String username;

	@Value("${spring.rabbitmq.password}")
	private String password;

	private final ObjectMapper objectMapper = new ObjectMapper();

	private ConnectionFactory factory;

	@Autowired
	private TaskStoreService taskStoreService;

	@PostConstruct
	public void init() throws Exception {
		ConnectionFactory factory = new ConnectionFactory();
		factory.setHost(host);
		factory.setPort(Integer.parseInt(port));
		factory.setUsername(username);
		factory.setPassword(password);
		this.factory = factory;

		// Start a consumer to listen for task messages
		Connection receiveConnection = factory.newConnection();
		Channel receiveChannel = receiveConnection.createChannel();

		receiveChannel.queueDeclare(TASK_QUEUE_NAME, true, false, false, null);
		logger.info(" [*] Waiting for task messages. ");
		receiveChannel.basicQos(1); // Fair dispatch

		DeliverCallback deliverCallback = (consumerTag, delivery) -> {
			String message = new String(delivery.getBody(), "UTF-8");
			logger.info(" [x] Received '" + message + "'");
			// process task completion
			try {
				Task task = objectMapper.readValue(message, Task.class);
				processTask(task);
			}
			finally {
				logger.info(" [x] Done");
				receiveChannel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
			}
		};
		receiveChannel.basicConsume(TASK_QUEUE_NAME, false, deliverCallback, consumerTag -> {
		});
	}

	private void processTask(Task msg) {
		// Simulate task processing
		try {
			Task task = taskStoreService.startTask(msg.getId());
			logger.info("Processing task {} of workflow {}", task.getId(), task.getWorkflowId());
			// Simulate time-consuming task
			doWork();
			task = taskStoreService.completeTask(task);
			sendTaskComplete(task);
			logger.info("Task {} of workflow {} completed.", task.getId(), task.getWorkflowId());
		}
		catch (Exception e) {
			logger.error("Error processing task " + msg.getId(), e);
		}
	}

	private void doWork() throws InterruptedException {
		Random rand = new Random();
		int delay = (rand.nextInt(20) + 1) * 1000;
		Thread.sleep(delay); // Simulate time-consuming task

		return;
	}

	/**
	 * Sends a message to the RabbitMQ queue.
	 * @param message The message to send.
	 * @throws Exception If an error occurs while sending the message.
	 */
	public void sendTaskComplete(Task task) throws Exception {
		try (Connection connection = factory.newConnection(); Channel channel = connection.createChannel()) {
			// Declare a durable queue
			channel.queueDeclare(TASK_COMPLETE_QUEUE_NAME, true, false, false, null);

			String message = objectMapper.writeValueAsString(task);

			// Publish a message to the queue
			channel.basicPublish("", TASK_COMPLETE_QUEUE_NAME, MessageProperties.PERSISTENT_TEXT_PLAIN,
					message.getBytes("UTF-8"));
			logger.info(" [x] Sent '" + message + "'");
		}
	}

}
