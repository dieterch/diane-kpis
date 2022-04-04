namespace GE.Myplant.Integration.NetCore.Performance
{
    public enum StatusV1
    {
        UNDEFINED = 0,
        BLOCKSTART = 1,
        BWS_MAN = 2,
        BWS_MAN_TRIP = 3,
        BWS_MAN_OPERATING= 4,
        BWS_MAN_OPERATING_TRIP= 5,
        OPERATING= 6,
        READY= 7,
        UNPLANNED_STANDSTILL= 8,
        UNPLANNED_STANDSTILL_TROUBLESHOOTING= 9,
        GAP= 10
    }
}
