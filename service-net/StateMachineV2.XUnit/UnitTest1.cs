using ClassLibStateMachine.UnitTestX.TestVector;
using GE.Myplant.Integration.NetCore.Alarm;
using GE.Myplant.StateMachineV2;
using StateMachinev2.XUnit;
using System.Linq;
using Xunit;

namespace StateMachine.XUnit
{
    public class UnitTest1
    {
        private readonly StateMachinCalc stateMachine = new StateMachinCalc();

        [Theory]
        [JsonVector(65316, 0, 0)]
        public void Test1(Vector_V1 vector)
        {

            var messages = vector.events
                .Where(e => int.TryParse(e.name, out int m))
                .Select(e => new MessageEvent { Number = int.Parse(e.name), Name = e.message, Severity = e.severity, Timestamp = e.timestamp }).ToList();
            var states = stateMachine.doCalc(messages);

            bool b = false;
            if (states.Count != vector.deviceStatus.Length)
            {
                b = true;
            }

            //stateMachine.CalculateOrg();
        }
    }
}
