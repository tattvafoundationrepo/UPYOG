import React, { useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { CardLabel, Dropdown, LabelFieldPair, TextInput, DatePicker } from "@upyog/digit-ui-react-components";
import { Controller, useForm } from "react-hook-form";

const RemovalFeeAmountField = ({control, setData, data, disabled}) => {
  const { t } = useTranslation();
  const [error, setError] = useState("");

  useEffect(() => {
    if (!data.removalFeeAmount) {
      setError("REQUIRED_FIELD");
    }
    else {
      setError("");
    }
  }, [data]);

  return (
    <div className="bmc-col3-card">
      <LabelFieldPair>
          <CardLabel className="bmc-label">{t("DEONAR_REMOVAL_FEE_AMOUNT")}</CardLabel>
          <Controller
              control={control}
              name="removalFeeAmount"
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
                        removalFeeAmount: e.target.value,
                      };
                      setData(newData);
                    }}
                    onBlur={props.onBlur}
                    optionKey="i18nKey"
                    t={t}
                    placeholder={t("DEONAR_REMOVAL_FEE_AMOUNT")}
                    disabled={disabled}
                  />
              </div>
              )}
          />
      </LabelFieldPair>
      {error && <div style={{ color: "red" }}>{error}</div>}
    </div>
  );
};

export default RemovalFeeAmountField;
