using System.ComponentModel.DataAnnotations;

using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;

using Telegram.BotAPI;
using Telegram.BotAPI.AvailableTypes;
using Telegram.BotAPI.AvailableMethods;
using Telegram.BotAPI.AvailableMethods.FormattingOptions;

using Presentation.Common.Contexts;
using Presentation.Common.Constant;

namespace Presentation.Handlers;

[ApiController, Route(template: "/")]
public class HttpHandler : ControllerBase
{
    private readonly BotClient _telegram;
    private readonly DatabaseContext _context;
    private readonly IConfiguration _configuration;
    private readonly ConfigurationDynamic _configurationDynamic;
    public HttpHandler(DatabaseContext context, BotClient telegram, IConfiguration configuration, ConfigurationDynamic configurationDynamic)
    {
        _context = context;
        _telegram = telegram;
        _configuration = configuration;
        _configurationDynamic = configurationDynamic;
    }
    [HttpPost("[Action]")]
    public async Task<IActionResult> Upload([FromBody] UploadModel model)
    {
        if (!ModelState.IsValid) return BadRequest(ModelState);

        if (!await _context.Devices.AnyAsync(x => x.DeviceId == model.Device))
            return BadRequest("You do not have the required access");

        if (!Directory.Exists(LocationConstant.LocationDownload))
            Directory.CreateDirectory(LocationConstant.LocationDownload);

        using (var stream = new FileStream(Path.Combine(LocationConstant.LocationDownload, Path.GetFileName(model.Document)), FileMode.Append))
            await stream.WriteAsync(model.Content, 0, model.Content.Length);

        return Ok();
    }
    [HttpPost("[Action]")]
    public async Task<IActionResult> Send(MessageModel model)
    {
        if (!ModelState.IsValid) return BadRequest(ModelState);

        if (!await _context.Devices.AnyAsync(x => x.DeviceId == model.Device))
            return BadRequest("You do not have the required access");

        string ResponseChatId = _configuration.GetSection("settings")["Group"];

        if (model.Type == MessageType.Document)
        {
            string location = Path.Combine(LocationConstant.LocationDownload, model.Document);

            if (System.IO.File.Exists(location))
            {
                await using Stream stream = System.IO.File.OpenRead(location);

                await _telegram.SendDocumentAsync(
                    chatId: ResponseChatId,
                    caption: model.Message,
                    document: new InputFile(
                        streamcontent: new StreamContent(stream),
                        filename: DateTime.Now.ToFileTime().ToString() + Path.GetExtension(location)),
                    parseMode: ParseMode.HTML);

            }
            else return BadRequest("File does not exist");
        }
        else await _telegram.SendMessageAsync(
                text: model.Message,
                chatId: ResponseChatId,
                parseMode: ParseMode.HTML,
                replyMarkup: KeyboardConstant.KeyboardMarkupControlPanel(model.Device),
                disableWebPagePreview: true
            );
        return Ok();
    }
}
public class UploadModel
{
    [Required]
    public string Device { get; set; }
    [Required]
    public byte[] Content { get; set; }
    [Required]
    public string Document { get; set; }
}

public class MessageModel
{
    [Required]
    public string Device { get; set; }
    [Required]
    public string Message { get; set; }
    public string Document { get; set; }
    public MessageType Type { get; set; }

}

public enum MessageType
{
    Message = 1,
    Document = 2
}