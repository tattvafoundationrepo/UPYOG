import React, { useState } from "react";
import { useTranslation } from "react-i18next";
import { CardLabel, Dropdown, LabelFieldPair, TextInput, DatePicker } from "@upyog/digit-ui-react-components";
import { Controller, useForm } from "react-hook-form";

const RemarkField = ({control, data, setData}) => {
  const { t } = useTranslation();

  return (
    <div className="bmc-col3-card">
        <LabelFieldPair>
            <CardLabel className="bmc-label">{t("DEONAR_REMARK")}</CardLabel>
            <Controller
            control={control}
            name="remark"
            rules={{ required: t("CORE_COMMON_REQUIRED_ERRMSG") }}
            render={(props) => (
                <div>
                <TextInput
                    value={props.value}
                    onChange={(e) => {
                      props.onChange(e.target.value);
                      const newData = {
                        ...data,
                        remark: e.target.value
                      };
                      setData(newData);
                    }}
                    onBlur={props.onBlur}
                    optionKey="i18nKey"
                    t={t}
                    placeholder={t("DEONAR_REMARK")}
                />
                </div>
            )}
            />
        </LabelFieldPair>
    </div>
  );
};

export default RemarkField;
