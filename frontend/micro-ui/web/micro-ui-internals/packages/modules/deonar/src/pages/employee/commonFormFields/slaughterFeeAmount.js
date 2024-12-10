import React, { useState } from "react";
import { useTranslation } from "react-i18next";
import { CardLabel, Dropdown, LabelFieldPair, TextInput, DatePicker } from "@upyog/digit-ui-react-components";
import { Controller, useForm } from "react-hook-form";

const SlaughterFeeAmountField = ({control, setData, data}) => {
  const { t } = useTranslation();

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
                    onBlur={onBlur}
                    optionKey="i18nKey"
                    t={t}
                    placeholder={t("DEONAR_SLAUGHTER_FEE_AMOUNT")}
                    disabled={true}
                    />
                </div>
                )}
            />
        </LabelFieldPair>
    </div>
  );
};

export default SlaughterFeeAmountField;
