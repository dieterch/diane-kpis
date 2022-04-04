package io.myplant.service.processors;

import io.myplant.domain.DeviceState;
import io.myplant.model.EngineAction;
import io.myplant.model.IeeeStates;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OperatingHourProcessor {

    public List<DeviceState> processStates(List<DeviceState> deviceStates) {
        for (int i = 0; i < deviceStates.size(); i++) {
            DeviceState checkingState = deviceStates.get(i);
            DeviceState lastState = null;

            if ((i - 1) >= 0) {
                lastState = deviceStates.get(i - 1);
            }

            // OH
            if (EngineAction.isOperating(checkingState.getActionActual()))
                checkingState.setOH(checkingState.getDuration());
            else
                checkingState.setOH(0L);

            if (lastState != null)
                checkingState.setCumOH(lastState.getCumOH() + checkingState.getOH());
            else
                checkingState.setCumOH(checkingState.getOH());

            // AOH
            if (EngineAction.isOperating(checkingState.getActionActual()) && checkingState.getIeeeState() == IeeeStates.AVAILABLE)
                checkingState.setAOH(checkingState.getDuration());
            else
                checkingState.setAOH(0L);

            if (lastState != null)
                checkingState.setCumAOH(lastState.getCumAOH() + checkingState.getAOH());
            else
                checkingState.setCumAOH(checkingState.getAOH());

            // PH
            if (checkingState.getIeeeState() != IeeeStates.DEACTIVATED_SHUTDOWN)
                checkingState.setPH(checkingState.getDuration());
            else
                checkingState.setPH(0L);

            if (lastState != null)
                checkingState.setCumPH(lastState.getCumPH() + checkingState.getPH());
            else
                checkingState.setCumPH(checkingState.getPH());

            if (checkingState.getIeeeState() == IeeeStates.FORCEDOUTAGE_MTBFO_REL)
                checkingState.setOHatLastFOO(checkingState.getCumAOH());
            else if (lastState != null)
                checkingState.setOHatLastFOO(lastState.getOHatLastFOO());

            if (lastState != null)
                checkingState.setHSLF(checkingState.getCumAOH() - checkingState.getOHatLastFOO());
        }
        return deviceStates;
    }
}
