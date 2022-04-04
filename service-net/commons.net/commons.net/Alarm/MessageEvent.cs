using Newtonsoft.Json;
using System;
using System.Collections.Generic;
using System.Text;

namespace GE.Myplant.Integration.NetCore.Alarm
{
    public class MessageEvent
    {
        [JsonProperty("t")]
        public long Timestamp { get; set; }

        [JsonProperty("no")]
        public int Number { get; set; }

        [JsonProperty("na")]
        public string Name { get; set; }

        [JsonProperty("s")]
        public int Severity { get; set; }

    }
}
