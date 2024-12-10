import React, { useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { CardLabel, LabelFieldPair, TextInput } from "@upyog/digit-ui-react-components";
import { Controller } from "react-hook-form";

const PaymentReferenceNumberField = ({ control, setData, data, disabled, style }) => {
  const { t } = useTranslation();
  const [error, setError] = useState("");

  // Custom validation function for alphanumeric characters
  const validateAlphanumeric = (value) => {
    const alphanumericRegex = /^[a-zA-Z0-9]*$/;
    if (!alphanumericRegex.test(value)) {
      return t("ALPHANUMERIC_ONLY_ERROR"); // Custom error message
    }
    return true;
  };

  useEffect(() => {
    if (!data.paymentReferenceNumber) {
      setError(t("CORE_COMMON_REQUIRED_ERRMSG"));
    } else {
      setError("");
    }
  }, [data]);

  useEffect(() => {
    if (disabled) {
      setError("");
    }
  }, [disabled]);

  return (
    <div className="bmc-col3-card" style={style}>
      <LabelFieldPair>
        <CardLabel className="bmc-label">{t("DEONAR_PAYMENT_REFERENCE_NUMBER")} &nbsp;{error && <sup style={{ color: "red", fontSize: "x-small" }}>{error}</sup>}</CardLabel>
        <Controller
          control={control}
          name="paymentReferenceNumber"
          rules={{
            required: t("CORE_COMMON_REQUIRED_ERRMSG"),
            validate: validateAlphanumeric, // Custom validation function
          }}
          render={(field) => (
            <div>
              <TextInput
                value={field.value}
                onChange={(e) => {
                  const inputValue = e.target.value;
                  const isValid = validateAlphanumeric(inputValue);
                  if (isValid === true) {
                    field.onChange(inputValue);
                    const newData = {
                      ...data,
                      paymentReferenceNumber: inputValue,
                    };
                    setData(newData);
                    setError(""); // Clear any existing error
                  } else {
                    setError(isValid); // Set validation error
                  }
                }}
                onBlur={field.onBlur}
                optionKey="i18nKey"
                t={t}
                placeholder={t("DEONAR_PAYMENT_REFERENCE_NUMBER")}
                disabled={disabled}
              />
            </div>
          )}
        />
      </LabelFieldPair>
    </div>
  );
};

export default PaymentReferenceNumberField;
