package io.myplant;

import io.myplant.model.Asset;
import io.myplant.model.Property;
import io.myplant.service.AssetInformationService;
import io.myplant.service.AssetService;
import io.myplant.service.StateKpiCalculation.StateMachineCalculator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

@Component
public class DianeKpisCalculationJob {
    private final Log log = LogFactory.getLog(DianeKpisCalculationJob.class);

    @Autowired
    private StateMachineCalculator stateMachineCalculator;

    @Autowired
    private AssetInformationService assetInformationService;

    @Autowired
    private AssetService assetService;

    private final DianeKpisProperties config;

    @Autowired
    public DianeKpisCalculationJob(DianeKpisProperties config) {
        this.config = config;
    }

    public void executeKpisCalculationForAssets(final Collection<Asset> assetList, boolean dryRun)
            throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(config.getParallelCalculations());

        LinkedList<Future<?>> calculations = new LinkedList<>();
        assetList.forEach(asset -> calculations.add(executorService.submit(() -> {
            log.info("start calculation for assetId: " + asset.getId());
            assetInformationService.saveInformation(asset.getId(), getCommissionDate(asset), asset.getTimezone());
            stateMachineCalculator.calculateStateMachineAndStoreResultFromAssetId(asset.getId(), dryRun);
        })));

        long totalCalculations = calculations.size();
        while (!calculations.isEmpty()) {
            Future<?> calculation = calculations.removeFirst();
            try {
                calculation.get(30, TimeUnit.MINUTES);
                log.info("remaining: " + calculations.size() + " total: " + totalCalculations);
            } catch (Exception ex) {
                log.error("error in calculation", ex);
                if (!calculation.isDone()) calculation.cancel(true);
            }
        }

        log.info("shutdown executor service");
        executorService.shutdown();
        log.info("await executor service termination");
        executorService.awaitTermination(5, TimeUnit.MINUTES);
        log.info("executor service terminated");
    }

    private String getCommissionDate(Asset asset) {
        String commssionDate = null;
        if (asset.getProperties() != null) {
            Optional<Property> first =
                    asset.getProperties()
                            .stream()
                            .filter(p -> p.getName().equals(DianeKpisConstants.COMMISSION_PROP))
                            .findFirst();
            if (first.isPresent()) commssionDate = first.get().getValue();
        }
        return commssionDate;
    }
}
