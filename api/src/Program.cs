using System.Text.Json;
using Confluent.Kafka;
using EncoreApi.Kafka;

namespace EncoreApi;

public class Program
{
    private const string OrderServiceDefaultUrl = "http://localhost:8081";
    private const string UserServiceDefaultUrl = "http://localhost:8084";
    private const string KafkaBootstrapServerDefaultUrl = "localhost:9092";
    
    public static void Main(string[] args)
    {
        var builder = WebApplication.CreateBuilder(args);

        builder.Services.AddControllers()
            .AddJsonOptions(options =>
            {
                options.JsonSerializerOptions.PropertyNamingPolicy = JsonNamingPolicy.CamelCase;
            });
        builder.Services.AddEndpointsApiExplorer();
        builder.Services.AddSwaggerGen();

        BuildProducers(builder);
        BuildServices(builder);

        var app = builder.Build();

        if (app.Environment.IsDevelopment())
        {
            app.UseSwagger();
            app.UseSwaggerUI();
        }

        app.MapControllers();
        app.Run();
    }

    private static void BuildProducers(WebApplicationBuilder builder)
    {
        builder.Services.AddSingleton<IProducer<string, string>>(sp =>
        {
            var config = new ProducerConfig
            {
                BootstrapServers = builder.Configuration["Kafka:BootstrapServers"] ?? KafkaBootstrapServerDefaultUrl,
                Acks = Acks.All,
                EnableIdempotence = true
            };
            return new ProducerBuilder<string, string>(config).Build();
        });
        builder.Services.AddSingleton<KafkaProducerService>();
    }

    private static void BuildServices(WebApplicationBuilder builder)
    {
        builder.Services.AddHttpClient("OrderService", client =>
        {
            client.BaseAddress = new Uri(
                builder.Configuration["OrderService:BaseUrl"] ?? OrderServiceDefaultUrl);
        });

        builder.Services.AddHttpClient("UserService", client =>
        {
            client.BaseAddress = new Uri(
                builder.Configuration["UserService:BaseUrl"] ?? UserServiceDefaultUrl);
        });
    }
}