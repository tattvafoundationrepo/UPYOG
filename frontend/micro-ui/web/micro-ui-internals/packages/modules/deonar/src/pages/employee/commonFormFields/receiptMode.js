import React, { useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { CardLabel, Dropdown, LabelFieldPair, TextInput, DatePicker } from "@upyog/digit-ui-react-components";
import { Controller, useForm } from "react-hook-form";
import { paymentModeOptions } from "../../../constants/dummyData";

const ReceiptModeField = ({ control, setData, data, style }) => {
  const { t } = useTranslation();
  const [options, setOptions] = useState([]);
  const [error, setError] = useState("");

  useEffect(() => {
    setOptions(paymentModeOptions);
  }, []);

  useEffect(() => {
    if (!data.receiptMode) {
      setError(t("CORE_COMMON_REQUIRED_ERRMSG"));
    } else {
      setError("");
    }
  }, [data]);

  return (
    <div className="bmc-col3-card" style={style}>
      <LabelFieldPair>
        <CardLabel className="bmc-label">
          {t("DEONAR_RECEIPT_MODE")} &nbsp;{error && <sup style={{ color: "red", fontSize: "x-small" }}>{error}</sup>}
        </CardLabel>
        <Controller
          control={control}
          name="receiptMode"
          rules={{ required: t("CORE_COMMON_REQUIRED_ERRMSG") }}
          render={(props) => (
            <div>
              <Dropdown
                name="receiptMode"
                selected={props.value}
                select={(value) => {
                  props.onChange(value);
                  const newData = {
                    ...data,
                    receiptMode: value,
                  };
                  setData(newData);
                }}
                onBlur={props.onBlur}
                optionKey="name"
                option={options}
                t={t}
                placeholder={t("DEONAR_RECEIPT_MODE")}
              />
            </div>
          )}
        />
      </LabelFieldPair>
    </div>
  );
};

export default ReceiptModeField;
