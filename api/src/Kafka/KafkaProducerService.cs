using Confluent.Kafka;
using System.Text.Json;

namespace EncoreApi.Kafka;

public class KafkaProducerService
{
    private readonly IProducer<string, string> _producer;
    private readonly ILogger<KafkaProducerService> _logger;
    private static readonly JsonSerializerOptions JsonOptions = new()
    {
        PropertyNamingPolicy = JsonNamingPolicy.CamelCase
    };

    public KafkaProducerService(
        IProducer<string, string> producer,
        ILogger<KafkaProducerService> logger)
    {
        _producer = producer;
        _logger = logger;
    }

    public async Task PublishAsync<T>(string topic, string key, T payload)
    {
        var message = new Message<string, string>
        {
            Key = key,
            Value = JsonSerializer.Serialize(payload, JsonOptions)
        };

        try
        {
            var result = await _producer.ProduceAsync(topic, message);
            _logger.LogInformation(
                "Published to {Topic} [partition {Partition} @ offset {Offset}]",
                topic, result.Partition, result.Offset);
        }
        catch (ProduceException<string, string> ex)
        {
            _logger.LogError(ex,
                "Failed to publish to {Topic} with key {Key}", topic, key);
            throw;
        }
    }
}