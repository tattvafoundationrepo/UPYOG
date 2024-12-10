import React, { useState, useEffect } from "react";
import { useTranslation } from "react-i18next";
import { CardLabel, LabelFieldPair, TextInput } from "@upyog/digit-ui-react-components";
import { Controller } from "react-hook-form";

const AnimalTokenNumberField = ({ control, data, setData, style}) => {
  const { t } = useTranslation();
  const [error, setError] = useState("");

  useEffect(() => {
    // Validate initial value when data changes
    if (!data.animalTokenNumber) {
      setError(t("CORE_COMMON_REQUIRED_ERRMSG"));
    } else if (data.animalTokenNumber < 0) {
      setError(t("CORE_COMMON_INVALID_VALUE_ERRMSG")); // Custom error message for values less than 0
    } else {
      setError("");
    }
  }, [data, t]);

  const handleInputChange = (e, props) => {
    const value = e.target.value.replace(/\D/g, ""); // Remove any non-digit characters

    // Allow empty string to handle clearing the input
    if (value || e.target.value === "") {
      props.onChange(value);
      const newData = {
        ...data,
        animalTokenNumber: value,
      };
      setData(newData);
      setError(""); // Clear any error if the input is valid or empty
    } else {
      setError(t("CORE_COMMON_INVALID_VALUE_ERRMSG")); // Set error if no valid input
    }
  };

  return (
    <div className="bmc-col3-card" style={style}>
      <LabelFieldPair>
        <CardLabel className="bmc-label">{t("DEONAR_ANIMAL_TOKEN_NUMBER")}&nbsp;{error && <sup style={{ color: "red", fontSize: "x-small" }}>{error}</sup>}</CardLabel>
        <Controller
          control={control}
          name="animalTokenNumber"
          rules={{ required: t("CORE_COMMON_REQUIRED_ERRMSG") }}
          render={(props) => (
            <div>
              <TextInput
                type="text" // Use type="text" to handle manual sanitization
                value={props.value}
                onChange={(e) => handleInputChange(e, props)}
                onBlur={props.onBlur}
                optionKey="i18nKey"
                t={t}
                placeholder={t("DEONAR_ANIMAL_TOKEN_NUMBER")}
              />
            </div>
          )}
        />
      </LabelFieldPair>
    </div>
  );
};

export default AnimalTokenNumberField;
