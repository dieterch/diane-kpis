FROM mcr.microsoft.com/dotnet/core/aspnet:3.1
ADD ./ /app
WORKDIR /app
COPY out/ /app
EXPOSE 8080
ENV ASPNETCORE_URLS=http://*:8080
WORKDIR /app
ENTRYPOINT [ "dotnet", "StateMachineCalculationService.dll"]
