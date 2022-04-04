using System;
using System.Collections.Generic;
using System.Linq;
using GE.Myplant.Integration.NetCore.Alarm;
using GE.Myplant.Integration.NetCore.Performance;
using GE.Myplant.StateMachineV2;
using Microsoft.AspNetCore.Mvc;
using Microsoft.Extensions.Logging;
using Newtonsoft.Json;
using StateMachineCalculationService.Models;

namespace StateMachineCalculationService.Controllers
{
    [Route("api/[controller]")]
    [ApiController]
    public class StateController : ControllerBase
    {
        private readonly ILogger logger;

        public StateController(ILogger<HealthController> logger)
        {
            this.logger = logger;
        }

        // POST: api/State
        [HttpPost("{assetId}")]
        [DisableRequestSizeLimit]
        //[HttpPost("")]
        public List<DeviceStatusV2> Post(long assetId, [FromBody] List<MessageEvent> value)
        {
            //long assetId = 0;

            if (value != null && value.Count != 0)
                logger.LogInformation("calculate states for asset id {0} for {1} messages. {2} {3} {4} {5}", assetId, value.Count, value[0].Name, value[0].Number, value[0].Severity, value[0].Timestamp);
            else
            {
                logger.LogInformation("calculate states for asset id {0} for 0 messages.", assetId);
                return new List<DeviceStatusV2>();
            }

            var stateMachine = new StateMachinCalc();

            var result = stateMachine.doCalc(value);

            result.ForEach(a =>
            {
                if (a.TriggerDate == DateTime.MinValue)
                    a.TriggerDate = null;
                //if(a.AV_MAN_Activated_Status != AvailableStates.Undefined)
                //    logger.LogInformation("AV_MAN_Activated_Status {0} found for {1}", result.Count, assetId);
            });

            //var l = result.Where(a => a.AV_MAN_Activated_Status != AvailableStates.Undefined).ToList();

            logger.LogInformation("return {0} states for asset id {1}", result.Count, assetId);

            //var st = JsonConvert.SerializeObject(result);

            return result;
        }




        //// PUT: api/State/5
        //[HttpPut("{id}")]
        //public void Put(int id, [FromBody] string value)
        //{
        //}

        //// DELETE: api/ApiWithActions/5
        //[HttpDelete("{id}")]
        //public void Delete(int id)
        //{
        //}
    }
}
