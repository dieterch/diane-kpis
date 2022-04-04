using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using Microsoft.AspNetCore.Http;
using Microsoft.AspNetCore.Mvc;
using Microsoft.Extensions.Logging;

namespace StateMachineCalculationService.Controllers
{
    [Route("/")]
    [ApiController]
    public class HealthController : ControllerBase
    {
        [HttpGet("health")]
        public Dictionary<string, string> Get()
        {
            var res = new Dictionary<string, string>();
            res.Add("status","OK");
            return res;
        }
    }
}