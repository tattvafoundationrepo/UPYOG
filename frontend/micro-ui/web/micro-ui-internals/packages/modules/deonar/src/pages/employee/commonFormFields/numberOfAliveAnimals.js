import React, { useState, useEffect } from "react";
import { useTranslation } from "react-i18next";
import { CardLabel, LabelFieldPair, TextInput } from "@upyog/digit-ui-react-components";
import { Controller } from "react-hook-form";

const NumberOfAliveAnimalsField = ({ control, data, setData }) => {
  const { t } = useTranslation();
  const [error, setError] = useState("");

  const handleInputChange = (e, field) => {
    const value = e.target.value;

    if (value >= 0 && value < 1000) {
      field.onChange(value);
      const newData = {
        ...data,
        numberOfAliveAnimals: value,
      };
      setData(newData);
      setError(""); 
    } else {
      setError(t("CORE_COMMON_INVALID_RANGE_ERRMSG")); 
    }
  };

  useEffect(() => {
    if (!data.numberOfAliveAnimals) {
      setError(t("CORE_COMMON_REQUIRED_ERRMSG"));
    }
  }, [data]);

  return (
    <div className="bmc-col3-card">
      <LabelFieldPair>
        <CardLabel className="bmc-label">{t("DEONAR_NUMBER_OF_ANIMALS")}&nbsp;{error && <sup style={{ color: "red", fontSize: "x-small" }}>{error}</sup>}</CardLabel>
        <Controller
          control={control}
          name="numberOfAliveAnimals"
          render={(field) => (
            <div>
              <TextInput
                type="number"
                value={field.value}
                onChange={(e) => handleInputChange(e, field)}
                onBlur={field.onBlur}
                optionKey="i18nKey"
                t={t}
                placeholder={t("DEONAR_NUMBER_OF_ALIVE_ANIMALS")}
              />
            </div>
          )}
        />
      </LabelFieldPair>
    </div>
  );
};

export default NumberOfAliveAnimalsField;
