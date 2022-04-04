using System;
using System.Collections.Generic;
using System.Text;

namespace GE.Myplant.Integration.NetCore.Performance
{
    public class DeviceStatus
    {
        public long DeviceId { get; set; }
        public StatusV1 OldStatus { get; set; } = StatusV1.UNDEFINED;
        public StatusV1 CurrentStatus { get; set; } = StatusV1.UNDEFINED;
        public DateTime From;
        public DateTime To;
        public String Trigger;
    }

}
