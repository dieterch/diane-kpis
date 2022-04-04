using System;
using System.Collections.Generic;
using System.Text;

namespace GE.Myplant.Integration.NetCore.Utils
{
    public static class DateTimeExt
    {
        private static readonly DateTime epoch = new DateTime(1970, 1, 1, 0, 0, 0, DateTimeKind.Utc);

        public static DateTime FromUnixTime(this long unixTime)
        {
            return epoch.AddSeconds(unixTime);
        }

        public static long ToUnixTime(this DateTime date)
        {
            return Convert.ToInt64((date - epoch).TotalSeconds);
        }

        public static DateTime FromUnixTimeMilli(this long unixTime)
        {
            return epoch.AddMilliseconds(unixTime);
        }

        public static long ToUnixTimeMilli(this DateTime date)
        {
            return Convert.ToInt64((date - epoch).TotalMilliseconds);
        }

    }
}
