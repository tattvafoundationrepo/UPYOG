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
import HealthStatDropdownField from "../commonFormFields/healthStatDropdown";
import WeightField from "../commonFormFields/weightField";
import ReceiverNumberField from "../commonFormFields/receiverName";
import ReceiverNameField from "../commonFormFields/receiverName";
import ReceiverField from "../commonFormFields/receiverName";
import NumberOfAnimalsField from "../commonFormFields/numberOfAnimals";

const GatePass = () => {
  const { t } = useTranslation();
  const [data, setData] = useState({});
  const [subFormType, setSubFormType] = useState(null);

  const {
    control,
    setValue,
    handleSubmit,
    getValues,
    formState: { errors, isValid },
  } = useForm({ defaultValues: {}, mode: "onChange" });

  const fetchDataByReferenceNumber = async (referenceNumber) => {
    const mockData = {
      arrivalUuid: referenceNumber,
      importType: "Type A",
      importPermissionNumber: "123456",
      importPermissionDate: new Date(),
      traderName: "John Doe",
      licenseNumber: "LIC123",
      vehicleNumber: "ABC123",
      numberOfAliveAnimals: 5,
      numberOfDeadAnimals: 2,
      arrivalDate: new Date(),
      arrivalTime: "12:00",
    };
    return mockData;
  };

  const handleSearch = async () => {
    const referenceNumber = getValues("arrivalUuid");
    if (referenceNumber) {
      try {
        const result = await fetchDataByReferenceNumber(referenceNumber);
        setData(result);
        Object.keys(result).forEach((key) => {
          setValue(key, result[key]);
        });
      } catch (error) {
        console.error("Failed to fetch data", error);
      }
    }
  };

  const onSubmit = (formData) => {
    console.log("Form data submitted:", formData);
    const jsonData = JSON.stringify(formData);
    console.log("Generated JSON:", jsonData);
  };

  return (
    <React.Fragment>
      <div className="bmc-card-full">
        <form onSubmit={handleSubmit(onSubmit)}>
          <MainFormHeader title={"DEONAR_GATE_PASS"} />
          <div className="bmc-row-card-header">
            <div className="bmc-card-row">
                <ShopkeeperNameField />
                <HelkariNameField />
                <SearchButtonField />
            </div>
          </div>
          <div className="bmc-row-card-header">
            <div className="bmc-card-row">
                <ShopkeeperNameField />
                <HelkariNameField />
                <VehicleTypeDropdownField control={control} setData={setData} data={data} />
                <VehicleNumberField control={control} setData={setData} data={data} />
                <ReceiverField name="receiverNameField" label="DEONAR_RECEIVER_NAME" />
                <ReceiverField name="receiverContact" label="DEONAR_RECEIVER_CONTACT" />
                <NumberOfAnimalsField />
                <HealthStatDropdownField name="typeOfMeat" label="DEONAR_TYPE_OF_MEAT" />
                <WeightField name="weight" label="DEONAR_WEIGHT_IN_KG" />
                <ReferenceNumberField />
                <SubmitPrintButtonFields />
            </div>
          </div>
        </form>
      </div>
    </React.Fragment>
  );
};

export default GatePass;
