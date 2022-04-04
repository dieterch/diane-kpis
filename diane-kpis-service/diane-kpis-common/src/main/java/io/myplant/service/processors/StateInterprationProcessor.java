package io.myplant.service.processors;

import io.myplant.Utils;
import io.myplant.domain.AssetInformation;
import io.myplant.domain.DeviceState;
import io.myplant.model.*;
import io.myplant.service.ScopeMapperService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StateInterprationProcessor {
    private static final Logger logger = LoggerFactory.getLogger(StateInterprationProcessor.class);

    private final ScopeMapperService scopeService;
    private static final long MIN30_TIMESPAN = 30 * 60 * 1000L;
    private static final long MIN60_TIMESPAN = 60 * 60 * 1000L;


    public List<DeviceState> processStates(
            long assetId,
            List<DeviceState> deviceStates,
            AssetInformation assetInformation) {

        long monitoringStartDate = Utils.getMonitoringStart(assetId, assetInformation);

        logger.info("assetId " + assetId + ": Start RAM Monitoring: " + ((monitoringStartDate == 0) ? "disabled" : Utils.getTimeGmt(monitoringStartDate)));

        DeviceState lastState = null;

        long startOfVzState = 0;
        IeeeStates lastVzState = IeeeStates.UNDEFINED;
        List<DeviceState> statesToInsert = new ArrayList<>();

        DeviceState lastOutageState = null;
        List<DeviceState> statesBetweenOutages = new ArrayList<>();
        long outageNumber = 1;

        boolean remotelyControlledAsset = !(assetInformation == null || assetInformation.getAvCalcType() == null || assetInformation.getAvCalcType() == 0);

        for (DeviceState state : deviceStates) {
            AvailableStates availableState = AvailableStateCalculation.calculateAvailableState(assetId, state.getAws()
                    , state.getBws(), state.getAvss()
                    , state.getActionActual(), remotelyControlledAsset);

            state.setAvailableState(availableState);

            IeeeStates actualIeeeState;
            if (monitoringStartDate == 0 || state.getActionFrom() < monitoringStartDate) {
                actualIeeeState = IeeeStates.DEACTIVATED_SHUTDOWN;
            } else {
                IeeeStates lastOutageIeeeState = null;
                if (lastState != null && IeeeStates.isOutage(lastState.getIeeeState())) {
                    lastOutageIeeeState = lastState.getIeeeState();
                }

                actualIeeeState = IeeeStateCalculator.calcIeeeState(
                        availableState,
                        state.getActionActual(),
                        (lastState == null) ? AvailableStates.UNDEFINED : lastState.getAvailableState(),
                        lastOutageIeeeState,
                        (lastState == null) ? EngineAction.UNDEFINED : lastState.getActionActual()
                );
            }

            // vu state
            state.setKielVuState(actualIeeeState);
            state.setIeeeState(actualIeeeState);

            if (IeeeStates.isOutage(actualIeeeState)) {
                state.setScope(scopeService.getScope(state.getTriggerMsgNo(), actualIeeeState));
                if (lastState != null && OutagesProcessor.isSameOutage(state, lastState, StateType.IEEE)) {
                    state.setOutageNumber(lastState.getOutageNumber());
                    state.setScope(lastState.getScope());
                } else {
                    // new outage
                    state.setOutageNumber(outageNumber++);
                }
            }

            // outage joining (break detection)
            if (lastOutageState == null && IeeeStates.isOutage(actualIeeeState)) {
                // start break detection
                lastOutageState = state;
                statesBetweenOutages = new ArrayList<>();
            } else if (!IeeeStates.isOutage(actualIeeeState) && lastOutageState != null) {
                // no outage, could be in the gap
                long timespanBetweenOutages = state.getActionFrom() - lastOutageState.getActionFrom();
                if (timespanBetweenOutages < MIN30_TIMESPAN) {
                    statesBetweenOutages.add(state);
                } else {
                    lastOutageState = null;
                    statesBetweenOutages = new ArrayList<>();
                }
            } else if (IeeeStates.isOutage(actualIeeeState) && lastOutageState != null) {
                // check if we have other states in the gap
                long timespanBetweenOutages = state.getActionFrom() - lastOutageState.getActionFrom();
                if (statesBetweenOutages.size() != 0
                        && timespanBetweenOutages < MIN30_TIMESPAN
                        && state.getTriggerMsgNo() == lastOutageState.getTriggerMsgNo()) {

                    IeeeStates lastOutageIeeeState = lastOutageState.getIeeeState();
                    ScopeType lastOutageScope = lastOutageState.getScope();
                    long lastOutageNumber = lastOutageState.getOutageNumber();
                    statesBetweenOutages.forEach(s -> {
                        s.setIeeeState(lastOutageIeeeState);
                        s.setScope(lastOutageScope);
                        s.setOutageNumber(lastOutageNumber);
                    });
                } else {
                    lastOutageState = state;
                    statesBetweenOutages = new ArrayList<>();
                }
            }

            // Vz check
            if (IeeeStates.isOutage(actualIeeeState)) {
                if (startOfVzState == 0) {
                    startOfVzState = state.getActionFrom();
                    state.setKielVzState(actualIeeeState);
                    lastVzState = actualIeeeState;
                }
            }

            if (startOfVzState == 0) {
                state.setKielVzState(actualIeeeState);
            } else if (state.getActionFrom() < (MIN60_TIMESPAN + startOfVzState)) {
                if (state.getActionTo() <= (MIN60_TIMESPAN + startOfVzState)) {
                    state.setKielVzState(lastVzState);
                    if (state.getIeeeState() == IeeeStates.AVAILABLE && lastOutageState != null && lastOutageState.getIeeeState() == IeeeStates.FORCEDOUTAGE_MTBFO_REL ) {
                        state.setIeeeState(lastOutageState.getIeeeState());
                    }
                } else {
                    // create an state to insert because we must split the Vz state
                    long splitTimeStamp = MIN60_TIMESPAN + startOfVzState;
                    DeviceState state2Insert = state.toBuilder().actionTo(splitTimeStamp).kielVzState(lastVzState).build();
                    statesToInsert.add(state2Insert);
                    state.setActionFrom(splitTimeStamp);
                    state.setKielVzState(actualIeeeState);
                }
            } else {
                state.setKielVzState(actualIeeeState);
                startOfVzState = 0;
            }

            // check missing causal alarm
            if (IeeeStates.isOutage(state.getIeeeState()) && lastOutageState != null && state.getTriggerMsgNo() == 0) {
                state.setTriggerMsgNo(lastOutageState.getTriggerMsgNo());
            }

            // set scope
            if (IeeeStates.isOutage(state.getIeeeState())) {
                //state.setScope(scopeService.getScope(state.getTriggerMsgNo(), state.getIeeeState()));
                if ((state.getScope() == ScopeType.None || state.getScope() == null) && lastState != null) {
                    state.setScope(lastState.getScope());
                }
                if (state.getScope() == ScopeType.None || state.getScope() == null) {
                    state.setScope(ScopeType.Unclear);
                }
            } else {
                state.setScope(ScopeType.None);
            }

            lastState = state;
        }

        if (statesToInsert.size() != 0) {
            deviceStates.addAll(statesToInsert);
            deviceStates.sort(null);
        }

        return deviceStates;
    }
}