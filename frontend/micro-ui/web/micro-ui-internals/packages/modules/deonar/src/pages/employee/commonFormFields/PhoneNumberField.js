import React, { useState } from "react";
import { useTranslation } from "react-i18next";
import { CardLabel, LabelFieldPair, TextInput } from "@upyog/digit-ui-react-components";
import { Controller } from "react-hook-form";

const PhoneNumberField = ({ name, label, control, data, setData }) => {
  const { t } = useTranslation();
  const [error, setError] = useState("");
  const filterInvalidCharacters = (value) => {
    setError("");
    return value.replace(/[^0-9]/g, '').slice(0, 10);
  };
  const validateInput = (value) => {
    if (value?.length === 0) {
      setError(t("CORE_COMMON_REQUIRED_ERRMSG"));
      return false;
    }
    if (value?.length !== 10) {
      setError(t("CORE_COMMON_REQUIRED_ERRMSG"));
      return false;
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
            validate: validateInput 
          }}
          render={({ value, onChange, onBlur }) => (
            <div>
              <TextInput
                value={value || ""}
                onChange={(e) => {
                  const filteredValue = filterInvalidCharacters(e.target.value);
                  onChange(filteredValue); 
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

export default PhoneNumberField;
