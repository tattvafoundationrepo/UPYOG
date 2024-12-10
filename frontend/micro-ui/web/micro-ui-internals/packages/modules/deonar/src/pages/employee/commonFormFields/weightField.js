import React, { useState } from "react";
import { useTranslation } from "react-i18next";
import { CardLabel, Dropdown, LabelFieldPair, TextInput, DatePicker } from "@upyog/digit-ui-react-components";
import { Controller, useForm } from "react-hook-form";

const WeightField = ({name, label, control, data, setData}) => {
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
                    <TextInput
                    type="number"
                    value={props.value}
                    onChange={(e) => {
                      props.onChange(e.target.value);
                      const newData = {
                        ...data,
                        [name]: e.target.value
                      };
                      setData(newData);
                    }}
                    onBlur={props.onBlur}
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

export default WeightField;
