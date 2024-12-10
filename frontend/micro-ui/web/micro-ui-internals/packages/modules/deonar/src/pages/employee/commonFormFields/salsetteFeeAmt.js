import React, { useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { CardLabel, LabelFieldPair, TextInput } from "@upyog/digit-ui-react-components";
import { Controller } from "react-hook-form";

const SalsetteFeeAmountField = ({ control, data, setData }) => {
  const { t } = useTranslation();
  const [error, setError] = useState("");

  useEffect(() => {
    if (!data.salsetteFeeAmount) {
      setError(t("REQUIRED_FIELD"));
    } else {
      setError("");
    }
  }, [data, t]);

  const validateRange = (value) => {
    const numValue = parseFloat(value);
    if (isNaN(numValue) || numValue <= 0 || numValue >= 100000) {
      return t("VALUE_OUT_OF_RANGE_ERROR"); // Custom error message for out-of-range values
    }
    return true;
  };

  return (
    <div className="bmc-col3-card">
      <LabelFieldPair>
        <CardLabel className="bmc-label">{t("DEONAR_SALSETTE_FEE_AMOUNT")}</CardLabel>
        <Controller
          control={control}
          name="salsetteFeeAmount"
          rules={{ required: t("CORE_COMMON_REQUIRED_ERRMSG") }}
          render={({ value, onChange, onBlur }) => (
            <div>
              <TextInput
                type="number"
                value={value}
                onChange={(e) => {
                  const inputValue = e.target.value;
                  const validationError = validateRange(inputValue);
                  
                  if (validationError === true) {
                    setError("");
                    onChange(inputValue); // Update value if valid
                    const newData = {
                      ...data,
                      salsetteFeeAmount: inputValue
                    };
                    setData(newData);
                  } else {
                    setError(validationError); // Show error message
                  }
                }}
                onBlur={onBlur}
                optionKey="i18nKey"
                t={t}
                placeholder={t("DEONAR_SALSETTE_FEE_AMOUNT")}
              />
            </div>
          )}
        />
      </LabelFieldPair>
      {error && <div style={{ color: "red" }}>{error}</div>}
    </div>
  );
};

export default SalsetteFeeAmountField;
