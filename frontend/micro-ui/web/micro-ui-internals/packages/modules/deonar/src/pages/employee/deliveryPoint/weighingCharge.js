import React, { useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { Controller, useForm } from "react-hook-form";
import SearchButtonField from "../commonFormFields/searchBtn";
import MainFormHeader from "../commonFormFields/formMainHeading";
import ShopkeeperNameField from "../commonFormFields/shopkeeperName";
import HelkariNameField from "../commonFormFields/helkariName";
import PaymentModeField from "../commonFormFields/paymentMode";
import ReferenceNumberField from "../commonFormFields/referenceNumber";
import SubmitPrintButtonFields from "../commonFormFields/submitPrintBtn";
import HealthStatDropdownField from "../commonFormFields/healthStatDropdown";
import WeightField from "../commonFormFields/weightField";
import { typeOfAnimal, weighingChargeMockData } from "../../../constants/dummyData";
import { COLLECTION_POINT_ENDPOINT } from "../../../constants/apiEndpoints";
import useSubmitForm from "../../../hooks/useSubmitForm";

const WeighingCharge = () => {
  const { t } = useTranslation();
  const [data, setData] = useState({});
  const [animalTypeOptions, setAnimalTypeOptions] = useState([]);
  const { submitForm, isSubmitting, response, error } = useSubmitForm(COLLECTION_POINT_ENDPOINT);

  const {
    control,
    setValue,
    handleSubmit,
    getValues,
    formState: { errors, isValid },
  } = useForm({ defaultValues: {
    typeOfAnimal: {},
    carcassWeight: 0,
    kenaWeight: 0,
    paymentMode: {},
    referenceNumber: 0
  }, mode: "onChange" });

  useEffect(() => {
    setAnimalTypeOptions(typeOfAnimal);
  }, []);

  const fetchDataByReferenceNumber = async () => {
    return weighingChargeMockData;
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
          <MainFormHeader title={"DEONAR_WEIGHING_CHARGE"} />
          <div className="bmc-row-card-header">
            <div className="bmc-card-row">
                <ShopkeeperNameField control={control} data={data} setData={setData} />
                <HelkariNameField control={control} data={data} setData={setData} />
                <SearchButtonField onSearch={handleSearch} />
            </div>
          </div>
          <div className="bmc-row-card-header">
            <div className="bmc-card-row">
                <HealthStatDropdownField name="typeOfAnimal" label="DEONAR_TYPE_OF_ANIMAL" control={control} data={data} setData={setData} options={animalTypeOptions} />
                <WeightField name="carcassWeight" label="DEONAR_CARCASS_WEIGHT" control={control} data={data} setData={setData} />
                <WeightField name="kenaWeight" label="DEONAR_KENA_WEIGHT" control={control} data={data} setData={setData} />
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

export default WeighingCharge;
