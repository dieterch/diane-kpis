using System.Collections.Generic;

namespace ClassLibStateMachine.UnitTestX.TestVector
{
    public class Vector_V1
    {
        public long fromTimeStamp { get; set; }
        public long toTimeStamp { get; set; }
        public List<Event> events { get; set; }

        public string jNumber { get; set; }
        public int assetId { get; set; }
        public string serialNumber { get; set; }
        public string model { get; set; }
        public DeviceStatusV1[] deviceStatus { get; set; }
    }
}
