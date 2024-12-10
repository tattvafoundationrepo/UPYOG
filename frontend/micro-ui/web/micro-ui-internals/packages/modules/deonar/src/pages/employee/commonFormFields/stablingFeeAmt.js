import React, { useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { CardLabel, Dropdown, LabelFieldPair, TextInput, DatePicker } from "@upyog/digit-ui-react-components";
import { Controller, useForm } from "react-hook-form";

const StablingFeeAmountField = ({control, setData, data, style}) => {
  const { t } = useTranslation();
  const [error, setError] = useState("");

  useEffect(() => {
    if (!data.stablingFeeAmount) {
      setError(t("REQUIRED_FIELD"));
    }
    else {
      setError("");
    }
  }, [data]);

  return (
    <div className="bmc-col3-card" style={style}>
        <LabelFieldPair>
            <CardLabel className="bmc-label">{t("DEONAR_STABLING_FEE_AMOUNT")}</CardLabel>
            <Controller
                control={control}
                name="stablingFeeAmount"
                rules={{ required: t("CORE_COMMON_REQUIRED_ERRMSG") }}
                render={(props) => (
                <div>
                    <TextInput
                    value={props.value}
                    onChange={(e) => {
                      props.onChange(e.target.value);
                      const newData = {
                        ...data,
                        stablingFeeAmount: e.target.value
                      };
                      setData(newData);
                    }}
                    onBlur={props.onBlur}
                    optionKey="i18nKey"
                    t={t}
                    placeholder={t("DEONAR_STABLING_FEE_AMOUNT")}
                    />
                </div>
                )}
            />
        </LabelFieldPair>
        {error && <div style={{ color: "red" }}>{error}</div>}
      </div>
  );
};

export default StablingFeeAmountField;
