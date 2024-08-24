import React, { useState } from "react";
import { useTranslation } from "react-i18next";
import { CardLabel, Dropdown, LabelFieldPair, TextInput, DatePicker } from "@egovernments/digit-ui-react-components";
import { Controller, useForm } from "react-hook-form";

const InspectionDate = ({label, name, control, setData, data}) => {
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
                  <DatePicker 
                  date={props.value} 
                  onChange={(e) => {
                    props.onChange(e.target.value);
                    const newData = {
                      ...data,
                      [name]: e.target.value
                    };
                    setData(newData);
                  }} 
                  onBlur={props.onBlur} 
                  placeholder={label} />
                </div>
            )}
            />
        </LabelFieldPair>
    </div>
  );
};

export default InspectionDate;
