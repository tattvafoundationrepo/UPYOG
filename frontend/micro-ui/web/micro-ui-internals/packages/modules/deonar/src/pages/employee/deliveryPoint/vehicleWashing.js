import React, { useState } from "react";
import { useTranslation } from "react-i18next";
import { Controller, useForm } from "react-hook-form";
import SearchButtonField from "../commonFormFields/searchBtn";
import MainFormHeader from "../commonFormFields/formMainHeading";
import ShopkeeperNameField from "../commonFormFields/shopkeeperName";
import HelkariNameField from "../commonFormFields/helkariName";
import VehicleTypeDropdownField from "../commonFormFields/vehicleTypeDropdown";
import VehicleNumberField from "../commonFormFields/vehicleNumber";
import WashingChargeAmountField from "../commonFormFields/washingChargeAmt";
import PaymentModeField from "../commonFormFields/paymentMode";
import ReferenceNumberField from "../commonFormFields/referenceNumber";
import SubmitPrintButtonFields from "../commonFormFields/submitPrintBtn";
import useSubmitForm from "../../../hooks/useSubmitForm";
import { COLLECTION_POINT_ENDPOINT } from "../../../constants/apiEndpoints";
import { vehicleWashingMockData } from "../../../constants/dummyData";

const VehicleWashing = () => {
  const { t } = useTranslation();
  const [data, setData] = useState({});

  const {
    control,
    setValue,
    handleSubmit,
    getValues,
    formState: { errors, isValid },
  } = useForm({ defaultValues: {
    vehicleType: {},
    vehicleNumber: 'MH-00 0000',
    washingChargeAmount: 0,
    paymentMode: {},
    referenceNumber: 0
  }, mode: "onChange" });

  const { submitForm, isSubmitting, response, error } = useSubmitForm(COLLECTION_POINT_ENDPOINT);

  const fetchDataByReferenceNumber = async () => {
    return vehicleWashingMockData;
  };

  const handleSearch = async () => {
    const shopkeeperName = getValues("shopkeeperName");
    const helkariName = getValues("helkariName");
    if (shopkeeperName && helkariName) {
      try {
        const result = await fetchDataByReferenceNumber();
        setData(result);
        console.log(result);
        Object.keys(result).forEach((key) => {
          setValue(key, result[key]);
        });
        setValue('shopkeeperName', shopkeeperName);
        setValue('helkariName', helkariName);
      } catch (error) {
        console.error("Failed to fetch data", error);
      }
    }
  };

  const onSubmit = async (formData) => {
    try {
      const result = await submitForm(formData);
      console.log("Form successfully submitted:", result);
      alert("Form submission successful !");
    } catch (error) {
      console.error("Error submitting form:", error);
      alert("Form submission failed !");
    }
  };

  return (
    <React.Fragment>
      <div className="bmc-card-full">
        <form onSubmit={handleSubmit(onSubmit)}>
          <MainFormHeader title={"DEONAR_VEHICLE_WASHING_CHARGE"} />
          <div className="bmc-row-card-header">
            <div className="bmc-card-row">
                <ShopkeeperNameField control={control} data={data} setData={setData} />
                <HelkariNameField control={control} data={data} setData={setData} />
                <SearchButtonField onSearch={handleSearch} />
            </div>
          </div>
          <div className="bmc-row-card-header">
            <div className="bmc-card-row">
                <VehicleTypeDropdownField control={control} setData={setData} data={data} />
                <VehicleNumberField control={control} setData={setData} data={data} />
                <WashingChargeAmountField control={control} data={data} setData={setData} />
                <PaymentModeField control={control} data={data} setData={setData} />
                <ReferenceNumberField control={control} data={data} setData={setData} />
                <SubmitPrintButtonFields />
            </div>
          </div>
        </form>
      </div>
    </React.Fragment>
  );
};

export default VehicleWashing;
