using Microsoft.EntityFrameworkCore;
using workflow_manager_dotnet.Data;
using workflow_manager_dotnet.Services;

var builder = WebApplication.CreateBuilder(args);

// Add services to the container.
builder.Services.AddControllers();
builder.Services.AddEndpointsApiExplorer();
builder.Services.AddSwaggerGen();

builder.Services.AddDbContext<ApplicationDbContext>(options =>
    options.UseNpgsql(builder.Configuration.GetConnectionString("DefaultConnection")));

builder.Services.AddScoped<RabbitMQService>();
builder.Services.AddScoped<TaskStoreService>();
builder.Services.AddScoped<WorkflowService>();

var app = builder.Build();

// Ensure database is created
using (var scope = app.Services.CreateScope())
{
    var dbContext = scope.ServiceProvider.GetRequiredService<ApplicationDbContext>();
    dbContext.Database.EnsureCreated();
}

// Initialize RabbitMQ
using (var scope = app.Services.CreateScope())
{
    var rabbitMQService = scope.ServiceProvider.GetRequiredService<RabbitMQService>();
    var workflowService = scope.ServiceProvider.GetRequiredService<WorkflowService>();
    rabbitMQService.Init(workflowService);
}

// Configure the HTTP request pipeline.
if (app.Environment.IsDevelopment())
{
    app.UseSwagger();
    app.UseSwaggerUI();
}

app.UseHttpsRedirection();
app.UseAuthorization();
app.MapControllers();

app.Run();