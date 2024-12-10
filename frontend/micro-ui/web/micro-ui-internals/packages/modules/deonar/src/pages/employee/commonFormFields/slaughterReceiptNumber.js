import React, { useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { CardLabel, Dropdown, LabelFieldPair, TextInput, DatePicker } from "@upyog/digit-ui-react-components";
import { Controller, useForm } from "react-hook-form";

const SlaughterReceiptNumberField = ({ setData, data, control }) => {
  const { t } = useTranslation();
  const [error, setError] = useState("");

  useEffect(() => {
    if (!data.slaughterReceiptNumber) {
      setError("*");
    } else {
      setError("");
    }
  }, [data]);

  return (
    <div className="bmc-col3-card">
      <LabelFieldPair>
        <CardLabel className="bmc-label">
          {t("DEONAR_SLAUGHTER_RECEIPT_NUMBER")}&nbsp;{error && <sup style={{ color: "red", fontSize: "x-small" }}>{error}</sup>}
        </CardLabel>
        <Controller
          control={control}
          name="slaughterReceiptNumber"
          rules={{ required: t("CORE_COMMON_REQUIRED_ERRMSG") }}
          render={(props) => (
            <div>
              <TextInput
                value={props.value}
                onChange={(e) => {
                  props.onChange(e.target.value);
                  const newData = {
                    ...data,
                    slaughterReceiptNumber: e.target.value,
                  };
                  setData(newData);
                }}
                onBlur={props.onBlur}
                optionKey="i18nKey"
                t={t}
                placeholder={t("DEONAR_SLAUGHTER_RECEIPT_NUMBER")}
              />
            </div>
          )}
        />
      </LabelFieldPair>
    </div>
  );
};

export default SlaughterReceiptNumberField;
