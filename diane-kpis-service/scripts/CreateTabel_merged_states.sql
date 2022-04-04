DROP TABLE IF EXISTS  ram_automation.merged_states;
CREATE TABLE  ram_automation.merged_states (
    serial VARCHAR(50),
    timezone VARCHAR(50),
    action_actual VARCHAR(50),
    available_state VARCHAR(50),
    ieee_state VARCHAR(50),
    vu_state VARCHAR(50),
    vz_state VARCHAR(50),
    action_from VARCHAR(50),
    action_to VARCHAR(50),
    action_from_gmt VARCHAR(50),
    action_to_gmt VARCHAR(50),
    action_from_local VARCHAR(50),
    action_to_local VARCHAR(50),
    time_zone_offset VARCHAR(10),
    causal_alarm_code INT,
    causal_alarm_text VARCHAR(250),
    alarm_count SMALLINT,
    responsibility VARCHAR(20),
    demand_switch VARCHAR(50),
    service_switch VARCHAR(50),
    ram_switch VARCHAR(50),

    outageNumber INT,
    first_row SMALLINT,
    durationHrs NUMERIC,
    OH NUMERIC,
    CumOH NUMERIC,
    AOH NUMERIC,
    CumAOH NUMERIC,
    PH NUMERIC,
    CumPH NUMERIC,
    OHatLastFOO NUMERIC,
    HSLF NUMERIC
);

