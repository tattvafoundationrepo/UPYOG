import React, { useState, useEffect } from "react";
import { useTranslation } from "react-i18next";
import { CardLabel, LabelFieldPair, TextInput } from "@upyog/digit-ui-react-components";
import { Controller } from "react-hook-form";

const NumberOfDeadAnimalsField = ({ control, data, setData }) => {
  const { t } = useTranslation();
  const [error, setError] = useState("");

  const handleInputChange = (e, field) => {
    const value = e.target.value;

    if (value >= 0 && value < 10000) {
      field.onChange(value);
      const newData = {
        ...data,
        numberOfDeadAnimals: value,
      };
      setData(newData);
      setError(""); // Clear error if input is valid
    } else {
      setError(t("CORE_COMMON_INVALID_RANGE_ERRMSG")); // Display error if out of range
    }
  };

  useEffect(() => {
    if (!data.numberOfDeadAnimals) {
      setError(t("CORE_COMMON_REQUIRED_ERRMSG"));
    }
  }, [data]);

  return (
    <div className="bmc-col3-card">
      <LabelFieldPair>
        <CardLabel className="bmc-label">{t("DEONAR_NUMBER_OF_DEAD_ANIMALS")} &nbsp;{error && <sup style={{ color: "red", fontSize: "x-small" }}>{error}</sup>}</CardLabel>
        <Controller
          control={control}
          name="numberOfDeadAnimals"
          render={(field) => (
            <div>
              <TextInput
                value={field.value}
                onChange={(e) => handleInputChange(e, field)}
                onBlur={field.onBlur}
                optionKey="i18nKey"
                t={t}
                placeholder={t("DEONAR_NUMBER_OF_DEAD_ANIMALS")}
              />
            </div>
          )}
        />
      </LabelFieldPair>
    </div>
  );
};

export default NumberOfDeadAnimalsField;
