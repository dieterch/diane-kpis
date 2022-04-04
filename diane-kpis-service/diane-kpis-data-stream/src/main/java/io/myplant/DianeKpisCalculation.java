package io.myplant;

import io.myplant.model.*;
import io.myplant.model.asset.ListRequest;
import io.myplant.service.*;
import io.myplant.seshat.api.AssetApi;
import io.myplant.seshat.api.OrganizationApi;
import io.myplant.utils.StopWatch;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@EnableConfigurationProperties(DianeKpisProperties.class)
public class DianeKpisCalculation implements ApplicationRunner {
    private final Log log = LogFactory.getLog(DianeKpisCalculation.class);

    private static final String ENGINES_COMMAND_LINE_ARGUMENT = "engines";
    private static final String ORGANIZATIONS_COMMAND_LINE_ARGUMENT = "organizations";
    private static final String EXPORT_COMMAND_LINE_ARGUMENT = "exportToS3";
    private static final String DRY_RUN_COMMAND_LINE_ARGUMENT = "dryRun";

    @Autowired
    private OrganizationApi organizationApi;
    @Autowired
    private DianeKpisCalculationJob dianeKpisCalculationJob;
    @Autowired
    private AssetService assetService;
    @Autowired
    private AssetApi assetApi;
    @Autowired
    private CsvService csvService;
    @Autowired
    private StateMachineV2Service stateMachineV2Service;
    @Autowired
    private AssetInformationService assetInformationService;
    @Autowired
    private StartsService startsService;
    @Autowired
    private S3FileService s3FileService;

    private final DianeKpisProperties config;

    private boolean exportToS3;
    private boolean dryRun;

    // serial numbers to export
    private static String[] SERIE9_FLEET = new String[]{
        // Rosenheim
        "1200968",

        // Stapelfeld
        "1221743",

        // ACEA
        "1221749", "1137636",
        // META
        "1204731", "1221311",

        // Kiel
        "1172721", "1174165", "1173991", "1174000", "1172413", "1173960",
        "1173877", "1173775", "1173687", "1173587", "1172686", "1172627",
        "1172595", "1172526", "1173834", "1172521", "1172474", "1172365",
        "1172348", "1172267",

        // SKY
        "1142351", "1142276", "1142338", "1142401", "1142463", "1142441",

        // BMW
        "1320090", "1320079", "1320114", "1320072", "1318930", "1318886",
        "1318904", "1318934", "1352236", "1352268", "1352323",


        // T3E & T4E
        "1072102", "1086054", "1114128", "1224208", "1229935", "1381726",
        "1384259", "1413661", "1245789", "1245559", "1248161", "1114010",
        "1244265", "1343471", "1343476", "1360054", "1362321", "1367041",
        "1395922",

        // Merheim
        "1405614", "1405664", "1405727",


        // T3F
        "1461606", "1457068", "1251013", "1466181", "1251261", "1470239",
        "1255290",

        "1245101",

        // add for Marius at 21.april.2021
        "1144627", "1241844", "1241842", "1120997", "1243812", "1249332",
        "1402507", "1402687", "1402790", "1402901", "1402948", "1395957",
        "1410780", "1360905", "1360902", "1360874", "1360884", "1415073",
        "1416600", "1417994", "1418558", "1418583", "1416626", "1416625",
        "1445343", "1445339", "1456419", "1456435", "1462365", "1446115",
        "1446117",

        // add for Marius at 27.07.2021 (version 3.1.12)
        "1254337",

        // add for Marius at 12.10.2021 (version 3.1.14)
        "1427771"

            // T3CD Engines
//            "1074265", "1086044", "1113852", "1113153", "1225884", "1234273",
//            "1212889", "1163774", "1318149", "5088101", "1380942", "1384596",
//            "1404820"
    };

    @Autowired
    private ConfigurableApplicationContext context;

    @Autowired
    public DianeKpisCalculation(DianeKpisProperties config) {
        this.config = config;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        List<String> engineArg = args.getOptionValues(ENGINES_COMMAND_LINE_ARGUMENT);
        List<String> organizationArg = args.getOptionValues(ORGANIZATIONS_COMMAND_LINE_ARGUMENT);
        List<String> exportArg = args.getOptionValues(EXPORT_COMMAND_LINE_ARGUMENT);
        List<String> dryRunArg = args.getOptionValues(DRY_RUN_COMMAND_LINE_ARGUMENT);

        if (exportArg != null && !exportArg.isEmpty()) {
            this.exportToS3 = exportArg.get(0).equalsIgnoreCase("true");
        } else {
            this.exportToS3 = true; // default, if no param specified
        }
        this.dryRun = dryRunArg != null && !dryRunArg.isEmpty() && dryRunArg.get(0).equalsIgnoreCase("true");

        List<String> serialFilter = null;

        if (engineArg != null && !engineArg.isEmpty()) {
            String engines = engineArg.get(0);
            log.info("launched with " + ENGINES_COMMAND_LINE_ARGUMENT + " argument: " + engines);
            serialFilter = Arrays.stream(StringUtils.split(engineArg.get(0), ",|; ")).collect(Collectors.toList());
        } else if (organizationArg != null && !organizationArg.isEmpty()) {
            String organizations = organizationArg.get(0);
            log.info("launched with " + ORGANIZATIONS_COMMAND_LINE_ARGUMENT + " argument: " + organizations);
            if (!StringUtils.equals(organizations, "*")) {
                serialFilter = Arrays.stream(StringUtils.split(organizationArg.get(0), ",|; "))
                        .map(Long::parseLong)
                        .map(organizationId -> organizationApi.getOrganisationAssetIds(organizationId))
                        .flatMap(x -> x.stream().map(y -> assetService.getSerialModel(y)))
                        .map(ModelSerial::getSerial)
                        .collect(Collectors.toList());
            }
        }
        else{
            // Pre calculation of serie 9 fleet
            performCalculation(getAllEngines(SERIE9_FLEET), true);
        }

        Collection<Asset> engines;
        if (serialFilter != null && !serialFilter.isEmpty()) {
            engines = getAllEngines(serialFilter.toArray(new String[0]));
        } else {
            engines = getAllEngines(null);
        }
        performCalculation(engines, false);

        context.close();
    }

    private void performCalculation(Collection<Asset> assetList, boolean export) {
        StopWatch watch = new StopWatch();
        watch.start();

        log.info("running calculation for " + assetList.size() + " assets");

        try {
            dianeKpisCalculationJob.executeKpisCalculationForAssets(assetList, dryRun);
            if (export) {
                createSerie9Export();
                exportToS3();
            }
        } catch (Exception ex) {
            log.error("error executing Calculation", ex);
        }
        log.info("FINISHED calculation for " + assetList.size() + " assets tooks " + watch.pretty(watch.stop()));
    }

    private void createSerie9Export() {
        if (dryRun) {
            log.warn("dryRun flag enabled - do not store any data");
            return;
        }
        String lan = "en";
        try {
            List<Asset> allEngines = getAllEngines(SERIE9_FLEET);
            // set assetInfo for kiel fleet
            for (Asset asset : allEngines) {
                assetInformationService.saveInformation(asset.getId(), getCommissionDate(asset), asset.getTimezone());
            }

            log.info("create states");
            List<DeviceStateDto> states = stateMachineV2Service.getStatesOfAsset(DianeKpisConstants.MODEL, SERIE9_FLEET, true, false, null, null, null, true, lan);
            csvService.createCSVFile(states, config.getTempFolder() + "states.csv", DeviceStateDto::getCsvHeaderStrings, DeviceStateDto::getCsvRow);
            log.info("states " + states.size() + " found");

            log.info("create merged states");
            List<DeviceStateDto> merged = stateMachineV2Service.getStatesOfAsset(DianeKpisConstants.MODEL, SERIE9_FLEET, true, true, null, null, null, true, lan);
            csvService.createCSVFile(merged, config.getTempFolder() + "merged_states.csv", DeviceStateDto::getCsvHeaderStrings, DeviceStateDto::getCsvRow);
            log.info("merged states " + merged.size() + " found");

            log.info("create starts");
            List<StartDto> starts = startsService.getStartsOfAssets(DianeKpisConstants.MODEL, SERIE9_FLEET, false, null, null, true, lan);
            csvService.createCSVFile(starts, config.getTempFolder() + "starts.csv", StartDto::getCsvHeader, StartDto::getCsvRow);
            log.info("starts " + starts.size() + " found");

            log.info("create merged starts");
            List<StartDto> mergedstarts = startsService.getStartsOfAssets(DianeKpisConstants.MODEL, SERIE9_FLEET, true, null, null, true, lan);
            csvService.createCSVFile(mergedstarts, config.getTempFolder() + "merged_starts.csv", StartDto::getCsvHeader, StartDto::getCsvRow);
            log.info("merged starts " + mergedstarts.size() + " found");

            log.info("create ieee outages");
            List<OutageDto> outagesOfAsset_Ieee = stateMachineV2Service.getOutagesOfAsset(DianeKpisConstants.MODEL, SERIE9_FLEET, StateType.IEEE, false, null, null, true, lan);
            csvService.createCSVFile(outagesOfAsset_Ieee, config.getTempFolder() + "outages_ieee.csv", OutageDto::getCsvHeader, OutageDto::getCsvRow);
            log.info("ieee outages " + outagesOfAsset_Ieee.size() + " found");

            log.info("create merged ieee outages");
            List<OutageDto> outagesOfAsset_Ieee_merged = stateMachineV2Service.getOutagesOfAsset(DianeKpisConstants.MODEL, SERIE9_FLEET, StateType.IEEE, true, null, null, true, lan);
            csvService.createCSVFile(outagesOfAsset_Ieee_merged, config.getTempFolder() + "merged_outages_ieee.csv", OutageDto::getCsvHeader, OutageDto::getCsvRow);
            log.info("merged ieee outages " + outagesOfAsset_Ieee_merged.size() + " found");

            log.info("create vu outages");
            List<OutageDto> outagesOfAsset_vu = stateMachineV2Service.getOutagesOfAsset(DianeKpisConstants.MODEL, SERIE9_FLEET, StateType.VU, false, null, null, true, lan);
            csvService.createCSVFile(outagesOfAsset_vu, config.getTempFolder() + "outages_vu.csv", OutageDto::getCsvHeader, OutageDto::getCsvRow);
            log.info("vu outages " + outagesOfAsset_vu.size() + " found");

            log.info("create merged vu outages");
            List<OutageDto> outagesOfAsset_vu_merged = stateMachineV2Service.getOutagesOfAsset(DianeKpisConstants.MODEL, SERIE9_FLEET, StateType.VU, true, null, null, true, lan);
            csvService.createCSVFile(outagesOfAsset_vu_merged, config.getTempFolder() + "merged_outages_vu.csv", OutageDto::getCsvHeader, OutageDto::getCsvRow);
            log.info("merged vu outages " + outagesOfAsset_vu_merged.size() + " found");

            log.info("create vz outages");
            List<OutageDto> outagesOfAsset_vz = stateMachineV2Service.getOutagesOfAsset(DianeKpisConstants.MODEL, SERIE9_FLEET, StateType.VZ, false, null, null, true, lan);
            csvService.createCSVFile(outagesOfAsset_vz, config.getTempFolder() + "outages_vz.csv", OutageDto::getCsvHeader, OutageDto::getCsvRow);
            log.info("vz outages " + outagesOfAsset_vz.size() + " found");

            log.info("create merged vz outages");
            List<OutageDto> outagesOfAsset_vz_merged = stateMachineV2Service.getOutagesOfAsset(DianeKpisConstants.MODEL, SERIE9_FLEET, StateType.VZ, true, null, null, true, lan);
            csvService.createCSVFile(outagesOfAsset_vz_merged, config.getTempFolder() + "merged_outages_vz.csv", OutageDto::getCsvHeader, OutageDto::getCsvRow);
            log.info("merged vz outages " + outagesOfAsset_vz_merged.size() + " found");

        } catch (Exception ex) {
            log.error("problem to create exports", ex);
        }
    }

    private void exportToS3() {
        if (!exportToS3 || dryRun) {
            log.warn("upload to s3 disabled");
            return;
        }

        log.info("upload from '" + config.getTempFolder() + "states.csv' to s3");
        s3FileService.uploadFile("states.csv", new File(config.getTempFolder() + "states.csv"));
        log.info("upload from '" + config.getTempFolder() + "merged_states.csv' to s3");
        s3FileService.uploadFile("merged_states.csv", new File(config.getTempFolder() + "merged_states.csv"));

        log.info("upload from '" + config.getTempFolder() + "starts.csv' to s3");
        s3FileService.uploadFile("starts.csv", new File(config.getTempFolder() + "starts.csv"));
        log.info("upload from '" + config.getTempFolder() + "merged_starts.csv' to s3");
        s3FileService.uploadFile("merged_starts.csv", new File(config.getTempFolder() + "merged_starts.csv"));

        log.info("upload from '" + config.getTempFolder() + "outages_ieee.csv' to s3");
        s3FileService.uploadFile("outages_ieee.csv", new File(config.getTempFolder() + "outages_ieee.csv"));
        log.info("upload from '" + config.getTempFolder() + "outages_vu.csv' to s3");
        s3FileService.uploadFile("outages_vu.csv", new File(config.getTempFolder() + "outages_vu.csv"));
        log.info("upload from '" + config.getTempFolder() + "outages_vz.csv' to s3");
        s3FileService.uploadFile("outages_vz.csv", new File(config.getTempFolder() + "outages_vz.csv"));

        log.info("upload from '" + config.getTempFolder() + "merged_outages_ieee.csv' to s3");
        s3FileService.uploadFile("merged_outages_ieee.csv", new File(config.getTempFolder() + "merged_outages_ieee.csv"));
        log.info("upload from '" + config.getTempFolder() + "merged_outages_vu.csv' to s3");
        s3FileService.uploadFile("merged_outages_vu.csv", new File(config.getTempFolder() + "merged_outages_vu.csv"));
        log.info("upload from '" + config.getTempFolder() + "merged_outages_vz.csv' to s3");
        s3FileService.uploadFile("merged_outages_vz.csv", new File(config.getTempFolder() + "merged_outages_vz.csv"));
        log.info("upload to s3 finished");
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

    private List<Asset> getAllEngines(String[] serialFilter) {
        ListRequest.ListRequestBuilder builder = ListRequest.builder().assetType(DianeKpisConstants.MODEL).property(DianeKpisConstants.COMMISSION_PROP);

        if (serialFilter != null) {
            builder.serialNumbers(Arrays.asList(serialFilter));
        }

        AssetsResponse assetsResponse = assetApi.list(builder.build());
        return assetsResponse.getData();
    }
}
