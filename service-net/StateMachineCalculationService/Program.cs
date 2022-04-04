using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Threading.Tasks;
using Microsoft.AspNetCore;
using Microsoft.AspNetCore.Hosting;
using Microsoft.Extensions.Configuration;
using Microsoft.Extensions.Logging;
using Serilog;
using Serilog.Sinks.Elasticsearch;

namespace StateMachineCalculationService
{
    public class Program
    {
        public static void Main(string[] args)
        {
            CreateWebHostBuilder(args).Build().Run();
        }

        public static IWebHostBuilder CreateWebHostBuilder(string[] args) =>
            WebHost.CreateDefaultBuilder(args)
                .UseSerilog((ctx, config) =>
                {
                    config
                        .MinimumLevel.Information()
                        .Enrich.FromLogContext();

                    config.WriteTo.Console();
                    //if (ctx.HostingEnvironment.IsDevelopment())
                    //{
                    //    config.WriteTo.Console();
                    //}
                    //else
                    //{
                    //    config.WriteTo.Console(new ElasticsearchJsonFormatter());
                    //}
                })
                .UseStartup<Startup>()
                .ConfigureKestrel((context, options) =>
                {
                  options.Limits.MaxRequestBodySize = null;
                });
    }
}
