import React, { useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { CardLabel, Dropdown, LabelFieldPair, TextInput, DatePicker } from "@upyog/digit-ui-react-components";
import { Controller, useForm } from "react-hook-form";

const ReferenceNumberField = ({control, data, setData, style}) => {
  const { t } = useTranslation();
  const [error, setError] = useState("");

  useEffect(() => {
    if (!data.referenceNumber) {
      setError(t("*"));
    }
    else {
      setError("");
    }
  }, [data]);

  const validateAlphanumeric = (value) => {
    const regex = /^[a-zA-Z0-9]*$/; // Alphanumeric validation
    if (value === "") {
      setError(t("CORE_COMMON_REQUIRED_ERRMSG")); // Error message for empty value
    } else if (!regex.test(value)) {
      setError(t("ONLY_ALPHANUMERIC_CHARACTERS_ALLOWED")); // Error message for non-alphanumeric characters
    }
    else {
      setError("");
    }
    return true;
  };

  return (
    <div className="bmc-col3-card" style={style}>
        <LabelFieldPair>
            <CardLabel className="bmc-label">{t("DEONAR_REFERENCE_NUMBER")}&nbsp;{error && <sup style={{ color: "red", fontSize: "x-small" }}>{error}</sup>}</CardLabel>
            <Controller
                control={control}
                name="referenceNumber"
                rules={{ required: t("CORE_COMMON_REQUIRED_ERRMSG") }}
                render={(props) => (
                <div>
                    <TextInput
                    value={props.value}
                    onChange={(e) => {
                      props.onChange(e.target.value);
                      validateAlphanumeric(e.target.value);
                        const newData = {
                          ...data,
                          referenceNumber: e.target.value
                        };
                        setData(newData);
                   }}
                    onBlur={props.onBlur}
                    optionKey="i18nKey"
                    t={t}
                    placeholder={t("DEONAR_REFERENCE_NUMBER")}
                    />
                </div>
                )}
            />
        </LabelFieldPair>
      </div>
  );
};

export default ReferenceNumberField;
