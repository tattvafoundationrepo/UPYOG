import React, { useState, useEffect } from "react";
import { useTranslation } from "react-i18next";
import { CardLabel, LabelFieldPair, TextInput } from "@upyog/digit-ui-react-components";
import { Controller } from "react-hook-form";
import useRemovalFee from "../../../hooks/useRemovalFee";
import useStablingFee from "../../../hooks/useStablingFee";
import { slaughterFeeAmt } from "../../../constants/dummyData";

const NumberOfAnimalsField = ({ control, data, setData, disabled, setValues, source, getValues, defaultValue, style }) => {
  const { t } = useTranslation();
  const [error, setError] = useState("");
  const removalFeePerUnit = useRemovalFee();
  const stablingFee = useStablingFee();

  const handleInputChange = (e, props) => {
    const value = e.target.value.replace(/\D/g, ""); // Remove any non-digit characters

    if (value >= 0 && value < 1000) {
      props.onChange(value);

      if (setValues) {
        if (source === "removal") {
          const amount = value * removalFeePerUnit;
          const newData = {
            ...data,
            numberOfAnimals: value,
            removalFeeAmount: amount,
          };
          setValues("removalFeeAmount", amount);
          setData(newData);
        } else if (source === "slaughter") {
          const amount = value * slaughterFeeAmt.amount;
          const newData = {
            ...data,
            numberOfAnimals: value,
            slaughterFeeAmount: amount,
          };
          setValues("slaughterFeeAmount", amount);
          setData(newData);
        } else if (source === "stabling") {
          let amount = 0;
          let stablingDays = getValues("stablingDays");
          if (stablingDays != null && stablingDays > 0) {
            amount = value * stablingFee * stablingDays;
          }
          const newData = {
            ...data,
            numberOfAnimals: value,
            stablingFeeAmount: amount,
          };
          setValues("stablingFeeAmount", amount);
          setData(newData);
        }
      } else {
        const newData = {
          ...data,
          numberOfAnimals: value,
        };
        setData(newData);
      }

      setError(""); // Clear any error if the input is valid
    } else {
      setError(t("CORE_COMMON_INVALID_RANGE_ERRMSG")); // Set error if the input is out of range
    }
  };

  useEffect(() => {
    if (!data.numberOfAnimals) {
      setError(t("CORE_COMMON_REQUIRED_ERRMSG"));
    }
  }, [data]);

  return (
    <div className="bmc-col3-card" style={style}>
      <LabelFieldPair>
        <CardLabel className="bmc-label">{t("DEONAR_NUMBER_OF_ANIMALS")}&nbsp;{error && <sup style={{ color: "red", fontSize: "x-small" }}>{error}</sup>}</CardLabel>
        <Controller
          control={control}
          defaultValue= {defaultValue}
          name="numberOfAnimals"
          render={(props) => (
            <div>
              <TextInput
                type="number" // Ensure only numeric input is allowed
                value={props.value}
                onChange={(e) => handleInputChange(e, props)}
                onBlur={props.onBlur}
                optionKey="i18nKey"
                t={t}
                placeholder={t("DEONAR_NUMBER_OF_ANIMALS")}
                disabled={disabled}
              />
            </div>
          )}
        />
      </LabelFieldPair>
    </div>
  );
};

export default NumberOfAnimalsField;
