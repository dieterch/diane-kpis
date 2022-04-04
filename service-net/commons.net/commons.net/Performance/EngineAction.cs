using System;
using System.Collections.Generic;
using System.Text;

namespace GE.Myplant.Integration.NetCore.Performance
{
    public enum EngineAction
    {
        Undefinded = 0,
        Data_GAP = 2,
        Start_Preparation = 4,
        Start = 6,
        Idle = 8,
        Synchronisation = 10,
        Operation = 12,
        RampUp_Mains_Parallel_Operation = 14,
        Mains_Parallel_Operation = 16,
        RampUp_Island_Operation = 18,
        Island_Operation = 20,
        Load_Rampdown = 22,
        Engine_Cooldown = 24,
        Ready = 26,
        Not_Ready = 28,
        Mains_Failure = 30,
        Forced_Outage = 32,
        Troubleshooting = 34,
    }

}
