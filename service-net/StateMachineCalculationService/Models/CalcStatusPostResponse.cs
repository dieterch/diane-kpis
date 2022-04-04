using GE.Myplant.Integration.NetCore.Alarm;
using GE.Myplant.Integration.NetCore.Performance;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;

namespace StateMachineCalculationService.Models
{
    public class CalcStatusPostResponse
    {
        public long AssetId;
        public List<DeviceStatusV2> states;
    }
}
