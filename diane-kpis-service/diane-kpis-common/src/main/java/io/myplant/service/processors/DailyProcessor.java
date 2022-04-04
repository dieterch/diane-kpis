package io.myplant.service.processors;

import io.myplant.service.StateKpiCalculation.StateUtils;
import io.myplant.domain.DeviceState;
import io.myplant.model.SplitType;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class DailyProcessor {
    public List<DeviceState> processStates(List<DeviceState> deviceStatusList){
        return processStates(deviceStatusList, SplitType.Daily);
    }

    public List<DeviceState> processStates(List<DeviceState> deviceStatusList, SplitType splitType){
        List<DeviceState> dailyList = new ArrayList<>();

        for (DeviceState state : deviceStatusList) {
            long from = state.getActionFrom();
            long endOfSplit = getEndTimeStamp(from, splitType); //StateUtils.getTimestampEndOfDay(from);
            do {
                DeviceState newStateForNextDay =
                        state.toBuilder()
                                .actionFrom(from)
                                .actionTo(
                                        Math.min(state.getActionTo(), endOfSplit))
                                .build();
                dailyList.add(newStateForNextDay);

                if (state.getActionTo() < endOfSplit) break;

                from = endOfSplit + 1; // start with next day
                endOfSplit = getEndTimeStamp(from, splitType); //StateUtils.getTimestampEndOfDay(from);

            } while (true);
        }
        return dailyList;
    }

    private long getEndTimeStamp(long from, SplitType splitType){
        if(splitType == SplitType.Monthly)
            return StateUtils.getTimestampEndOfDay(from);// StateUtils.getTimestampEndOfMonth(from);
        return  StateUtils.getTimestampEndOfDay(from);
    }
}
