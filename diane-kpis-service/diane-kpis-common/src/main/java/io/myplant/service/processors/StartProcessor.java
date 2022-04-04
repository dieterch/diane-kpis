package io.myplant.service.processors;

import io.myplant.Utils;
import io.myplant.domain.AssetInformation;
import io.myplant.domain.DeviceState;
import io.myplant.domain.Start;
import io.myplant.model.AvailableStates;
import io.myplant.model.EngineAction;
import io.myplant.model.IeeeStates;
import io.myplant.model.ScopeType;
import io.myplant.service.ScopeMapperService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class StartProcessor {

    private final ScopeMapperService scopeService = new ScopeMapperService();

    // start sequence maybe sometimes different (even the same engine)
    private EngineAction[] validStartSequence = new EngineAction[]{EngineAction.START_PREPARATION, EngineAction.START, EngineAction.IDLE
            , EngineAction.SYNCHRONISATION, EngineAction.RAMPUP_MAINS_PARALLEL_OPERATION, EngineAction.MAINS_PARALLEL_OPERATION};

    private EngineAction[] validStartSequence2 = new EngineAction[]{EngineAction.START_PREPARATION, EngineAction.START, EngineAction.IDLE
            , EngineAction.SYNCHRONISATION, EngineAction.RAMPUP_ISLAND_OPERATION, EngineAction.ISLAND_OPERATION};

    private EngineAction[] validStartSequence3 = new EngineAction[]{EngineAction.START_PREPARATION, EngineAction.START, EngineAction.IDLE,
            EngineAction.RAMPUP_ISLAND_OPERATION, EngineAction.ISLAND_OPERATION};

    private EngineAction[] validStartSequence4 = new EngineAction[]{EngineAction.START_PREPARATION, EngineAction.START, EngineAction.IDLE
            , EngineAction.RAMPUP_MAINS_PARALLEL_OPERATION, EngineAction.MAINS_PARALLEL_OPERATION};

    private List<EngineAction> cancelEngineActions = Arrays.asList(EngineAction.LOAD_RAMPDOWN, EngineAction.ENGINE_COOLDOWN, EngineAction.READY,
            EngineAction.DATA_GAP, EngineAction.MAINS_FAILURE, EngineAction.NOT_READY, EngineAction.UNDEFINED);


    public List<Start> run(long assetId, List<DeviceState> deviceStates, AssetInformation assetInformation) {

        List<Start> starts = new ArrayList<>();
        long monitoringStartDate = Utils.getMonitoringStart(assetId, assetInformation);

        Start checkingStart = null;
        int sequenceCount = 0;
        int sequenceCount2 = 0;
        int sequenceCount3 = 0;
        int sequenceCount4 = 0;

        EngineAction lastActionActual = null;
        boolean rampUpMainsParallelReached = false;
        boolean isForcedOutage = false;
        boolean canceledStart = false;

        for (int i = 0; i < deviceStates.size(); i++) {
            DeviceState state = deviceStates.get(i);
            DeviceState nextState = null;

            if ((i + 1) < deviceStates.size()) {
                nextState = deviceStates.get(i + 1);
            }

            if (checkingStart == null && state.getActionActual() == validStartSequence[0]) {
                checkingStart = new Start(assetId, state.getActionFrom());
                lastActionActual = state.getActionActual();

                if (state.getIeeeState() == IeeeStates.FORCEDOUTAGE_MTBFO_REL || state.getIeeeState() == IeeeStates.FORCEDOUTAGE_REL) {
                    isForcedOutage = true;
                }

                if (sequenceCount <= 2) {// because first three items in all sequences are equal
                    sequenceCount2++;
                    sequenceCount3++;
                    sequenceCount4++;
                }
                sequenceCount++;
                continue;
            }

            // Ignore duplicate actions, occurs after midnight
            if (checkingStart == null || state.getActionActual() == lastActionActual) {
                continue;
            }

            if (state.getActionActual() == validStartSequence[sequenceCount]
                    && state.getIeeeState() != IeeeStates.FORCEDOUTAGE_MTBFO_REL && state.getIeeeState() != IeeeStates.FORCEDOUTAGE_REL) {
                lastActionActual = state.getActionActual();
                if (sequenceCount <= 2) {// because first three items in all sequences are equal
                    sequenceCount2++;
                    sequenceCount3++;
                    sequenceCount4++;
                } else if (sequenceCount == 3) {// because the forth element in sequence 1 and 2 are equal
                    sequenceCount2++;
                }
                sequenceCount++;

                if (lastActionActual == EngineAction.RAMPUP_MAINS_PARALLEL_OPERATION) {
                    rampUpMainsParallelReached = true;
                }

                // Bugfix Issue 116 - Added check for ieee-state in case it was changed to UNPLANNED_MAINTENANCE, which means excluded start
                if (state.getIeeeState() != IeeeStates.UNPLANNED_MAINTENANCE
                        && state.getIeeeState() != IeeeStates.DEACTIVATED_SHUTDOWN
                        && state.getIeeeState() != IeeeStates.PILOT_ACTIVITIES
                        && state.getIeeeState() != IeeeStates.PLANNED_OUTAGE) {
                    if (sequenceCount != validStartSequence.length) {
                        continue;
                    }

                    // finished okay
                    checkingStart.setTimeToMainsParallel(state.getActionFrom() - checkingStart.getStartDate());
                } else {
                    canceledStart = true;
                }
            } else if (state.getActionActual() == validStartSequence2[sequenceCount2]
                    && state.getIeeeState() != IeeeStates.FORCEDOUTAGE_MTBFO_REL && state.getIeeeState() != IeeeStates.FORCEDOUTAGE_REL) {
                lastActionActual = state.getActionActual();
                sequenceCount2++;

                if (lastActionActual == EngineAction.RAMPUP_ISLAND_OPERATION) {
                    rampUpMainsParallelReached = true;
                }

                // Bugfix Issue 116 - Added check for ieee-state in case it was changed to UNPLANNED_MAINTENANCE, which means excluded start
                if (state.getIeeeState() != IeeeStates.UNPLANNED_MAINTENANCE
                        && state.getIeeeState() != IeeeStates.DEACTIVATED_SHUTDOWN
                        && state.getIeeeState() != IeeeStates.PILOT_ACTIVITIES
                        && state.getIeeeState() != IeeeStates.PLANNED_OUTAGE) {
                    if (sequenceCount2 != validStartSequence2.length) {
                        continue;
                    }

                    // finished okay
                    checkingStart.setTimeToMainsParallel(state.getActionFrom() - checkingStart.getStartDate());
                } else {
                    canceledStart = true;
                }
            } else if (state.getActionActual() == validStartSequence3[sequenceCount3]
                    && state.getIeeeState() != IeeeStates.FORCEDOUTAGE_MTBFO_REL && state.getIeeeState() != IeeeStates.FORCEDOUTAGE_REL) {
                lastActionActual = state.getActionActual();
                sequenceCount3++;

                if (lastActionActual == EngineAction.RAMPUP_ISLAND_OPERATION) {
                    rampUpMainsParallelReached = true;
                }

                // Bugfix Issue 116 - Added check for ieee-state in case it was changed to UNPLANNED_MAINTENANCE, which means excluded start
                if (state.getIeeeState() != IeeeStates.UNPLANNED_MAINTENANCE
                        && state.getIeeeState() != IeeeStates.DEACTIVATED_SHUTDOWN
                        && state.getIeeeState() != IeeeStates.PILOT_ACTIVITIES
                        && state.getIeeeState() != IeeeStates.PLANNED_OUTAGE) {
                    if (sequenceCount3 != validStartSequence3.length) {
                        continue;
                    }

                    // finished okay
                    checkingStart.setTimeToMainsParallel(state.getActionFrom() - checkingStart.getStartDate());
                } else {
                    canceledStart = true;
                }
            } else if (sequenceCount4 < validStartSequence4.length && state.getActionActual() == validStartSequence4[sequenceCount4]) {
                lastActionActual = state.getActionActual();
                sequenceCount4++;

                if (lastActionActual == EngineAction.RAMPUP_MAINS_PARALLEL_OPERATION) {
                    rampUpMainsParallelReached = true;
                }

                // Bugfix Issue 116 - Added check for ieee-state in case it was changed to UNPLANNED_MAINTENANCE, which means excluded start
                if (state.getIeeeState() != IeeeStates.UNPLANNED_MAINTENANCE
                        && state.getIeeeState() != IeeeStates.DEACTIVATED_SHUTDOWN
                        && state.getIeeeState() != IeeeStates.PILOT_ACTIVITIES
                        && state.getIeeeState() != IeeeStates.PLANNED_OUTAGE) {
                    if (sequenceCount4 != validStartSequence4.length) {
                        continue;
                    }

                    // finished okay
                    checkingStart.setTimeToMainsParallel(state.getActionFrom() - checkingStart.getStartDate());
                } else {
                    canceledStart = true;
                }
            } else {
                // no successful start
                checkingStart.setTimeToMainsParallel(0);

                if (state.getIeeeState() == IeeeStates.FORCEDOUTAGE_REL || state.getIeeeState() == IeeeStates.FORCEDOUTAGE_MTBFO_REL) {
                    // trip occured
                    checkingStart.setFailedStart(1);
                    checkingStart.setTriggerMSGNo(state.getTriggerMsgNo());

                    if (rampUpMainsParallelReached) {
                        checkingStart.setTripsBeforeMainsParallel(1);
                    } else {
                        checkingStart.setTripsBeforeRampUpMainsParallel(1);
                    }
                } else if (!cancelEngineActions.contains(state.getActionActual())) {
                    // trip occured
                    checkingStart.setFailedStart(1);
                    checkingStart.setTriggerMSGNo(state.getTriggerMsgNo());

                    if (rampUpMainsParallelReached) {
                        checkingStart.setTripsBeforeMainsParallel(1);
                    } else {
                        checkingStart.setTripsBeforeRampUpMainsParallel(1);
                    }
                } else {
                    canceledStart = true;
                }
            }

            // check if we should count the start
            // reason 2
            if (isForcedOutage) {
                checkingStart.setReason("Troubleshooting / maintenance / deactivated shutdown");
                checkingStart.setExcluded(1);
                checkingStart.setExcludedVu(1);
                checkingStart.setValidStart(0);
                checkingStart.setValidStartGCB(0);
            } else if (canceledStart && !rampUpMainsParallelReached) {
                checkingStart.setReason("Test run or canceled for UNK reason");
                checkingStart.setExcluded(1);
                checkingStart.setExcludedVu(1);
                checkingStart.setValidStart(0);
                checkingStart.setValidStartGCB(0);

            } else if (canceledStart && rampUpMainsParallelReached) {
                checkingStart.setReason("Test run or canceled for UNK reason");
                checkingStart.setExcluded(0);
                checkingStart.setExcludedVu(1);
                checkingStart.setValidStart(0);
                checkingStart.setValidStartGCB(1);
            } else if (!rampUpMainsParallelReached && (state.getIeeeState() == IeeeStates.FORCEDOUTAGE_REL || state.getIeeeState() == IeeeStates.FORCEDOUTAGE_MTBFO_REL)) {
                checkingStart.setExcluded(1);
                checkingStart.setExcludedVu(1);
                checkingStart.setValidStart(0);
                checkingStart.setValidStartGCB(0);
            }
            // reason 1a
            else if (!rampUpMainsParallelReached && checkingStart.getFailedStart() == 0 && (state.getIeeeState() != IeeeStates.AVAILABLE
                    || state.getAvailableState() == AvailableStates.NOT_AVAILABLE
                    || state.getAvailableState().isTroubleshooting()
                    || state.getAvailableState().isMaintenance())) {
                checkingStart.setReason("Troubleshooting / maintenance / deactivated shutdown");
                checkingStart.setExcluded(1);
                checkingStart.setExcludedVu(1);
                checkingStart.setValidStart(0);
                checkingStart.setValidStartGCB(0);
            } else if (rampUpMainsParallelReached && checkingStart.getFailedStart() == 0 && (state.getIeeeState() != IeeeStates.AVAILABLE
                    || state.getAvailableState() == AvailableStates.NOT_AVAILABLE
                    || state.getAvailableState().isTroubleshooting()
                    || state.getAvailableState().isMaintenance())) {
                checkingStart.setReason("Troubleshooting / maintenance / deactivated shutdown");
                checkingStart.setExcluded(0);
                checkingStart.setExcludedVu(1);
                checkingStart.setValidStart(0);
                checkingStart.setValidStartGCB(1);
            }
            // reason 1b
            else if (!rampUpMainsParallelReached && state.getAvailableState() != null && state.getAvailableState().isDeactivated()) {
                checkingStart.setReason("Deactivated");
                checkingStart.setValidStart(0);
                checkingStart.setValidStartGCB(0);
                checkingStart.setExcluded(1);
                checkingStart.setExcludedVu(1);

            } else if (rampUpMainsParallelReached && state.getAvailableState() != null && state.getAvailableState().isDeactivated()) {
                checkingStart.setReason("Deactivated");
                checkingStart.setExcluded(0);
                checkingStart.setExcludedVu(1);
                checkingStart.setValidStart(0);
                checkingStart.setValidStartGCB(1);
            }
            // reason 3
            else if (!rampUpMainsParallelReached && (state.getScope() == ScopeType.Partner || state.getScope() == ScopeType.Customer || state.getScope() == ScopeType.INNIO_BOP)) {
                checkingStart.setReason("Customer/Partner/BOP responsibility");
                checkingStart.setExcluded(1);
                checkingStart.setExcludedVu(1);
                checkingStart.setValidStart(0);
                checkingStart.setValidStartGCB(0);
            } else if (rampUpMainsParallelReached && (state.getScope() == ScopeType.Partner || state.getScope() == ScopeType.Customer || state.getScope() == ScopeType.INNIO_BOP)) {
                checkingStart.setReason("Customer/Partner/BOP responsibility");
                checkingStart.setExcluded(0);
                checkingStart.setExcludedVu(1);
                checkingStart.setValidStart(0);
                checkingStart.setValidStartGCB(1);
            }
            // reason 4
            else if (monitoringStartDate == 0 || checkingStart.getStartDate() < monitoringStartDate) {
                checkingStart.setReason("Before RAM monitoring start date");
                checkingStart.setExcluded(1);
                checkingStart.setExcludedVu(1);
                checkingStart.setValidStart(0);
                checkingStart.setValidStartGCB(0);
            } else {
                checkingStart.setExcluded(0);
                checkingStart.setExcludedVu(0);
                checkingStart.setValidStartGCB(1);
                checkingStart.setValidStart(1);
            }

            // only failed starts have a scope
            if (checkingStart.getTripsBeforeRampUpMainsParallel() > 0 || checkingStart.getTripsBeforeMainsParallel() > 0) {
                checkingStart.setScope(scopeService.getScope(state.getTriggerMsgNo(), state.getIeeeState()));
                checkingStart.setOutageNumber(state.getOutageNumber());
            }

            starts.add(checkingStart);
            checkingStart = null;
            sequenceCount = 0;
            sequenceCount2 = 0;
            sequenceCount3 = 0;
            sequenceCount4 = 0;
            lastActionActual = null;
            rampUpMainsParallelReached = false;
            isForcedOutage = false;
            canceledStart = false;
        }

        return starts;
    }
}