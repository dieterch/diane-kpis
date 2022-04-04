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
import java.util.List;

@Service
public class StartStateProcessor {
    private final ScopeMapperService scopeService = new ScopeMapperService();

    private Start start = null;

    private EngineAction currentState = null;

    private long assetId;

    private long monitoringStartDate;

    public List<Start> run(long assetId, List<DeviceState> deviceStates, AssetInformation assetInformation){
        List<Start> startList = new ArrayList<>();
        this.assetId = assetId;
        this.monitoringStartDate = Utils.getMonitoringStart(assetId, assetInformation);

        for(DeviceState state : deviceStates)
        {
            Start start = processDeviceState(state);

            if (start != null){
                startList.add(start);
            }
        }

        return startList;
    }

    /**
     *
     * @param state
     * @return a Start object when the start is finished, otherwise null;
     */
    public Start processDeviceState(DeviceState state) {
        if (this.start == null){
            this.start = new Start(this.assetId, state.getActionFrom());
        }

        // Ignore duplicate actions, occurs after midnight
        if (this.currentState == state.getActionActual()){
            return null;
        }

        // If an exclusion state occurs or an action finishes the startup sequence, return the start object and set it to null to start fresh
        if (processActionActual(state) || processExclusionStates(state)){
            this.start.setScope(scopeService.getScope(state.getTriggerMsgNo(), state.getIeeeState()));
            this.start.setOutageNumber(state.getOutageNumber());

            Start temp = this.start;
            this.start = null;
            this.currentState = null;

            return temp;
        }

        return null;
    }

    private boolean processExclusionStates(DeviceState state){
        if (this.currentState != null
                && (isExclusionReason1a(state)
                || isExclusionReason1b(state)
                || isExclusionReason2(state)
                || isExclusionReason3(state)
                || isExclusionReason4(state))){

            this.start.setExcludedVu(1);
            this.start.setExcluded(1);

            return true;
        }

        return false;
    }

    private boolean isExclusionReason1a(DeviceState state) {
        if (state.getIeeeState() != IeeeStates.AVAILABLE
                || state.getAvailableState() == AvailableStates.NOT_AVAILABLE
                || state.getAvailableState().isTroubleshooting()
                || state.getAvailableState().isMaintenance()
                || state.getIeeeState() == IeeeStates.UNPLANNED_MAINTENANCE) {

            this.start.setReason("Troubleshooting / maintenance / deactivated shutdown");
            return true;
        }

        return false;
    }

    private boolean isExclusionReason1b(DeviceState state) {
        if (state.getAvailableState().isDeactivated()){
            this.start.setReason("Deactivated");
            return true;
        }

        return false;
    }

    private boolean isExclusionReason2(DeviceState state) {
        if (!isRamupState(this.currentState) && isEngineCooldownOrReady(state.getActionActual())) {
            this.start.setReason("Test run or cancelled for UNK reason");
            return true;
        }

        return false;
    }

    private boolean isExclusionReason3(DeviceState state) {
        if (isPartnerOrCustomer(state.getScope())){
            this.start.setReason("Customer/Partner responsibility");
            return true;
        }

        return false;
    }

    private boolean isExclusionReason4(DeviceState state) {
        if (this.monitoringStartDate == 0 || this.start.getStartDate() < this.monitoringStartDate){
            this.start.setReason("Before RAM calculating start date");
            return true;
        }

        return false;
    }

    /**
     * Handle a given state and change the state of the start
     * @param state
     * @return true if the start is finished and a new start-object
     */
    private boolean processActionActual(DeviceState state){
        switch(state.getActionActual()) {
            case START_PREPARATION:
                return processStartPreparation(state);
            case START:
                return processStart(state);
            case IDLE:
                return processIdle(state);
            case SYNCHRONISATION:
                return processSynchronisation(state);
            case RAMPUP_MAINS_PARALLEL_OPERATION:
                return processMainsRampup(state);
            case MAINS_PARALLEL_OPERATION:
                return processMainsOperation(state);
            case RAMPUP_ISLAND_OPERATION:
                return processIslandRampup(state);
            case ISLAND_OPERATION:
                return processIslandOperation(state);
            case READY:
                return false;
            default:
                return handleCancel(state);
        }
    }

    /**
     * If any other state occurs, the start is canceled
     *
     * @param state
     * @return
     */
    private boolean handleCancel(DeviceState state){
        this.start.setTimeToMainsParallel(0);

        if (isCancelState(state.getActionActual())) {
            if (isRamupState(this.currentState)) {
                this.start.setReason("Canceled start");
                this.start.setExcluded(1);
                this.start.setExcludedVu(1);
            } else {
                this.start.setExcludedVu(1);
            }
        } else {
            this.start.setFailedStart(1);
            this.start.setTriggerMSGNo(state.getTriggerMsgNo());

            if (isRamupState(this.currentState)){
                this.start.setTripsBeforeMainsParallel(1);
            } else {
                this.start.setTripsBeforeRampUpMainsParallel(1);
            }
        }

        return true;
    }

    /**
     * Check if the given state is a valid start cancellation state.
     *
     * @param state
     * @return true if the given state is a cancellation state.
     */
    private boolean isCancelState(EngineAction state){
        if (state != null) {
            switch (state) {
                case LOAD_RAMPDOWN:
                case ENGINE_COOLDOWN:
                case READY:
                    return true;
            }
        }

        return false;
    }

    private boolean isEngineCooldownOrReady(EngineAction state){
        if (state != null) {
            switch (state) {
                case ENGINE_COOLDOWN:
                case READY:
                    return true;
            }
        }

        return false;
    }

    private boolean isPartnerOrCustomer(ScopeType scope){
        if (scope != null) {
            switch (scope) {
                case Partner:
                case Customer:
                    return true;
            }
        }

        return false;
    }

    /**
     * Check if the given state is a rampup state.
     *
     * @param state
     * @return true if the given state is a rampup state.
     */
    private boolean isRamupState(EngineAction state){
        if (state != null) {
            switch (state) {
                case RAMPUP_MAINS_PARALLEL_OPERATION:
                case RAMPUP_ISLAND_OPERATION:
                    return true;
            }
        }

        return false;
    }

    /**
     * Process state START_PREPARATION
     * #1 start state in mains parallel and island operations
     *
     * @param state
     * @return always returns false, because the start operation is not finished yet
     */
    private boolean processStartPreparation(DeviceState state){
        this.currentState = EngineAction.START_PREPARATION;

        return false;
    }


    /**
     * Process state START
     * #2 state in mains parallel and island operations
     *
     * @param state
     * @return always returns false, because the start operation is not finished yet
     */
    private boolean processStart(DeviceState state){
        if (this.currentState == EngineAction.START_PREPARATION) {
            this.currentState = EngineAction.START;
            return false;
        }else{
            //TODO error
        }

        return false;
    }

    // #3 start state in all operation
    /**
     * Process state RAMPUP_ISLAND_OPERATION
     * #3 state in mains parallel and island operations
     *
     * @param state
     * @return always returns false, because the start operation is not finished yet
     */
    private boolean processIdle(DeviceState state){
        if (this.currentState == EngineAction.START) {
            this.currentState = EngineAction.IDLE;
        }else{
            //TODO error
        }

        return false;
    }

    /**
     * Process state SYNCHRONISATION
     * #4 state for mains parallel operation, does not occur in island operations
     *
     * @param state
     * @return always returns false, because the start operation is not finished yet
     */
    private boolean processSynchronisation(DeviceState state){
        if (this.currentState == EngineAction.IDLE) {
            this.currentState = EngineAction.SYNCHRONISATION;
        }else{
            //TODO error
        }

        return false;
    }

    /**
     * Process state RAMPUP_MAINS_PARALLEL_OPERATION
     * #5 state for mains parallel operation
     *
     * @param state
     * @return always returns false, because the start operation is not finished yet
     */
    private boolean processMainsRampup(DeviceState state){
        if (this.currentState == EngineAction.SYNCHRONISATION) {
            this.currentState = EngineAction.RAMPUP_MAINS_PARALLEL_OPERATION;
        }else{
            //TODO error
        }

        return false;
    }

    /**
     * Process state MAINS_PARALLEL_OPERATION
     * #6 start state for mains parallel operation, the start is now finished
     *
     * @param state
     * @return returns true if the previous state was IDLE, the start operation is finished successful
     */
    private boolean processMainsOperation(DeviceState state){
        if (this.currentState == EngineAction.RAMPUP_MAINS_PARALLEL_OPERATION) {
            this.currentState = EngineAction.MAINS_PARALLEL_OPERATION;

            start.setTimeToMainsParallel(state.getActionFrom() - start.getStartDate());
            this.start.setValidStart(1);
            this.start.setValidStartGCB(1);

            return true;
        }else{
            //TODO error
        }

        return false;
    }

    /**
     * Process state RAMPUP_ISLAND_OPERATION
     * #4 state for island operations
     *
     * @param state
     * @return always returns false, because the start operation is not finished yet
     */
    private boolean processIslandRampup(DeviceState state){
        if (this.currentState == EngineAction.IDLE) {
            this.currentState = EngineAction.RAMPUP_ISLAND_OPERATION;
        }else{
            //TODO error
        }

        return false;
    }

    /**
     * Process state RAMPUP_ISLAND_OPERATION
     * #5 state for island operations, the start is now finished
     *
     * @param state
     * @return returns true if the previous state was IDLE, the start operation is finished successful
     */
    private boolean processIslandOperation(DeviceState state){
        if (this.currentState == EngineAction.IDLE) {
            this.currentState = EngineAction.RAMPUP_ISLAND_OPERATION;
            this.start.setValidStart(1);

            return true;
        }else{
            //TODO error
        }

        return false;
    }

    public void reset(){
        this.currentState = null;
        this.start = null;
    }
}