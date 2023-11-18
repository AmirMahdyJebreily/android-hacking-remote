namespace Presentation.Common.Utilities;

public class LocalizationUtility
{
    public IConfiguration Configuration { get; set; }
    public LocalizationUtility(IConfiguration configuration)
        => this.Configuration = configuration;

    public string Translate(string text)
        => Configuration.GetSection("LOCALIZATION")[text] ?? text;
}