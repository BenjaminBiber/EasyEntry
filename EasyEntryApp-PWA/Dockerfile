FROM mcr.microsoft.com/dotnet/aspnet:8.0 AS base
USER $APP_UID
WORKDIR /app
EXPOSE 8080
EXPOSE 8081

FROM mcr.microsoft.com/dotnet/sdk:8.0 AS build
ARG BUILD_CONFIGURATION=Release
WORKDIR /src

RUN echo "Inhalt von /src vor COPY:" && ls -la /src

COPY ["EasyEntryApp-PWA/EasyEntryAppPWA/EasyEntryApp.csproj", "EasyEntryApp/"]
COPY ["EasyEntryApp-PWA/EasyEntryLib/EasyEntryLib.csproj", "EasyEntryLib/"]
RUN dotnet restore "EasyEntryApp/EasyEntryAppPWA.csproj"
COPY . .
WORKDIR "/src/EasyEntryApp"
RUN dotnet build "EasyEntryApp/EasyEntryAppPWA.csproj" -c $BUILD_CONFIGURATION -o /app/build

FROM build AS publish
ARG BUILD_CONFIGURATION=Release
RUN dotnet publish "EasyEntryApp/EasyEntryAppPWA.csproj" -c $BUILD_CONFIGURATION -o /app/publish /p:UseAppHost=false

FROM base AS final
WORKDIR /app
COPY --from=publish /app/publish .
ENTRYPOINT ["dotnet", "EasyEntryAppPWA.dll"]
