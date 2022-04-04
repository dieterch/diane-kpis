package io.myplant.service.processors;

import io.myplant.domain.AssetInformation;
import io.myplant.domain.DeviceState;
import io.myplant.domain.Start;
import io.myplant.model.EngineAction;
import io.myplant.model.IeeeStates;
import io.myplant.service.ScopeMapperService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

// Rewirte of the StartProcess from Marius - do not change this code
@Service
public class StartProcessorV2 {
    private final ScopeMapperService scopeService = new ScopeMapperService();

    public List<Start> run(long assetId, List<DeviceState> deviceStates, AssetInformation assetInformation) {

        List<Start> starts = new ArrayList<>();
        //long monitoringStartDate = Utils.getMonitoringStart(assetId, assetInformation);

        Start checkingStart = null;
        int sequenceCount = 0;

        EngineAction lastActionActual = null;
        boolean rampUpMainsParallelReached = false;
        boolean mainsParallelReached = false;
        boolean excludedMaintenance = false;
        boolean excludedDeactivated = false;
        boolean excludedCancelledUNK = false;
        boolean failedStart = false;

        DeviceState state = null;

        for (int i = 0; i < deviceStates.size(); i++) {
            if (i != 0) {
                lastActionActual = state.getActionActual();
            }
            state = deviceStates.get(i);

            if (checkingStart == null &&
                    (state.getActionActual() == EngineAction.START_PREPARATION || state.getActionActual() == EngineAction.START) &&
                    lastActionActual != EngineAction.START_PREPARATION && lastActionActual != EngineAction.START) {
                checkingStart = new Start(assetId, state.getActionFrom());

                // Check for first row in start sequence (start preparation)
                if (state.getIeeeState() == IeeeStates.FORCEDOUTAGE_MTBFO_REL || state.getIeeeState() == IeeeStates.FORCEDOUTAGE_REL
                        || state.getIeeeState() == IeeeStates.PILOT_ACTIVITIES || state.getIeeeState() == IeeeStates.PLANNED_OUTAGE
                        || state.getIeeeState() == IeeeStates.UNPLANNED_MAINTENANCE) {
                    excludedMaintenance = true;
                } else if (state.getIeeeState() == IeeeStates.DEACTIVATED_SHUTDOWN) {
                    excludedDeactivated = true;
                } else {
                    // Continue to next state in start sequence
                    sequenceCount++;
                    continue;
                }
            }

            // Go through valid start sequences
            if (excludedMaintenance == false && excludedDeactivated == false) {
                // Ignore repeated action actuals
                if (checkingStart == null) {
                    continue;
                }

                if (state.getActionActual() == lastActionActual) {
                    if (state.getIeeeState() == IeeeStates.PILOT_ACTIVITIES || state.getIeeeState() == IeeeStates.PLANNED_OUTAGE
                            || state.getIeeeState() == IeeeStates.UNPLANNED_MAINTENANCE) {
                        excludedMaintenance = true;
                    } else if (state.getIeeeState() == IeeeStates.DEACTIVATED_SHUTDOWN) {
                        excludedDeactivated = true;
                    } else if (state.getIeeeState() == IeeeStates.FORCEDOUTAGE_MTBFO_REL || state.getIeeeState() == IeeeStates.FORCEDOUTAGE_REL) {
                        failedStart = true;
                    } else {
                        continue;
                    }
                }

                if (sequenceCount == 1 &&
                        (state.getActionActual() == EngineAction.START || state.getActionActual() == EngineAction.IDLE)) {
                    if (state.getIeeeState() == IeeeStates.PILOT_ACTIVITIES || state.getIeeeState() == IeeeStates.PLANNED_OUTAGE
                            || state.getIeeeState() == IeeeStates.UNPLANNED_MAINTENANCE) {
                        excludedMaintenance = true;
                    } else if (state.getIeeeState() == IeeeStates.DEACTIVATED_SHUTDOWN) {
                        excludedDeactivated = true;
                    } else if (state.getIeeeState() == IeeeStates.FORCEDOUTAGE_MTBFO_REL || state.getIeeeState() == IeeeStates.FORCEDOUTAGE_REL) {
                        failedStart = true;
                    } else {
                        // Continue to next state in start sequence
                        sequenceCount++;
                        continue;
                    }
                } else if (sequenceCount == 2 &&
                        (state.getActionActual() == EngineAction.IDLE || state.getActionActual() == EngineAction.SYNCHRONISATION ||
                                state.getActionActual() == EngineAction.RAMPUP_MAINS_PARALLEL_OPERATION || state.getActionActual() == EngineAction.RAMPUP_ISLAND_OPERATION)) {
                    if (state.getIeeeState() == IeeeStates.PILOT_ACTIVITIES || state.getIeeeState() == IeeeStates.PLANNED_OUTAGE
                            || state.getIeeeState() == IeeeStates.UNPLANNED_MAINTENANCE) {
                        excludedMaintenance = true;
                    } else if (state.getIeeeState() == IeeeStates.DEACTIVATED_SHUTDOWN) {
                        excludedDeactivated = true;
                    } else if (state.getIeeeState() == IeeeStates.FORCEDOUTAGE_MTBFO_REL || state.getIeeeState() == IeeeStates.FORCEDOUTAGE_REL) {
                        failedStart = true;
                    } else {
                        if (state.getActionActual() == EngineAction.RAMPUP_MAINS_PARALLEL_OPERATION || state.getActionActual() == EngineAction.RAMPUP_ISLAND_OPERATION) {
                            rampUpMainsParallelReached = true;
                            // Continue to next state in start sequence
                            sequenceCount++;
                            continue;
                        } else {
                            // Continue to next state in start sequence
                            sequenceCount++;
                            continue;
                        }
                    }
                } else if (sequenceCount == 3 &&
                        (state.getActionActual() == EngineAction.SYNCHRONISATION || state.getActionActual() == EngineAction.RAMPUP_MAINS_PARALLEL_OPERATION ||
                                state.getActionActual() == EngineAction.RAMPUP_ISLAND_OPERATION || state.getActionActual() == EngineAction.MAINS_PARALLEL_OPERATION ||
                                state.getActionActual() == EngineAction.ISLAND_OPERATION)) {
                    if (state.getIeeeState() == IeeeStates.PILOT_ACTIVITIES || state.getIeeeState() == IeeeStates.PLANNED_OUTAGE
                            || state.getIeeeState() == IeeeStates.UNPLANNED_MAINTENANCE) {
                        excludedMaintenance = true;
                    } else if (state.getIeeeState() == IeeeStates.DEACTIVATED_SHUTDOWN) {
                        excludedDeactivated = true;
                    } else if (state.getIeeeState() == IeeeStates.FORCEDOUTAGE_MTBFO_REL || state.getIeeeState() == IeeeStates.FORCEDOUTAGE_REL) {
                        failedStart = true;
                    } else {
                        if (state.getActionActual() == EngineAction.RAMPUP_MAINS_PARALLEL_OPERATION || state.getActionActual() == EngineAction.RAMPUP_ISLAND_OPERATION) {
                            rampUpMainsParallelReached = true;
                            // Continue to next state in start sequence
                            sequenceCount++;
                            continue;
                        } else if (state.getActionActual() == EngineAction.MAINS_PARALLEL_OPERATION || state.getActionActual() == EngineAction.ISLAND_OPERATION) {
                            mainsParallelReached = true;
                        } else {
                            // Continue to next state in start sequence
                            sequenceCount++;
                            continue;
                        }
                    }
                } else if ((sequenceCount == 4 || sequenceCount == 5) &&
                        (state.getActionActual() == EngineAction.RAMPUP_MAINS_PARALLEL_OPERATION || state.getActionActual() == EngineAction.RAMPUP_ISLAND_OPERATION ||
                                state.getActionActual() == EngineAction.MAINS_PARALLEL_OPERATION || state.getActionActual() == EngineAction.ISLAND_OPERATION)) {
                    if (state.getIeeeState() == IeeeStates.PILOT_ACTIVITIES || state.getIeeeState() == IeeeStates.PLANNED_OUTAGE
                            || state.getIeeeState() == IeeeStates.UNPLANNED_MAINTENANCE) {
                        excludedMaintenance = true;
                    } else if (state.getIeeeState() == IeeeStates.DEACTIVATED_SHUTDOWN) {
                        excludedDeactivated = true;
                    } else if (state.getIeeeState() == IeeeStates.FORCEDOUTAGE_MTBFO_REL || state.getIeeeState() == IeeeStates.FORCEDOUTAGE_REL) {
                        failedStart = true;
                    } else {
                        if (state.getActionActual() == EngineAction.RAMPUP_MAINS_PARALLEL_OPERATION || state.getActionActual() == EngineAction.RAMPUP_ISLAND_OPERATION) {
                            rampUpMainsParallelReached = true;
                            // Continue to next state in start sequence
                            sequenceCount++;
                            continue;
                        } else if (state.getActionActual() == EngineAction.MAINS_PARALLEL_OPERATION || state.getActionActual() == EngineAction.ISLAND_OPERATION) {
                            mainsParallelReached = true;
                        } else {
                            // will not happen
                        }
                    }
                } else if (sequenceCount == 6 &&
                        (state.getActionActual() == EngineAction.MAINS_PARALLEL_OPERATION || state.getActionActual() == EngineAction.ISLAND_OPERATION)) {
                    if (state.getIeeeState() == IeeeStates.PILOT_ACTIVITIES || state.getIeeeState() == IeeeStates.PLANNED_OUTAGE
                            || state.getIeeeState() == IeeeStates.UNPLANNED_MAINTENANCE) {
                        excludedMaintenance = true;
                    } else if (state.getIeeeState() == IeeeStates.DEACTIVATED_SHUTDOWN) {
                        excludedDeactivated = true;
                    } else if (state.getIeeeState() == IeeeStates.FORCEDOUTAGE_MTBFO_REL || state.getIeeeState() == IeeeStates.FORCEDOUTAGE_REL) {
                        failedStart = true;
                    } else {
                        mainsParallelReached = true;
                    }
                } else {
                    if (state.getIeeeState() == IeeeStates.PILOT_ACTIVITIES || state.getIeeeState() == IeeeStates.PLANNED_OUTAGE
                            || state.getIeeeState() == IeeeStates.UNPLANNED_MAINTENANCE) {
                        excludedMaintenance = true;
                    } else if (state.getIeeeState() == IeeeStates.DEACTIVATED_SHUTDOWN) {
                        excludedDeactivated = true;
                    } else if (state.getIeeeState() == IeeeStates.FORCEDOUTAGE_MTBFO_REL || state.getIeeeState() == IeeeStates.FORCEDOUTAGE_REL) {
                        failedStart = true;
                    } else {
                        excludedCancelledUNK = true;
                    }
                }
            }

            // Assign proper start parameters
            if (excludedMaintenance) {
                // Excluded - troubleshooting / maintenance / deactivated shutdown
                if (rampUpMainsParallelReached) {
                    checkingStart.setValidStartGCB(1);
                    checkingStart.setValidStart(0);
                } else {
                    checkingStart.setValidStartGCB(0);
                    checkingStart.setValidStart(0);
                }
                checkingStart.setReason("Troubleshooting / maintenance / deactivated shutdown");
            } else if (excludedDeactivated) {
                // Excluded - deactivated
                if (rampUpMainsParallelReached) {
                    checkingStart.setValidStartGCB(1);
                    checkingStart.setValidStart(0);
                } else {
                    checkingStart.setValidStartGCB(0);
                    checkingStart.setValidStart(0);
                }
                checkingStart.setReason("Deactivated");
            } else if (failedStart) {
                checkingStart.setFailedStart(1);
                checkingStart.setValidStartGCB(1);
                checkingStart.setValidStart(1);
                if (rampUpMainsParallelReached) {
                    // Failed start - target load
                    checkingStart.setTripsBeforeRampUpMainsParallel(0);
                } else {
                    // Failed start - GCB close
                    checkingStart.setTripsBeforeRampUpMainsParallel(1);
                }
                checkingStart.setTripsBeforeMainsParallel(1);
                checkingStart.setTriggerMSGNo(state.getTriggerMsgNo());
                checkingStart.setScope(scopeService.getScope(state.getTriggerMsgNo(), state.getIeeeState()));
                checkingStart.setOutageNumber(state.getOutageNumber());
            } else if (mainsParallelReached) {
                // Fully successful start - target load reached
                checkingStart.setTimeToMainsParallel(state.getActionFrom() - checkingStart.getStartDate());
                checkingStart.setValidStartGCB(1);
                checkingStart.setValidStart(1);
                checkingStart.setTripsBeforeRampUpMainsParallel(0);
                checkingStart.setTripsBeforeMainsParallel(0);
            } else if (excludedCancelledUNK) {
                // Excluded - cancelled due to UNK reason
                if (rampUpMainsParallelReached) {
                    checkingStart.setValidStartGCB(1);
                    checkingStart.setValidStart(0);
                } else {
                    checkingStart.setValidStartGCB(0);
                    checkingStart.setValidStart(0);
                }
                checkingStart.setReason("Test run or cancelled for UNK reason");
            } else {
                // will not happen
            }

            // Final calculations
            if (checkingStart.getValidStartGCB() == 1) {
                checkingStart.setExcluded(0);
            } else {
                checkingStart.setExcluded(1);
            }
            if (checkingStart.getValidStart() == 1) {
                checkingStart.setExcludedVu(0);
            } else {
                checkingStart.setExcludedVu(1);
            }

            starts.add(checkingStart);

            // Reset parameters
            checkingStart = null;
            sequenceCount = 0;
            //        lastActionActual = 0
            rampUpMainsParallelReached = false;
            mainsParallelReached = false;
            excludedMaintenance = false;
            excludedDeactivated = false;
            excludedCancelledUNK = false;
            failedStart = false;
        }

        return starts;
    }
}