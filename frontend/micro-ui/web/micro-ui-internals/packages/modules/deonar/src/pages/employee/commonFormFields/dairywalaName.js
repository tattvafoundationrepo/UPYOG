import React, { useState, useEffect } from "react";
import { useTranslation } from "react-i18next";
import { CardLabel, Dropdown, LabelFieldPair } from "@upyog/digit-ui-react-components";
import { Controller } from "react-hook-form";
import { dairywalaNameOptions } from "../../../constants/dummyData";

const DairywalaNameField = ({ control, data, setData, disabled }) => {
  const { t } = useTranslation();
  const [error, setError] = useState("");
  const [options, setOptions] = useState([]);

  useEffect(() => {
    if (!data.dairywalaName) {
      setError(t("CORE_COMMON_REQUIRED_ERRMSG"));
    } else {
      setError("");
    }
  }, [data, t]);

  useEffect(() => {
    setOptions(dairywalaNameOptions);
  }, []);

  useEffect(() => {
    if (disabled) {
      setError("");
    }
  }, [disabled]);

  return (
    <div className="bmc-col3-card">
      <LabelFieldPair>
        <CardLabel className="bmc-label">{t("DEONAR_DAIRYWALA_NAME")} &nbsp;{error && <sup style={{ color: "red", fontSize: "x-small" }}>{error}</sup>}</CardLabel>
        <Controller
          control={control}
          name="dairywalaName"
          rules={{ required: t("CORE_COMMON_REQUIRED_ERRMSG") }}
          render={(field) => (
            <div>
              <Dropdown
                selected={field.value}
                select={(value) => {
                  field.onChange(value);
                  const newData = {
                    ...data,
                    dairywalaName: value
                  };
                  setData(newData);
                }}
                onBlur={field.onBlur}
                optionKey="name"
                t={t}
                placeholder={t("DEONAR_DAIRYWALA_NAME")}
                option={options}
                required={true}
                disabled={disabled}
              />
              {field.error && <div style={{ color: "red" }}>{field.error.message}</div>}
            </div>
          )}
        />
      </LabelFieldPair>
    </div>
  );
};

export default DairywalaNameField;
