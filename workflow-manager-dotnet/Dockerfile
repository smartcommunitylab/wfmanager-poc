# Build stage
FROM mcr.microsoft.com/dotnet/sdk:8.0 AS build
WORKDIR /src
COPY ["workflow-manager-dotnet.csproj", "."]
RUN dotnet restore "./workflow-manager-dotnet.csproj"
COPY . .
RUN dotnet publish "./workflow-manager-dotnet.csproj" -c Release -o /app/publish

# Runtime stage
FROM mcr.microsoft.com/dotnet/aspnet:8.0 AS runtime
WORKDIR /app
COPY --from=build /app/publish .
COPY appsettings.json .
EXPOSE 8080
ENTRYPOINT ["dotnet", "workflow-manager-dotnet.dll"]