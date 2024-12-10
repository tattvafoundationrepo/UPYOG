import React, { useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { CardLabel, TextInput, LabelFieldPair } from "@upyog/digit-ui-react-components";
import { Controller } from "react-hook-form";

const ArrivalUuidField = ({ control, setData, data, uuid, disabled }) => {
  const { t } = useTranslation();
  const [val, setVal] = useState("");
  const [error, setError] = useState("");

  useEffect(() => {
    if (!data.arrivalUuid) {
      setError(t("CORE_COMMON_REQUIRED_ERRMSG"));
    } else {
      setError("");
    }
  }, [data]);

  useEffect(() => {
    if (uuid) {
      setVal(uuid);
    } else {
      setVal("");
    }
  }, [uuid]);

  const validateAlphanumeric = (value) => {
    const regex = /^[a-zA-Z0-9]*$/; // Regular expression to allow only alphanumeric characters
    return regex.test(value);
  };

  return (
    <React.Fragment>
      <div className="bmc-col3-card" style={disabled ? { display: "none" } : {}}>
        <LabelFieldPair>
          <CardLabel className="bmc-label">
            {t("DEONAR_ARRIVAL_UUID")} &nbsp;{error && <sup style={{ color: "red", fontSize: "x-small" }}>{error}</sup>}
          </CardLabel>
          <Controller
            control={control}
            name="arrivalUuid"
            rules={{ required: t("CORE_COMMON_REQUIRED_ERRMSG") }}
            render={(props) => (
              <TextInput
                value={val}
                onChange={(e) => {
                  const value = e.target.value;
                  if (validateAlphanumeric(value)) {
                    setError("");
                    props.onChange(value);
                    const newData = {
                      ...data,
                      arrivalUuid: value,
                    };
                    setData(newData);
                    setVal(value);
                  } else {
                    setError(t("ONLY_ALPHANUMERIC_CHARACTERS_ALLOWED"));
                  }
                }}
                onBlur={props.onBlur}
                optionKey="i18nKey"
                t={t}
                placeholder={t("DEONAR_ARRIVAL_UUID")}
                disabled={disabled}
              />
            )}
          />
        </LabelFieldPair>
      </div>
    </React.Fragment>
  );
};

export default ArrivalUuidField;
