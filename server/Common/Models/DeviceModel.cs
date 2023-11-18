namespace Presentation.Common.Models;

public class DeviceModel : BaseModel
{
    public string Fullname { get; set; } = string.Empty;
    public string DeviceId { get; set; } = string.Empty;
    public string ConnectionId { get; set; } = string.Empty;
    public StatusDevice Status { get; set; }
    public DateTime LastConnect { get; set; }
}

public enum StatusDevice
{
    Online,
    Offline,
    Deleted
}