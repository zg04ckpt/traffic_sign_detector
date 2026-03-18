using Microsoft.AspNetCore.SignalR;

namespace Server3.Hubs
{
    public class TrainingStatusHub : Hub
    {
        public override async Task OnConnectedAsync()
        {
            var trackingId = Context.GetHttpContext().Request.Query["trackingId"];

            await Groups.AddToGroupAsync(Context.ConnectionId, trackingId);

            await base.OnConnectedAsync();
        }
    }
}
