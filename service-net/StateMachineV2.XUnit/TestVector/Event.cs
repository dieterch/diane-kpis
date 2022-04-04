using System;
using System.Text;

namespace ClassLibStateMachine.UnitTestX.TestVector
{
    public class Event
    {
        public string name { get; set; }
        public long timestamp { get; set; }
        public int assetId { get; set; }
        public string message { get; set; }
        public int severity { get; set; }
    }
}
