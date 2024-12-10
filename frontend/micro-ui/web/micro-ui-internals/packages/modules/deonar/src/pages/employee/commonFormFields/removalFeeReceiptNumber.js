import React, { useState, useEffect } from "react";
import { useTranslation } from "react-i18next";
import { CardLabel, LabelFieldPair, TextInput } from "@upyog/digit-ui-react-components";
import { Controller } from "react-hook-form";

const RemovalFeeReceiptNumberField = ({ control, data, setData, disabled }) => {
  const { t } = useTranslation();
  const [error, setError] = useState("");

  // Custom validation function for alphanumeric value
  const validateAlphanumeric = (value) => {
    const alphanumericRegex = /^[a-zA-Z0-9]*$/;
    if (!alphanumericRegex.test(value)) {
      return t("ALPHANUMERIC_ERROR"); // Custom error message for non-alphanumeric values
    }
    return true; // Return true if validation is successful
  };

  useEffect(() => {
    if (!data.removalFeeReceiptNumber) {
      setError(t("CORE_COMMON_REQUIRED_ERRMSG")); // Set required field error
    } else {
      setError("");
    }
  }, [data, t]);

  useEffect(() => {
    if (disabled) {
      setError("");
    }
  }, [disabled]);

  return (
    <div className="bmc-col3-card">
      <LabelFieldPair>
        <CardLabel className="bmc-label">{t("DEONAR_REMOVAL_FEE_RECEIPT_NUMBER")}</CardLabel>
        <Controller
          control={control}
          name="removalFeeReceiptNumber"
          rules={{
            required: t("CORE_COMMON_REQUIRED_ERRMSG"),
            validate: validateAlphanumeric, // Apply custom validation function
          }}
          render={({ value, onChange, onBlur, field }) => (
            <div>
              <TextInput
                value={value}
                onChange={(e) => {
                  const inputValue = e.target.value;
                  // Perform validation on input change
                  const validationError = validateAlphanumeric(inputValue);
                  
                  if (validationError !== true) {
                    setError(validationError);
                  } else {
                    setError("");
                    onChange(inputValue);
                    const newData = {
                      ...data,
                      removalFeeReceiptNumber: inputValue,
                    };
                    setData(newData);
                  }
                }}
                onBlur={onBlur}
                optionKey="i18nKey"
                t={t}
                placeholder={t("DEONAR_REMOVAL_FEE_RECEIPT_NUMBER")}
                disabled={disabled}
              />
              {error && <div style={{ color: "red" }}>{error}</div>}
            </div>
          )}
        />
      </LabelFieldPair>
    </div>
  );
};

export default RemovalFeeReceiptNumberField;
