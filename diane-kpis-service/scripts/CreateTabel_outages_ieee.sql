DROP TABLE IF EXISTS  ram_automation.outages_ieee;
CREATE TABLE  ram_automation.outages_ieee (
    site VARCHAR(250),
    serial VARCHAR(50),
    timezone VARCHAR(50),
    outage_from BIGINT,
    outage_to BIGINT,
    outage_from_gmt VARCHAR(50),
    outage_to_gmt VARCHAR(50),
    outage_from_local VARCHAR(50),
    outage_to_local VARCHAR(50),
    duration NUMERIC,
    responsibility VARCHAR(20),
    causal_alarm_code INT,
    causal_alarm_text VARCHAR(250),
    state VARCHAR(50),
    outageNumber INT
);

DROP TABLE IF EXISTS  ram_automation.merged_outages_ieee;
CREATE TABLE  ram_automation.merged_outages_ieee (
    site VARCHAR(250),
    serial VARCHAR(50),
    timezone VARCHAR(50),
    outage_from BIGINT,
    outage_to BIGINT,
    outage_from_gmt VARCHAR(50),
    outage_to_gmt VARCHAR(50),
    outage_from_local VARCHAR(50),
    outage_to_local VARCHAR(50),
    duration NUMERIC,
    responsibility VARCHAR(20),
    causal_alarm_code INT,
    causal_alarm_text VARCHAR(250),
    state VARCHAR(50),
    outageNumber INT
);

