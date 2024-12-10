import React, { useState } from "react";
import { useTranslation } from "react-i18next";
import { CardLabel, Dropdown, LabelFieldPair, TextInput, DatePicker } from "@upyog/digit-ui-react-components";
import { Controller, useForm } from "react-hook-form";

const HealthStatDropdownField = ({label, name, control, setData, data, options, required}) => {
  const { t } = useTranslation();

  return (
    <div className="bmc-col3-card">
        <LabelFieldPair>
            <CardLabel className="bmc-label">{t(label)}</CardLabel>
            <Controller
                control={control}
                name={name}
                rules={{ required: t("CORE_COMMON_REQUIRED_ERRMSG") }}
                render={(props) => (
                    <div>
                    <Dropdown
                        value={props.value}
                        name={name}
                        selected={props.value}
                        select={(value) => {
                          props.onChange(value);
                          const newData = {
                            ...data,
                            [name]: value
                          };
                          setData(newData);
                        }}
                        onBlur={props.onBlur}
                        optionKey="name"
                        option={options}
                        t={t}
                        placeholder={t(label)}
                        required={required || false}
                    />
                    </div>
                )}
            />
        </LabelFieldPair>
    </div>
  );
};

export default HealthStatDropdownField;
