#!/bin/bash
now="$(date)"
printf "Start import script at: %s\n" "$now"

if [ -e /root/import_states/merged_states.csv ]
then
    #cp /mnt/state-machine/merged_states.csv /root/import_states/merged_states.csv

    md5f1=$(md5sum "/root/import_states/merged_states.csv" | cut -d' ' -f1)
    md5f2=$(md5sum "/tmp/merged_states.csv" | cut -d' ' -f1)

    if [ "$md5f2" == "$md5f1" ]; then
        echo "no files changes exit"
    else
        echo "The files are different, starting script!"


        echo ""
        echo "Delete old starts tables, and create new"
        psql "dbname=rel_tableau host=127.0.0.1 user=myPlant password=P@ssword" -a -f CreateTabel_starts.sql

        if [ -e /root/import_states/starts.csv ]
        then
            #cp /mnt/state-machine/starts.csv /root/import_states/starts.csv
            echo "import starts"
            psql "dbname=rel_tableau host=127.0.0.1 user=myPlant password=P@ssword" -a -c "COPY  ram_automation.starts FROM STDIN WITH DELIMITER ',' CSV HEADER " <  /root/import_states/starts.csv
        fi

        if [ -e /root/import_states/merged_starts.csv ]
        then
            #cp /mnt/state-machine/merged_starts.csv /root/import_states/merged_starts.csv
            echo "import merged starts"
            psql "dbname=rel_tableau host=127.0.0.1 user=myPlant password=P@ssword" -a -c "COPY  ram_automation.merged_starts FROM STDIN WITH DELIMITER ',' CSV HEADER " <  /root/import_states/merged_starts.csv
        fi

        if [ -e /root/import_states/states.csv ]
        then
            #echo "copy states"
            #cp /mnt/state-machine/states.csv /root/import_states/states.csv

            echo "Delete old states tables, and create new"
            psql "dbname=rel_tableau host=127.0.0.1 user=myPlant password=P@ssword" -a -f CreateTabel_states.sql

            echo ""
            echo "import states"
            psql "dbname=rel_tableau host=127.0.0.1 user=myPlant password=P@ssword" -a -c "COPY  ram_automation.states FROM STDIN WITH DELIMITER ',' CSV HEADER " <  /root/import_states/states.csv
        fi


        if [ -e /root/import_states/outages_ieee.csv ]
        then
            #echo "copy outages"
            #cp /mnt/state-machine/outages_ieee.csv /root/import_states/outages_ieee.csv
            #cp /mnt/state-machine/merged_outages_ieee.csv /root/import_states/merged_outages_ieee.csv
            echo "Delete old outage ieee tables, and create new"
            psql "dbname=rel_tableau host=127.0.0.1 user=myPlant password=P@ssword" -a -f CreateTabel_outages_ieee.sql

            echo "import ieee outages"
            psql "dbname=rel_tableau host=127.0.0.1 user=myPlant password=P@ssword" -a -c "COPY  ram_automation.outages_ieee FROM STDIN WITH DELIMITER ',' CSV HEADER " <  /root/import_states/outages_ieee.csv

            echo "import merged ieee outages"
            psql "dbname=rel_tableau host=127.0.0.1 user=myPlant password=P@ssword" -a -c "COPY  ram_automation.merged_outages_ieee FROM STDIN WITH DELIMITER ',' CSV HEADER " <  /root/import_states/merged_outages_ieee.csv
        fi

        if [ -e /root/import_states/outages_vu.csv ]
        then
            #echo "copy outages"
            #cp /mnt/state-machine/outages_vu.csv /root/import_states/outages_vu.csv
            #cp /mnt/state-machine/merged_outages_vu.csv /root/import_states/merged_outages_vu.csv
            echo "Delete old outage vu tables, and create new"
            psql "dbname=rel_tableau host=127.0.0.1 user=myPlant password=P@ssword" -a -f CreateTabel_outages_vu.sql

            echo "import vu outages"
            psql "dbname=rel_tableau host=127.0.0.1 user=myPlant password=P@ssword" -a -c "COPY  ram_automation.outages_vu FROM STDIN WITH DELIMITER ',' CSV HEADER " <  /root/import_states/outages_vu.csv

            echo "import merged vu outages"
            psql "dbname=rel_tableau host=127.0.0.1 user=myPlant password=P@ssword" -a -c "COPY  ram_automation.merged_outages_vu FROM STDIN WITH DELIMITER ',' CSV HEADER " <  /root/import_states/merged_outages_vu.csv
        fi

        if [ -e /root/import_states/outages_vz.csv ]
        then
            #echo "copy outages"
            #cp /mnt/state-machine/outages_vz.csv /root/import_states/outages_vz.csv
            #cp /mnt/state-machine/merged_outages_vz.csv /root/import_states/merged_outages_vz.csv
            echo "Delete old outage vz tables, and create new"
            psql "dbname=rel_tableau host=127.0.0.1 user=myPlant password=P@ssword" -a -f CreateTabel_outages_vz.sql

            echo "import vz outages"
            psql "dbname=rel_tableau host=127.0.0.1 user=myPlant password=P@ssword" -a -c "COPY  ram_automation.outages_vz FROM STDIN WITH DELIMITER ',' CSV HEADER " <  /root/import_states/outages_vz.csv

            echo "import merged vz outages"
            psql "dbname=rel_tableau host=127.0.0.1 user=myPlant password=P@ssword" -a -c "COPY  ram_automation.merged_outages_vz FROM STDIN WITH DELIMITER ',' CSV HEADER " <  /root/import_states/merged_outages_vz.csv
        fi

        if [ -e /root/import_states/merged_states.csv ]
        then
            #echo "copy merged states"
            #cp /mnt/state-machine/merged_states.csv /root/import_states/merged_states.csv

            echo "Delete old merged states tables, and create new"
            psql "dbname=rel_tableau host=127.0.0.1 user=myPlant password=P@ssword" -a -f CreateTabel_merged_states.sql

            echo "import merged states"
            psql "dbname=rel_tableau host=127.0.0.1 user=myPlant password=P@ssword" -a -c "COPY  ram_automation.merged_states FROM STDIN WITH DELIMITER ',' CSV HEADER " <  /root/import_states/merged_states.csv
        fi

        cp /root/import_states/merged_states.csv  /tmp/merged_states.csv


        echo ""
        echo "done"
    fi
    
else
    echo "Files not found, shared directory not mounted??"
fi
