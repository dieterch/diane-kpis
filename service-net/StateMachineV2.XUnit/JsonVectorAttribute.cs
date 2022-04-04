using ClassLibStateMachine.UnitTestX.TestVector;
using Newtonsoft.Json;
using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Reflection;
using System.Text;
using Xunit.Sdk;

namespace StateMachinev2.XUnit
{
    class JsonVectorAttribute : DataAttribute
    {
        private readonly int assetId;
        private readonly long fromTimeStamp;
        private readonly long toTimeStamp;

        public JsonVectorAttribute(int assetId, long fromTimeStamp, long toTimeStamp)
        {
            this.assetId = assetId;
            this.fromTimeStamp = fromTimeStamp;
            this.toTimeStamp = toTimeStamp;
        }
        public override IEnumerable<object[]> GetData(MethodInfo testMethod)
        {
            var c = Directory.GetCurrentDirectory();
            var pathEvents = Path.Combine(Directory.GetCurrentDirectory(), "TestVector\\" + assetId + "_Events.json");
            var pathResult = Path.Combine(Directory.GetCurrentDirectory(), "TestVector\\" + assetId + "_Reliability_Result_V1.json");

            if (!File.Exists(pathEvents))
            {
                throw new ArgumentException($"Could not find file at path: {pathEvents}");
            }
            if (!File.Exists(pathResult))
            {
                throw new ArgumentException($"Could not find file at path: {pathResult}");
            }

            var vector = new Vector_V1();
            var fileEventsData = File.ReadAllText(pathEvents);
            var fileResultData = File.ReadAllText(pathResult);
            vector.events = JsonConvert.DeserializeObject<List<Event>>(fileEventsData).OrderBy(e => e.timestamp).ToList(); ;//.Where(e => e.timestamp >= fromTimeStamp && e.timestamp <= toTimeStamp).ToList();
            var resultData = JsonConvert.DeserializeObject<Reliability_Result_V1>(fileResultData);

            vector.assetId = resultData.assetId;
            vector.jNumber = resultData.jNumber;
            vector.model = resultData.model;
            vector.serialNumber = resultData.serialNumber;
            vector.deviceStatus = resultData.data; //.Where(e => e. >= fromTimeStamp && e.timestamp <= toTimeStamp).ToList();

            var list = new List<object[]>();
            var vectorList = new List<Vector_V1>();
            vectorList.Add(vector);
            list.Add(vectorList.ToArray());
            return list;
        }
    }
}
