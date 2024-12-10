import React, { useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { CardLabel, LabelFieldPair, TextInput } from "@upyog/digit-ui-react-components";
import { Controller } from "react-hook-form";

const ParkingAmountField = ({ control, data, setData }) => {
  const { t } = useTranslation();
  const [formField, setFormField] = useState('');
  const [error, setError] = useState("");

  // Custom validation function for the parking amount
  const validateAmount = (value) => {
    const numberValue = parseFloat(value);
    if (isNaN(numberValue) || numberValue <= 0 || numberValue >= 100000) {
      return t("PARKING_AMOUNT_VALIDATION_ERROR"); // Custom error message
    }
    return true;
  };

  // Handle input change and validation
  const handleInputChange = (e, props) => {
    const value = e.target.value;
    setFormField(value);

    // Validate amount
    const validationError = validateAmount(value);
    if (validationError !== true) {
      setError(validationError);
    } else {
      setError("");
    }

    // Update form state and data
    props.onChange(value);
    const newData = {
      ...data,
      parkingAmount: value,
    };
    setData(newData);
  };

  useEffect(() => {
    if (!data.parkingAmount) {
      setError(t("CORE_COMMON_REQUIRED_ERRMSG"));
    }
  }, [data]);

  return (
    <div className="bmc-col3-card">
      <LabelFieldPair>
        <CardLabel className="bmc-label">{t("DEONAR_PARKING_AMOUNT")}</CardLabel>
        <Controller
          control={control}
          name="parkingAmount"
          rules={{
            required: t("CORE_COMMON_REQUIRED_ERRMSG"),
          }}
          render={(field) => (
            <div>
              <TextInput
                type="number"
                value={formField}
                onChange={(e) => handleInputChange(e, field)}
                onBlur={field.onBlur}
                onInput={(e) => {
                  // Prevent negative values
                  if (parseFloat(e.target.value) < 0) {
                    e.target.value = '';
                  }
                }}
                optionKey="i18nKey"
                t={t}
                placeholder={t("DEONAR_PARKING_AMOUNT")}
              />
              {field.error && <div style={{ color: "red" }}>{field.error.message}</div>}
              {error && <div style={{ color: "red" }}>{error}</div>}
            </div>
          )}
        />
      </LabelFieldPair>
    </div>
  );
};

export default ParkingAmountField;
