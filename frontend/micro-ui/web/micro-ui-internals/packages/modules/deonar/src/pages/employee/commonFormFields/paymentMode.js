import React, { useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { CardLabel, Dropdown, LabelFieldPair } from "@upyog/digit-ui-react-components";
import { Controller } from "react-hook-form";
import { paymentModeOptions } from "../../../constants/dummyData";

const PaymentModeField = ({ control, data, setData, disabled, style }) => {
  const { t } = useTranslation();
  const [error, setError] = useState("");
  const [options, setOptions] = useState([]);

  useEffect(() => {
    if (!data.paymentMode) {
      setError(t("CORE_COMMON_REQUIRED_ERRMSG"));
    } else {
      setError("");
    }
  }, [data, t]);

  useEffect(() => {
    setOptions(paymentModeOptions);
  }, []);

  useEffect(() => {
    if (disabled) {
      setError("");
    }
  }, [disabled]);

  return (
    <div className="bmc-col3-card" style={style}>
      <LabelFieldPair>
        <CardLabel className="bmc-label">{t("DEONAR_PAYMENT_MODE")}&nbsp;{error && <sup style={{ color: "red", fontSize: "x-small" }}>{error}</sup>}</CardLabel>
        <Controller
          control={control}
          name="paymentMode"
          rules={{ required: t("CORE_COMMON_REQUIRED_ERRMSG") }}
          render={(props) => (
            <Dropdown
              name="paymentMode"
              selected={props.value}
              select={(value) => {
                props.onChange(value);
                const newData = {
                  ...data,
                  paymentMode: value,
                };
                setData(newData);
              }}
              onBlur={props.onBlur}
              optionKey="name"
              option={options}
              t={t}
              placeholder={t("DEONAR_PAYMENT_MODE")}
              disabled={disabled}
            />
          )}
        />
      </LabelFieldPair>
    </div>
  );
};

export default PaymentModeField;
