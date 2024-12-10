import React, { useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { CardLabel, LabelFieldPair, TextInput } from "@upyog/digit-ui-react-components";
import { Controller } from "react-hook-form";

const CitizenNameField = ({ control, data, setData }) => {
  const { t } = useTranslation();
  const [error, setError] = useState("");

  // Validation function to check if value contains only letters and spaces
  const validateName = (value) => {
    const regex = /^[a-zA-Z\s]*$/; // Allows only letters and spaces
    if (!regex.test(value)) {
      return t("INVALID_NAME_ERROR"); // Custom error message for invalid characters
    }
    return true;
  };

  // Handle input change to validate and update state
  const handleInputChange = (e, props) => {
    const value = e.target.value;
    const validationError = validateName(value);

    if (validationError === true) {
      setError("");
      props.onChange(value);
      const newData = {
        ...data,
        citizenName: value,
      };
      setData(newData);
    } else {
      setError(validationError); // Show error message
    }
  };

  // UseEffect for initial validation
  useEffect(() => {
    if (!data.citizenName) {
      setError(t("CORE_COMMON_REQUIRED_ERRMSG"));
    } else {
      setError("");
    }
  }, [data, t]);

  return (
    <div className="bmc-col3-card">
      <LabelFieldPair>
        <CardLabel className="bmc-label">{t("DEONAR_CITIZEN_NAME")} &nbsp;{error && <sup style={{ color: "red", fontSize: "x-small" }}>{error}</sup>}</CardLabel>
        <Controller
          control={control}
          name="citizenName"
          render={(props) => (
            <div>
              <TextInput
                value={props.value}
                onChange={(e) => handleInputChange(e, props)}
                onBlur={(e) => {
                  // Validate onBlur as well to cover cases where user might finish typing and move away
                  const validationError = validateName(e.target.value);
                  setError(validationError === true ? "" : validationError);
                }}
                optionKey="i18nKey"
                t={t}
                placeholder={t("DEONAR_CITIZEN_NAME")}
              />
            </div>
          )}
        />
      </LabelFieldPair>
    </div>
  );
};

export default CitizenNameField;
