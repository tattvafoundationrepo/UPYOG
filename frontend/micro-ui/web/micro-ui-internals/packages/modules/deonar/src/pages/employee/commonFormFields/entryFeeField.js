import React, { useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { CardLabel, LabelFieldPair, TextInput } from "@upyog/digit-ui-react-components";
import { Controller } from "react-hook-form";
import useDeonarCommon from "../../../../../../libraries/src/hooks/deonar/useCommonDeonar";

const EntryFeeField = ({ control, setData, data, style }) => {
  const { t } = useTranslation();
  const [error, setError] = useState("");
  const [entryFee, setEntryFee] = useState("");

  const arrivalUUID = "ARR1001";

  const { fetchEntryFeeDetailsbyUUID } = useDeonarCommon();
  const { data: entryFeeData, isLoading } = fetchEntryFeeDetailsbyUUID({ ArrivalUUID: arrivalUUID });

  useEffect(() => {
    if (entryFeeData) {
      const animalDetails = entryFeeData.SecurityCheckDetails[0]?.animalDetails || [];
      const totalAnimalCount = animalDetails.reduce((total, animal) => total + (animal.count || 0), 0);
      const calculatedEntryFee = totalAnimalCount * 100;
      setEntryFee(calculatedEntryFee);
      control.setValue("entryFee", calculatedEntryFee);
      setData({ ...data, entryFee: calculatedEntryFee });
    }
  }, []);

  const validateEntryFee = (value) => {
    if (value <= 0) {
      return t("ENTRY_FEE_MUST_BE_GREATER_THAN_ZERO"); // Custom error message for value <= 0
    }
    return true;
  };

  return (
    <div className="bmc-col3-card" style={style}>
      <LabelFieldPair>
        <CardLabel className="bmc-label">{t("DEONAR_ENTRY_FEE")}</CardLabel>
        <Controller
          control={control}
          name="entryFee"
          rules={{
            required: t("CORE_COMMON_REQUIRED_ERRMSG"),
            validate: validateEntryFee, 
          }}
          render={({ value, onChange, onBlur, name, ref, fieldState }) => (
            <div>
              <TextInput type="number" value={entryFee || value} readOnly onBlur={onBlur} placeholder={t("DEONAR_ENTRY_FEE")} />
              {error && <div style={{ color: "red" }}>{error}</div>}
            </div>
          )}
        />
      </LabelFieldPair>
    </div>
  );
};

export default EntryFeeField;
