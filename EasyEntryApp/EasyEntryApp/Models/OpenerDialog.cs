using EasyEntryApp.Components.Dialogs;
using MudBlazor;
using Color = Microsoft.Maui.Graphics.Color;

namespace doorOpener.Models;

public class OpenerDialog
{
        /// <summary>
        /// Displays a generic dialog with the specified parameters.
        /// </summary>
        /// <param name="descriptionText">The description text to be displayed in the dialog.</param>
        /// <param name="colorScheme">The color scheme of the dialog.</param>
        /// <param name="title">The title of the dialog.</param>
        /// <param name="acceptButtonText">The text for the accept button.</param>
        /// <param name="DialogService">The dialog service used to show the dialog.</param>
        /// <param name="cancelButtonText">The text for the cancel button (optional).</param>
        /// <returns>A task that represents the asynchronous operation. The task result contains the dialog reference.</returns>
        public static async Task<IDialogReference> ShowGenericDialog(string descriptionText, MudBlazor.Color colorScheme, string title, string acceptButtonText, IDialogService DialogService, string cancelButtonText = "")
        {
            var parameters = new DialogParameters();

            // Explicitly specify the type arguments for Add
            parameters.Add(nameof(GenericDialog.DescriptionText), descriptionText);
            parameters.Add(nameof(GenericDialog.ColorScheme), colorScheme);
            parameters.Add(nameof(GenericDialog.AcceptButtonText), acceptButtonText);

            if (!string.IsNullOrEmpty(cancelButtonText))
            {
                parameters.Add(nameof(GenericDialog.CancelButtonText), cancelButtonText);
            }

            return await DialogService.ShowAsync<GenericDialog>(title, parameters);
        }

}