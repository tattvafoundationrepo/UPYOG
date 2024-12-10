import React, { useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { CardLabel, Dropdown, LabelFieldPair, TextInput, DatePicker } from "@upyog/digit-ui-react-components";
import { Controller, useForm } from "react-hook-form";
import { vehicleWashingAmount } from "../../../constants/dummyData";

const WashingChargeAmountField = ({control, data, setData}) => {
  const { t } = useTranslation();
  const [val, setVal] = useState(0);

  useEffect(() => {
    setVal(vehicleWashingAmount.amount);
  }, []);

  return (
    <div className="bmc-col3-card">
        <LabelFieldPair>
            <CardLabel className="bmc-label">{t("DEONAR_WASHING_CHARGE_AMOUNT")}</CardLabel>
            <Controller
                control={control}
                name="washingChargeAmount"
                rules={{ required: t("CORE_COMMON_REQUIRED_ERRMSG") }}
                render={(props) => (
                <div>
                    <TextInput
                    value={val}
                    onChange={(e) => {
                      props.onChange(e.target.value);
                      const newData = {
                        ...data,
                        washingChargeAmount: e.target.value
                      };
                      setData(newData);
                      setVal(e.target.value);
                    }}
                    onBlur={props.onBlur}
                    optionKey="i18nKey"
                    t={t}
                    placeholder={t("DEONAR_WASHING_CHARGE_AMOUNT")}
                    />
                </div>
                )}
            />
        </LabelFieldPair>
    </div>
  );
};

export default WashingChargeAmountField;
