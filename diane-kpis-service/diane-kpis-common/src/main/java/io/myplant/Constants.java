package io.myplant;

import java.util.Arrays;
import java.util.List;

public class Constants {
    public static final String ASSET_SERIALS_TOKEN = "serial-numbers";
    public static final String ASSET_MODEL_TOKEN = "assetType";
    public static final String DATE_STRING_TOKEN = "date-string";
    public static final String DAILY_TOKEN = "daily";
    //public static final String STATE_STRING_TOKEN = "state-strings";
    public static final String CALC_NEW_TOKEN = "calculate-new";
    public static final String FROM_TOKEN = "from";
    public static final String TO_TOKEN = "to";
    public static final String OUTAGES_TYPE_TOKEN = "outage-type";
    public static final String STATE_TYPE_TOKEN = "state-type";
    public static final String LANGUAGE_TOKEN = "lang";
    public static final String USE_MERGED_TOKEN = "use-merged";

    public static final String FILTER_UNPLANNED_MAINTENANCE = "filter-unplanned-maintenance";

    public static List<Long> KIEL_FLEET_IDS = Arrays.asList(
            117057L, 117002L, 117081L, 117912L, 117086L, 117082L,
            117083L, 117085L, 117087L, 116871L, 116249L, 116250L,
            116295L, 116253L, 117084L, 115964L, 115965L, 115807L,
            115708L, 115706L);

}
