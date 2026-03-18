using System.Text.Json;
using Microsoft.AspNetCore.Mvc;

namespace Server3.Middlewares
{
    public class GlobalExceptionCatchingMiddleware : IMiddleware
    {
        public async Task InvokeAsync(HttpContext context, RequestDelegate next)
        {
            try
            {
                await next(context);
            }
            catch (Exception ex)
            {
                if (context.Response.HasStarted)
                {
                    throw;
                }

                var problem = new ProblemDetails
                {
                    Title = "Internal Server Error",
                    Detail = ex.Message,
                    Status = StatusCodes.Status500InternalServerError,
                    Instance = context.Request.Path
                };

                context.Response.StatusCode = StatusCodes.Status500InternalServerError;
                context.Response.ContentType = "application/problem+json";

                var json = JsonSerializer.Serialize(problem);
                await context.Response.WriteAsync(json);

                Console.WriteLine(ex.Message);
            }
        }
    }
}
