import React, { useState, useEffect } from "react";
import { useTranslation } from "react-i18next";
import { CardLabel, LabelFieldPair, TextInput } from "@upyog/digit-ui-react-components";
import { Controller } from "react-hook-form";

const PenaltyChargeAmountField = ({ control, data, setData, amount }) => {
  const { t } = useTranslation();
  const [error, setError] = useState("");

  useEffect(() => {
    if (amount) {
      setError("");
    }
  }, [amount]);

  return (
    <div className="bmc-col3-card">
      <LabelFieldPair>
        <CardLabel className="bmc-label">{t("DEONAR_PENALTY_CHARGE_AMOUNT")}</CardLabel>
        <Controller
          control={control}
          name="penaltyChargeAmount"
          render={(props) => (
            <div>
              <TextInput
                type="number"
                value={props.value || amount || ""}
                onChange={(e) => {
                  const value = e.target.value;
                  props.onChange(value);
                  const newData = {
                    ...data,
                    penaltyChargeAmount: value,
                  };
                  setData(newData);
                }}
                onBlur={props.onBlur}
                optionKey="i18nKey"
                t={t}
                placeholder={t("DEONAR_PENALTY_CHARGE_AMOUNT")}
              />
              {error && <div style={{ color: "red" }}>{error}</div>}
            </div>
          )}
        />
      </LabelFieldPair>
    </div>
  );
};

export default PenaltyChargeAmountField;
