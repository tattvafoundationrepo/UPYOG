import React, { useState } from "react";
import { useTranslation } from "react-i18next";
import { CardLabel, Dropdown, LabelFieldPair, TextInput, DatePicker } from "@upyog/digit-ui-react-components";
import { Controller, useForm } from "react-hook-form";

const SlaughterFeeAmountField = () => {
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
            <CardLabel className="bmc-label">{t("DEONAR_SLAUGHTER_FEE_AMOUNT")}</CardLabel>
            <Controller
                control={control}
                name="slaughterFeeAmount"
                rules={{ required: t("CORE_COMMON_REQUIRED_ERRMSG") }}
                render={({ value, onChange, onBlur }) => (
                <div>
                    <TextInput
                    value={value}
                    onChange={(e) => onChange(e.target.value)}
                    onBlur={onBlur}
                    optionKey="i18nKey"
                    t={t}
                    placeholder={t("DEONAR_SLAUGHTER_FEE_AMOUNT")}
                    />
                </div>
                )}
            />
        </LabelFieldPair>
    </div>
  );
};

export default SlaughterFeeAmountField;
