import React, { useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { CardLabel, Dropdown, LabelFieldPair, TextInput, DatePicker } from "@upyog/digit-ui-react-components";
import { Controller, useForm } from "react-hook-form";

const DeathRemovalTypesDropdownField = ({setDeathType, options, control, data, setData}) => {
  const { t } = useTranslation();
  const [error, setError] = useState("");

  useEffect(() => {
    if (!data.deathRemovalType) {
      setError(t("CORE_COMMON_REQUIRED_ERRMSG"));
    }
    else {
      setError("");
    }
  }, [data]);

  return (
    <div className="bmc-col3-card">
        <LabelFieldPair>
            <CardLabel className="bmc-label">{t("DEONAR_DEATH_REMOVAL_TYPE")} &nbsp;{error && <sup style={{ color: "red", fontSize: "x-small" }}>{error}</sup>}</CardLabel>
            <Controller
            control={control}
            name="deathRemovalType"
            rules={{ required: t("CORE_COMMON_REQUIRED_ERRMSG") }}
            render={(props) => (
                <Dropdown
                selected={props.value}
                select={
                    (value) => {
                      props.onChange(value);
                      const newData = {
                        ...data,
                        deathRemovalType: value
                      };
                      setData(newData);
                      setDeathType(value);
                    }
                }
                onBlur={props.onBlur}
                optionKey="name"
                t={t}
                placeholder={t("DEONAR_DEATH_REMOVAL_TYPE")}
                option={options}
                />
            )}
            />
        </LabelFieldPair>
    </div>
  );
};

export default DeathRemovalTypesDropdownField;
