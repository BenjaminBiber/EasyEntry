using EasyEntryApi;
using Microsoft.EntityFrameworkCore;
using MySqlConnector;

var builder = WebApplication.CreateBuilder(args);

// Add services to the container.

builder.Services.AddControllers();
// Learn more about configuring Swagger/OpenAPI at https://aka.ms/aspnetcore/swashbuckle
builder.Services.AddEndpointsApiExplorer();
builder.Services.AddSwaggerGen();

var connectionString = new MySqlConnectionStringBuilder
{
    Server = Environment.GetEnvironmentVariable("DB_SERVER") ?? "157.180.82.47",
    UserID = Environment.GetEnvironmentVariable("DB_User") ?? "root",
    Password = Environment.GetEnvironmentVariable("DB_Password") ?? "4cows",
    Database = Environment.GetEnvironmentVariable("DB_DB") ?? "miniplaner",
    Port = 3306,
}.ConnectionString;

builder.Services.AddDbContext<AppDbContext>(options =>
    options.UseMySql(
        connectionString,
        ServerVersion.AutoDetect(builder.Configuration.GetConnectionString("Default"))
    ));

var app = builder.Build();

using (var scope = app.Services.CreateScope())
{
    var dbContext = scope.ServiceProvider.GetRequiredService<AppDbContext>();
    dbContext.Database.Migrate();
}

// Configure the HTTP request pipeline.
if (app.Environment.IsDevelopment())
{
    app.UseSwagger();
    app.UseSwaggerUI();
}

app.UseHttpsRedirection();

app.UseAuthorization();

app.MapControllers();

app.Run();