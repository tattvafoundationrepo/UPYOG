import React, { useState } from "react";
import { useTranslation } from "react-i18next";
import { CardLabel, LabelFieldPair, TextInput } from "@upyog/digit-ui-react-components";
import { Controller } from "react-hook-form";

const ReceiverField = ({ name, label, control, data, setData }) => {
  const { t } = useTranslation();
  const [error, setError] = useState("");

  // Function to validate and filter out invalid characters
  const filterInvalidCharacters = (value, isMobileNumber) => {
    setError("");
    if (isMobileNumber) {
      // Allow only digits
      
      return value.replace(/[^0-9]/g, '');
    } else {
      // Allow only alphabetic characters and spaces
      return value.replace(/[^a-zA-Z\s]/g, '');
    }
  };

  // Function to validate input based on field name
  const validateInput = (value) => {
    if (name === "receiverContact") {
      // Mobile number validation: 10 digits
      if (value.length === 0) setError(t("CORE_COMMON_REQUIRED_ERRMSG"));
      if (!/^\d{10}$/.test(value)) setError(t("CORE_COMMON_REQUIRED_ERRMSG")); // Adjust the message as needed
    } else {
      // Person name validation
      if (value.length === 0) setError(t("CORE_COMMON_REQUIRED_ERRMSG"));
      if (!/^[a-zA-Z\s]+$/.test(value)) setError(t("CORE_COMMON_REQUIRED_ERRMSG")); // Adjust the message as needed
    }
    return true;
  };

  return (
    <div className="bmc-col3-card">
      <LabelFieldPair>
        <CardLabel className="bmc-label">
          {t(label)}&nbsp;{error && <sup style={{ color: "red", fontSize: "x-small" }}>{error}</sup>}
        </CardLabel>
        <Controller
          control={control}
          name={name}
          rules={{
            validate: validateInput // Apply conditional validation
          }}
          render={({ value, onChange, onBlur }) => (
            <div>
              <TextInput
                value={value || ""}
                onChange={(e) => {
                  // Determine if we are validating a mobile number
                  const isMobileNumber = name === "receiverContact";
                  const filteredValue = filterInvalidCharacters(e.target.value, isMobileNumber);
                  
                  onChange(filteredValue); // Update the value in react-hook-form
                  setData((prevData) => ({
                    ...prevData,
                    [name]: filteredValue
                  }));
                }}
                onBlur={onBlur}
                optionKey="i18nKey"
                t={t}
                placeholder={t(label)}
              />
            </div>
          )}
        />
      </LabelFieldPair>
    </div>
  );
};

export default ReceiverField;
