import React, { useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { Controller, useForm } from "react-hook-form";
import MainFormHeader from "../commonFormFields/formMainHeading";
import HealthStatDropdownField from "../commonFormFields/healthStatDropdown";
import PenaltyChargeAmountField from "../commonFormFields/penaltyChargeAmt";
import { CardLabel, LabelFieldPair, TextInput, Toast } from "@upyog/digit-ui-react-components";
import SubmitButtonField from "../commonFormFields/submitBtn";

const PenaltyCharge = () => {
  const { t } = useTranslation();
  const [data, setData] = useState({});
  const [penaltyOptions, setPenaltyOptions] = useState([]);
  const [penaltyAmount, setPenaltyAmount] = useState();
  const [feeAmount, setFeeAmount] = useState();
  const [error, setError] = useState(null);
  const [showPenaltyPerUnit, setShowPenaltyPerUnit] = useState(false);
  const [toast, setToast] = useState(null);

  const {
    control,
    setValue,
    handleSubmit,
    getValues,
    reset,
    formState: { errors, isValid },
  } = useForm();

  const { data: penaltyData, isLoading, isError } = Digit.Hooks.deonar.useGetPenalty();

  useEffect(() => {
    if (penaltyData && penaltyData.PenaltyTypeDetails) {
      const penaltyOptions = penaltyData.PenaltyTypeDetails.map((item) => ({
        name: item.penaltyType,
        value: item.id,
        feeAmount: item.feeAmount,
        penaltyPerUnit: item.perUnit,
      }));
      setPenaltyOptions(penaltyOptions);
    } else if (isError) {
      setError(error);
    }
  }, [penaltyData, isError, error]);

  const savePenaltiesData = Digit.Hooks.deonar.useSavePenalty();

  useEffect(() => {
    if (getValues("typeOfPenalty")) {
      const selectedPenalty = penaltyOptions.find((option) => option.value === getValues("typeOfPenalty").value);
      if (selectedPenalty) {
        setPenaltyAmount(selectedPenalty.feeAmount);
        setValue("penaltyChargeAmount", selectedPenalty.feeAmount);
        setFeeAmount(selectedPenalty.feeAmount);
        setShowPenaltyPerUnit(selectedPenalty.penaltyPerUnit);
      }
    }
  }, [getValues("typeOfPenalty"), penaltyOptions, setValue]);

  useEffect(() => {
    const penaltyPerUnit = getValues("penaltyPerUnit");
    if (penaltyPerUnit && penaltyAmount) {
      const updatedFeeAmount = penaltyAmount * penaltyPerUnit;
      setFeeAmount(updatedFeeAmount);
      setValue("penaltyChargeAmount", updatedFeeAmount);
    }
  }, [getValues("penaltyPerUnit"), penaltyAmount, setValue]);

  const showToast = (type, message, duration = 5000) => {
    setToast({ key: type, action: message });
    setTimeout(() => {
      setToast(null);
    }, duration);
  };

  const onSubmit = async (formData) => {
    const payload = {
      penaltyId: formData.typeOfPenalty.value,
      amount: formData.penaltyChargeAmount,
      units: formData.penaltyPerUnit,
      stakeholderId: formData.licenseNumber,
    };

    savePenaltiesData.mutate(payload, {
      onSuccess: () => {
        reset({
          typeOfPenalty: "",
          penaltyChargeAmount: "",
          penaltyPerUnit: "",
          licenseNumber: "",
        });
        setPenaltyAmount("");
        setFeeAmount("");
        showToast("success", t("DEONAR_PENALTY_CHARGE_DATA_SUBMITTED_SUCCESSFULY"));
      },
      onError: (error) => {
        console.error("Failed to fetch data", error);
        showToast("success", t("DEONAR_PENALTY_CHARGE_DATA_NOT_SUBMITTED_SUCCESSFULY"));
      },
    });
  };

  return (
    <React.Fragment>
      <div className="bmc-card-full">
        <form onSubmit={handleSubmit(onSubmit)}>
          <MainFormHeader title={t("DEONAR_PENALTY_CHARGE")} />
          <div className="bmc-row-card-header">
            <div className="bmc-card-row">
              <HealthStatDropdownField
                name="typeOfPenalty"
                label={t("DEONAR_TYPE_OF_PENALTY")}
                control={control}
                data={data}
                setData={setData}
                options={penaltyOptions}
              />
              {showPenaltyPerUnit && (
                <div className="bmc-col3-card">
                  <LabelFieldPair>
                    <CardLabel className="bmc-label">{t("DEONAR_PENALTY_PERUNIT")}</CardLabel>
                    <Controller
                      control={control}
                      name="penaltyPerUnit"
                      rules={{ required: t("CORE_COMMON_REQUIRED_ERRMSG") }}
                      render={(props) => (
                        <div>
                          <TextInput
                            value={props.value}
                            onChange={(e) => {
                              props.onChange(e.target.value);
                              const newData = { ...data, penaltyPerUnit: e.target.value };
                              setData(newData);
                            }}
                            onBlur={props.onBlur}
                            optionKey="i18nKey"
                            t={t}
                            placeholder={t("DEONAR_PENALTY_PERUNIT")}
                          />
                        </div>
                      )}
                    />
                  </LabelFieldPair>
                </div>
              )}
              <PenaltyChargeAmountField control={control} data={data} setData={setData} amount={feeAmount} />

              <div className="bmc-col3-card">
                <LabelFieldPair>
                  <CardLabel className="bmc-label">{t("DEONAR_LICENSE_NUMBER")}</CardLabel>
                  <Controller
                    control={control}
                    name="licenseNumber"
                    rules={{ required: t("CORE_COMMON_REQUIRED_ERRMSG") }}
                    render={(props) => (
                      <div>
                        <TextInput
                          value={props.value}
                          onChange={(e) => {
                            props.onChange(e.target.value);
                          }}
                          onBlur={props.onBlur}
                          optionKey="i18nKey"
                          t={t}
                          placeholder={t("DEONAR_LICENSE_NUMBER")}
                        />
                      </div>
                    )}
                  />
                </LabelFieldPair>
              </div>

              <SubmitButtonField control={control} />
            </div>
          </div>
        </form>
      </div>
      {toast && (
        <Toast
          error={toast.key === t("error")}
          label={t(toast.key === t("success") ? t("DEONAR_PENALTY_CHARGE_DATA_SUBMITTED_SUCCESSFULY") : toast.action)}
          onClose={() => setToast(null)}
          style={{ maxWidth: "670px" }}
        />
      )}
    </React.Fragment>
  );
};

export default PenaltyCharge;
