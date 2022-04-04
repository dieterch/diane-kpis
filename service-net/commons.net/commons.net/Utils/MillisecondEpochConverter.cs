using Newtonsoft.Json;
using Newtonsoft.Json.Converters;
using System;
using System.Collections.Generic;
using System.Text;

namespace GE.Myplant.Integration.NetCore.Utils
{
    public class MillisecondEpochConverter : DateTimeConverterBase
    {
        public override void WriteJson(JsonWriter writer, object value, JsonSerializer serializer)
        {
            if(value != null)
                writer.WriteRawValue(((DateTime)value).ToUnixTimeMilli().ToString());
        }

        public override object ReadJson(JsonReader reader, Type objectType, object existingValue, JsonSerializer serializer)
        {
            if (reader.Value == null) { return null; }
                return ((long)reader.Value).FromUnixTimeMilli();
        }
    }
}
