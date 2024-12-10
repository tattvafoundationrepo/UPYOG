import React, { useState, useEffect } from "react";
import { useTranslation } from "react-i18next";
import { CardLabel, LabelFieldPair, DatePicker } from "@upyog/digit-ui-react-components";
import { Controller } from "react-hook-form";
import useDate from "../../../hooks/useCurrentDate";

const ArrivalDateField = ({ control, data, setData }) => {
  const { t } = useTranslation();
  const currentDate = useDate(0);
  const [formField, setFormField] = useState(currentDate);
  const [error, setError] = useState("");

  useEffect(() => {
    const validateArrivalDate = () => {
      if (!data.arrivalDate) {
        setError(t("CORE_COMMON_REQUIRED_ERRMSG"));
        return;
      }

      const arrivalDate = new Date(data.arrivalDate);
      const importPermissionDate = new Date(data.importPermissionDate);
      const today = new Date();

      if (arrivalDate > today) {
        setError(t("ARRIVAL_DATE_CANNOT_BE_IN_FUTURE"));
      } else if (arrivalDate <= importPermissionDate) {
        setError(t("ARRIVAL_DATE_MUST_BE_AFTER_IMPORT_PERMISSION_DATE"));
      } else {
        setError("");
      }
    };

    validateArrivalDate();
  }, [data]);

  return (
    <div className="bmc-col3-card">
      <LabelFieldPair>
        <CardLabel className="bmc-label">{t("DEONAR_ARRIVAL_DATE")} &nbsp;{error && <sup style={{ color: "red", fontSize: "x-small" }}>{error}</sup>}</CardLabel>
        <Controller
          control={control}
          name="arrivalDate"
          defaultValue={currentDate}
          rules={{ required: t("CORE_COMMON_REQUIRED_ERRMSG") }}
          render={(field) => (
            <div>
              <DatePicker
                date={field.value}
                onChange={(e) => {
                  field.onChange(e);
                  setFormField(e);
                  const newData = {
                    ...data,
                    arrivalDate: e,
                  };
                  setData(newData);
                  
                }}
                onBlur={field.onBlur}
                placeholder={t("DEONAR_ARRIVAL_DATE")}
              />
            </div>
          )}
        />
      </LabelFieldPair>
    </div>
  );
};

export default ArrivalDateField;
