package io.myplant.service;

import io.myplant.domain.OverwriteState;
import io.myplant.model.ModelSerial;
import io.myplant.model.OverwriteStateDto;
import io.myplant.model.ScopeType;
import io.myplant.repository.OverwriteStateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class OverwriteStateService {
    private final OverwriteStateRepository repository;
    private final AssetService assetService;

    @Autowired
    public OverwriteStateService(OverwriteStateRepository repository, AssetService assetService) {
        this.repository = repository;
        this.assetService = assetService;
    }

    public OverwriteStateDto getById(long id) {
        Optional<OverwriteState> one = repository.findById(id);
        if (!one.isPresent())
            return null;

        ModelSerial serialModel = assetService.getSerialModel(one.get().getAssetId());

        return new OverwriteStateDto(one.get(), serialModel.getModel(), serialModel.getSerial());
    }


    public OverwriteStateDto setById(long id, OverwriteStateDto request) {
        OverwriteState one = repository.getOne(id);
        ModelSerial serialModel = assetService.getSerialModel(one.getAssetId());

        OverwriteValues(one, request);

        OverwriteState save = repository.save(one);
        return new OverwriteStateDto(save, serialModel.getModel(), serialModel.getSerial());
    }

    private void OverwriteValues(OverwriteState one, OverwriteStateDto request) {
        one.setActionActual(request.getActionActual());
        one.setActionFrom(request.getActionFrom());
        one.setActionTo(request.getActionTo());
        one.setIeeeState(request.getIeeeState());
        one.setScope(ScopeType.getNameByValue(request.getScope()));
        one.setDescription(request.getDescription());
        one.setDuration(request.getDuration());
    }


    public OverwriteStateDto createOne(OverwriteStateDto request) {
        OverwriteState state = CreateEntity(request);
        ModelSerial serialModel = assetService.getSerialModel(request.getAssetId());

        OverwriteState save = repository.save(state);
        return new OverwriteStateDto(save, serialModel.getModel(), serialModel.getSerial());

    }

    private OverwriteState CreateEntity(OverwriteStateDto request) {
        OverwriteState state = new OverwriteState();

        state.setAssetId(request.getAssetId());
        state.setActionActual(request.getActionActual());
        state.setActionFrom(request.getActionFrom());
        state.setActionTo(request.getActionTo());
        state.setIeeeState(request.getIeeeState());
        state.setScope(ScopeType.getNameByValue(request.getScope()));
        state.setDescription(request.getDescription());
        state.setDuration(request.getDuration());

        return state;
    }

    public void deleteOne(long id) {
        repository.deleteById(id);
    }

    public List<OverwriteStateDto> getAll(String model, String[] serials) {

        HashMap<Long, String> assetIdSerialMap = assetService.getIdSerialMap(model, serials);
        Set<Long> assetIds = assetIdSerialMap.keySet();

        List<OverwriteState> allStates = repository.findByAssetIdIn(assetIds);
        List<OverwriteStateDto> result = new ArrayList<>();
        for (OverwriteState state : allStates) {
            OverwriteStateDto stateDto = new OverwriteStateDto(state, model, assetIdSerialMap.get(state.getAssetId()));
            result.add(stateDto);
        }

        return result;
    }

}

