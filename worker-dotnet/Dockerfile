# Build stage
FROM mcr.microsoft.com/dotnet/sdk:8.0 AS build
WORKDIR /src
COPY ["worker-dotnet.csproj", "."]
RUN dotnet restore "./worker-dotnet.csproj"
COPY . .
RUN dotnet publish "./worker-dotnet.csproj" -c Release -o /app/publish

# Runtime stage
FROM mcr.microsoft.com/dotnet/runtime:8.0 AS runtime
WORKDIR /app
COPY --from=build /app/publish .
COPY appsettings.json .
ENTRYPOINT ["dotnet", "worker-dotnet.dll"]