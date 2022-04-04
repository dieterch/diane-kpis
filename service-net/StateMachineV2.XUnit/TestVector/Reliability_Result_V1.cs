using System;
using System.Collections.Generic;
using System.Text;

namespace ClassLibStateMachine.UnitTestX.TestVector
{
    public partial class Reliability_Result_V1
    {
        public string jNumber { get; set; }
        public int assetId { get; set; }
        public string serialNumber { get; set; }
        public string model { get; set; }
        public DeviceStatusV1[] data { get; set; }

    }
}
