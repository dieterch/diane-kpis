using System;
using Xunit;
using GE.Myplant.Integration.NetCore.Utils;

namespace Integration.NetCore.XUnit
{
    public class DateTimeExtTest
    {
        [Fact]
        public void TestFromUnixTimeOk()
        {

            long t1 = 1540381392L;
            var res1 = t1.FromUnixTime();
            var expected = new DateTime(2018, 10, 24, 11, 43, 12, DateTimeKind.Utc);
            Assert.Equal(expected, res1);
        }

        [Fact]
        public void TestFromUnixTimeNOk()
        {

            long t1 = 1540381392L;
            var res1 = t1.FromUnixTime();
            Assert.NotEqual(res1, new DateTime(2017, 10, 24, 11, 43, 11, DateTimeKind.Utc));
        }

        [Fact]
        public void TestToUnixTimeOk()
        {
            long res = new DateTime(2018, 10, 24, 11, 43, 12, DateTimeKind.Utc).ToUnixTime();
            long expected = 1540381392L;
            Assert.Equal(expected, res);
        }

    }
}
