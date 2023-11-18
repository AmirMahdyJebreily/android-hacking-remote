using Presentation.Common.Models;
using Presentation.Common.Contexts;

using Microsoft.AspNetCore.SignalR;
using Microsoft.EntityFrameworkCore;

namespace Presentation.Handlers;

public class WebSocketHandler : Hub
{
    private readonly DatabaseContext _context;
    private readonly ConfigurationDynamic _configuration;
    public WebSocketHandler(DatabaseContext context, ConfigurationDynamic configuration)
    {
        _context = context;
        _configuration = configuration;
    }

    public override async Task OnConnectedAsync()
    {
        if (Context.GetHttpContext().Request.Query.ContainsKey("target"))
        {
            string device_id = Context.GetHttpContext().Request.Query["target"];

            var model = await _context.Devices.IgnoreQueryFilters().SingleOrDefaultAsync(x => x.DeviceId == device_id)
                ?? new DeviceModel { DeviceId = device_id };

            await Clients.Client(Context.ConnectionId)
                .SendAsync(
                    "Configuration",
                    _configuration.AutomaticHide);

            if (model.Status != StatusDevice.Deleted)
            {
                model.LastConnect = DateTime.Now;
                model.Status = StatusDevice.Online;
                model.ConnectionId = Context.ConnectionId;

                _context.Devices.Update(model);
                await _context.SaveChangesAsync();

                await base.OnConnectedAsync();
            }
        }
        await base.OnDisconnectedAsync(new Exception("You are blocked from this service."));
    }
    public override async Task OnDisconnectedAsync(Exception exception)
    {
        if (Context.GetHttpContext().Request.Query.ContainsKey("target"))
        {
            string device_id = Context.GetHttpContext().Request.Query["target"];

            if (await _context.Devices.AnyAsync(x => x.DeviceId == device_id))
            {
                var model = await _context.Devices.SingleOrDefaultAsync(x => x.DeviceId == device_id);

                model.LastConnect = DateTime.Now;
                model.Status = StatusDevice.Offline;

                _context.Devices.Update(model);

                await _context.SaveChangesAsync();
            }
        }
        await base.OnDisconnectedAsync(exception);
    }
}
