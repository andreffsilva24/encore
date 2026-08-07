## Build and publish the .NET application
FROM mcr.microsoft.com/dotnet/sdk:9.0 AS build
# Injects the host machine's CA certificates to handle corporate SSL inspection.
# Requires ca-certs.pem to be present in the api/ directory — see README for setup instructions.
COPY ca-certs.pem /usr/local/share/ca-certificates/ca-certs.crt
RUN apt-get update && apt-get install -y ca-certificates \
    && update-ca-certificates
WORKDIR /src
COPY src/EncoreApi.csproj .
RUN dotnet restore
COPY src/ .
RUN dotnet publish -c Release -o /app/publish

## Create the final runtime image
FROM mcr.microsoft.com/dotnet/aspnet:9.0 AS final
WORKDIR /app
EXPOSE 8080
COPY --from=build /app/publish .
ENTRYPOINT ["dotnet", "EncoreApi.dll"]