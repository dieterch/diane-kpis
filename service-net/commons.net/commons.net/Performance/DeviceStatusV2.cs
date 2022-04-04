using GE.Myplant.Integration.NetCore.Utils;
using Newtonsoft.Json;
using Newtonsoft.Json.Converters;
using System;

namespace GE.Myplant.Integration.NetCore.Performance
{
    public class DeviceStatusV2 
    {
        public EngineAction ActionActual { get; set; } = EngineAction.Undefinded;

        [JsonConverter(typeof(MillisecondEpochConverter))]
        public DateTime? ActionFrom;
        [JsonConverter(typeof(MillisecondEpochConverter))]
        public DateTime? ActionTo;

        [JsonConverter(typeof(MillisecondEpochConverter))]
        public DateTime? TriggerDate;
        public long TriggerMSGNo;
        public String TriggerText;
        public String TriggerResponsibility;
        public long TriggerCount;

        [JsonProperty("dss")]
        public DemandSelectorSwitchStates DemandSelectorSwitch;
        [JsonProperty("sss")]
        public ServiceSelectorSwitchStates ServiceSelectorSwitch;
        [JsonProperty("avss")]
        public AvailableStates AV_MAN_Activated_Status;

        [JsonConverter(typeof(MillisecondEpochConverter))]
        public DateTime? CalcDate;
    }

}
