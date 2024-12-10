import React, { useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { CardLabel, LabelFieldPair, TextInput } from "@upyog/digit-ui-react-components";
import { Controller } from "react-hook-form";

const ImportPermissionNumberField = ({ control, setData, data , style}) => {
  const { t } = useTranslation();
  const [error, setError] = useState("");

  // Default useEffect for setting initial error based on data
  useEffect(() => {
    if (!data.importPermissionNumber) {
      setError(t("CORE_COMMON_REQUIRED_ERRMSG"));
    } else {
      setError("");
    }
  }, [data]);

  // Validate the import permission number
  const validateImportPermissionNumber = (value) => {
    const regex = /^[a-zA-Z0-9]*$/; // Allow only alphanumeric characters

    if (value.length === 0) {
      return t("CORE_COMMON_REQUIRED_ERRMSG"); // Custom error message for empty field
    } else if (!regex.test(value)) {
      return t("ONLY_ALPHANUMERIC_CHARACTERS_ALLOWED"); // Custom error message for invalid characters
    }
    return true;
  };

  return (
    <div className="bmc-col3-card" style={style}>
      <LabelFieldPair>
        <CardLabel className="bmc-label">{t("DEONAR_IMPORT_PERMISSION_NUMBER")}&nbsp;{error && <sup style={{ color: "red", fontSize: "x-small" }}>{error}</sup>}</CardLabel>
        <Controller
          control={control}
          name="importPermissionNumber"
          render={({ value, onChange, onBlur }) => (
            <div>
              <TextInput
                value={value || ""} // Ensure the value is controlled
                onChange={(e) => {
                  const value = e.target.value;
                  const validationError = validateImportPermissionNumber(value);

                  if (validationError === true) {
                    setError(""); // Clear error if input is valid
                  } else {
                    setError(validationError); // Set error if invalid
                  }

                  onChange(value); // Update the form state
                  setData((prevData) => ({
                    ...prevData,
                    importPermissionNumber: value,
                  }));
                }}
                onBlur={() => {
                  const validationError = validateImportPermissionNumber(value);
                  setError(validationError === true ? "" : validationError);
                }}
                optionKey="i18nKey"
                t={t}
                placeholder={t("DEONAR_IMPORT_PERMISSION_NUMBER")}
              />
              {/* {error && <div style={{ color: "red" }}>{error}</div>} */}
            </div>
          )}
        />
      </LabelFieldPair>
    </div>
  );
};

export default ImportPermissionNumberField;
