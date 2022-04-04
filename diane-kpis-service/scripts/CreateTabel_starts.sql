DROP TABLE IF EXISTS  ram_automation.starts;
CREATE TABLE  ram_automation.starts (
    serial VARCHAR(50),
    start_date VARCHAR(50),
    start_date_gmt VARCHAR(50),
    start_date_local VARCHAR(50),
    responsibility VARCHAR(20),
    valid_start_gcb_close SMALLINT,
    valid_start_target_load SMALLINT,
	excluded SMALLINT,
    excluded_vu SMALLINT,
    time_to_mains_parallel INT,
    trip_before_ramp_up_mains_parallel SMALLINT,
    trip_before_mains_parallel SMALLINT,
    causal_alarm_code INT,
    causal_alarm_text VARCHAR(250),
    reason VARCHAR(250),
    outageNumber INT
      );

DROP TABLE IF EXISTS  ram_automation.merged_starts;
CREATE TABLE  ram_automation.merged_starts (
    serial VARCHAR(50),
    start_date VARCHAR(50),
    start_date_gmt VARCHAR(50),
    start_date_local VARCHAR(50),
    responsibility VARCHAR(20),
    valid_start_gcb_close SMALLINT,
    valid_start_target_load SMALLINT,
    excluded SMALLINT,
    excluded_vu SMALLINT,
    time_to_mains_parallel INT,
    trip_before_ramp_up_mains_parallel SMALLINT,
    trip_before_mains_parallel SMALLINT,
    causal_alarm_code INT,
    causal_alarm_text VARCHAR(250),
    reason VARCHAR(250),
    outageNumber INT
      );
