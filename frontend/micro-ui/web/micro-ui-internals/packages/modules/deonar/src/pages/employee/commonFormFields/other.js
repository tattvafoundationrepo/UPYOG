import React, { useState, useEffect } from "react";
import { useTranslation } from "react-i18next";
import { CardLabel, LabelFieldPair, TextInput } from "@upyog/digit-ui-react-components";
import { Controller } from "react-hook-form";

const OtherField = ({ control, setData, data }) => {
  const { t } = useTranslation();
  const [error, setError] = useState("");

  const handleInputChange = (e, props) => {
    const value = e.target.value;
    // Update form state and data
    props.onChange(value);
    const newData = {
      ...data,
      other: value,
    };
    setData(newData);

    // Custom validation: Set error if value is empty
    if (value.length === 0) {
      setError(t("CORE_COMMON_REQUIRED_ERRMSG"));
    } else {
      setError("");
    }
  };

  return (
    <div className="bmc-col3-card">
      <LabelFieldPair>
        <CardLabel className="bmc-label">{t("DEONAR_OTHER")} &nbsp;{error && <sup style={{ color: "red", fontSize: "x-small" }}>{error}</sup>}</CardLabel>
        <Controller
          control={control}
          name="other"
          rules={{ required: t("CORE_COMMON_REQUIRED_ERRMSG") }}
          render={({ value, onChange, onBlur, error }) => (
            <div>
              <TextInput
                value={value}
                onChange={(e) => handleInputChange(e, { onChange })}
                onBlur={onBlur}
                optionKey="i18nKey"
                t={t}
                placeholder={t("DEONAR_OTHER")}
              />
              {/* Display validation error message if present */}
              {error && <div style={{ color: "red" }}>{error.message}</div>}
            </div>
          )}
        />
      </LabelFieldPair>
    </div>
  );
};

export default OtherField;
