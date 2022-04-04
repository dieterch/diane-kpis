using GE.Myplant.Integration.NetCore.Alarm;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;

namespace StateMachineCalculationService.Models
{
    public class CalcStatusPostRequest
    {
        public long AssetId;
        public List<MessageEvent> messageEvents;
    }
}
