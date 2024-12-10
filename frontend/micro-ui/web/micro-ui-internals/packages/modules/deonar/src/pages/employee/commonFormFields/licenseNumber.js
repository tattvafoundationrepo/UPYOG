import React, { useState, useEffect } from "react";
import { useTranslation } from "react-i18next";
import { CardLabel, LabelFieldPair, TextInput } from "@upyog/digit-ui-react-components";
import { Controller } from "react-hook-form";

const LicenseNumberField = ({ control, data, setData, disabled, style }) => {
  const { t } = useTranslation();
  const [formField, setFormField] = useState(data.licenseNumber || "");
  const [error, setError] = useState("");

  // Validate the license number
  const validateLicenseNumber = (value) => {
    const regex = /^[a-zA-Z0-9]*$/; // Allow only alphanumeric characters

    if (value.length === 0) {
      return t("CORE_COMMON_REQUIRED_ERRMSG"); // Error for empty field
    } else if (!regex.test(value)) {
      return t("ONLY_ALPHANUMERIC_CHARACTERS_ALLOWED"); // Error for invalid characters
    }
    return true;
  };

  const handleInputChange = (e, props) => {
    const value = e.target.value;
    const validationError = validateLicenseNumber(value);

    if (validationError === true) {
      setError(""); // Clear error if input is valid
    } else {
      setError(validationError); // Set error if invalid
    }

    // Update form state and data
    props.onChange(value);
    const newData = {
      ...data,
      licenseNumber: value,
    };
    setData(newData);
    setFormField(value);
  };

  useEffect(() => {
    if (disabled) {
      setError("");
    }
  }, [disabled]);

  return (
    <div className="bmc-col3-card" style={style}>
      <LabelFieldPair>
        <CardLabel className="bmc-label">{t("DEONAR_LICENSE_NUMBER")} &nbsp;{error && <sup style={{ color: "red", fontSize: "x-small" }}>{error}</sup>}</CardLabel>
        <Controller
          control={control}
          name="licenseNumber"
          render={({ value, onChange, onBlur }) => (
            <div>
              <TextInput
                value={value}
                onChange={(e) => handleInputChange(e, { onChange })}
                onBlur={onBlur}
                optionKey="i18nKey"
                t={t}
                placeholder={t("DEONAR_LICENSE_NUMBER")}
                disabled={disabled}
              />
            </div>
          )}
        />
      </LabelFieldPair>
    </div>
  );
};

export default LicenseNumberField;
