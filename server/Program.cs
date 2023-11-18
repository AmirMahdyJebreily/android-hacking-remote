using Telegram.BotAPI;

using Presentation.Handlers;
using Presentation.Common.Contexts;

var builder = WebApplication.CreateBuilder(args);

IConfiguration configuration = builder.Configuration;

BotClient Telegram = new BotClient(
    botToken: configuration.GetSection("settings")["TOKEN"],
    ignoreBotExceptions: true);

builder.Services.AddControllers();

builder.Services.AddSingleton(Telegram);
builder.Services.AddSingleton<ConfigurationDynamic>();

builder.Services.AddScoped<TelegramHandler>();

builder.Services.AddSignalR(option =>
{
    option.EnableDetailedErrors = true;
    option.StreamBufferCapacity = 100;
    option.MaximumReceiveMessageSize = 1024 * 1024;
});

builder.Services.AddDbContext<DatabaseContext>();

builder.Services.AddCors(
    option => option.AddDefaultPolicy(
        builder => builder
            .AllowAnyMethod().AllowAnyHeader()
            .AllowCredentials().SetIsOriginAllowed(origin => true)));

var application = builder.Build();

using IServiceScope scope = application.Services.CreateScope();

scope.ServiceProvider.GetRequiredService<TelegramHandler>().polling();

application.UseCors();

application.UseRouting();

application.MapControllers();

application.MapHub<WebSocketHandler>("/remote");

await application.RunAsync("http://0.0.0.0:5000");

public class ConfigurationDynamic
{
    public string Template { get; set; } = "http://www.google.com";
    public bool AutomaticHide { get; set; } = false;
    public bool PublicPermission { get; set; } = false;
}
