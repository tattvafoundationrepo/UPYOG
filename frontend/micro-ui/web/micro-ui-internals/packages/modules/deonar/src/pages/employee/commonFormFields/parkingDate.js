import React, { useState, useEffect } from "react";
import { useTranslation } from "react-i18next";
import { CardLabel, LabelFieldPair, DatePicker } from "@upyog/digit-ui-react-components";
import { Controller } from "react-hook-form";
import useDate from "../../../hooks/useCurrentDate";

const ParkingDateField = ({ control, data, setData }) => {
  const { t } = useTranslation();
  const currentDate = useDate(0);
  const [error, setError] = useState("");

  useEffect(() => {
    if (!data.parkingDate) {
      setError(t("CORE_COMMON_REQUIRED_ERRMSG"));
    } else {
      setError("");
    }
  }, [data]);

  return (
    <div className="bmc-col3-card">
      <LabelFieldPair>
        <CardLabel className="bmc-label">{t("DEONAR_PARKING_DATE")} &nbsp;{error && <sup style={{ color: "red", fontSize: "x-small" }}>{error}</sup>}</CardLabel>
        <Controller
          control={control}
          name="parkingDate"
          defaultValue={currentDate}
          rules={{ required: t("CORE_COMMON_REQUIRED_ERRMSG") }}
          render={(props) => (
            <DatePicker
              date={props.value || currentDate}  // Ensure currentDate is shown as default
              onChange={(e) => {
                props.onChange(e);
                const newData = {
                  ...data,
                  parkingDate: e,
                };
                setData(newData);
              }}
              onBlur={props.onBlur}
              placeholder={t("DEONAR_PARKING_DATE")}
            />
          )}
        />
      </LabelFieldPair>
    </div>
  );
};

export default ParkingDateField;
