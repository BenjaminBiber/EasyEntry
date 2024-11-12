using Microsoft.Extensions.Logging;
using MudBlazor.Services;
using ZXing.Net.Maui;
using ZXing.Net.Maui.Controls;

namespace EasyEntryApp
{
    public static class MauiProgram
    {
        public static MauiApp CreateMauiApp()
        {
            var builder = MauiApp.CreateBuilder();
            builder
                .UseMauiApp<App>()
                .ConfigureFonts(fonts =>
                {
                    fonts.AddFont("OpenSans-Regular.ttf", "OpenSansRegular");
                });
            builder.UseBarcodeReader();
            builder.Services.AddMauiBlazorWebView();
            builder.Services.AddMudServices();


#if DEBUG
            builder.Services.AddBlazorWebViewDeveloperTools();
    		builder.Logging.AddDebug();
#endif

            return builder.Build();
        }
    }
}
