import React, { useState } from "react";
import { useTranslation } from "react-i18next";
import { CardLabel, Dropdown, LabelFieldPair, TextInput, DatePicker } from "@egovernments/digit-ui-react-components";
import { Controller, useForm } from "react-hook-form";

const DeathRemovalTypesDropdownField = ({setDeathType, options}) => {
  const { t } = useTranslation();

  const {
    control,
    setValue,
    handleSubmit,
    getValues,
    formState: { errors, isValid },
  } = useForm({ defaultValues: {}, mode: "onChange" });

  return (
    <div className="bmc-col3-card">
        <LabelFieldPair>
            <CardLabel className="bmc-label">{t("DEONAR_DEATH_REMOVAL_TYPE")}</CardLabel>
            <Controller
            control={control}
            name={"deathRemovalType"}
            rules={{ required: t("CORE_COMMON_REQUIRED_ERRMSG") }}
            render={({ value, onChange, onBlur }) => (
                <Dropdown
                selected={value}
                select={
                    (value) => {
                    onChange(value);
                    setDeathType(value.name);
                    }
                }
                onBlur={onBlur}
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
