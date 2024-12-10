import React, { useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { CardLabel, Dropdown, LabelFieldPair, TextInput, DatePicker } from "@upyog/digit-ui-react-components";
import { Controller, useForm } from "react-hook-form";

const VeterinaryOfficerField = ({control, data, setData, userName}) => {
  const { t } = useTranslation();

  return (
    <div className="bmc-col3-card">
        <LabelFieldPair>
            <CardLabel className="bmc-label">{t("VETERINARY_OFFICER_NAME")}</CardLabel>
            <Controller
                control={control}
                name="veterinaryOfficerName"
                rules={{ required: t("CORE_COMMON_REQUIRED_ERRMSG") }}
                render={(props) => (
                    <div>
                      <TextInput
                      value={userName}
                      onBlur={props.onBlur}
                      optionKey="i18nKey"
                      t={t}
                      placeholder={t("VETERINARY_OFFICER_NAME")}
                      disabled={true}
                      />
                    </div>
                )}
            />
        </LabelFieldPair>
    </div>
  );
};

export default VeterinaryOfficerField;
