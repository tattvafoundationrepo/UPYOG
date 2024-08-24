import React, { useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { Controller, useForm } from "react-hook-form";
import ImportPermissionNumberField from "../commonFormFields/importPermissionNumber";
import LicenseNumberField from "../commonFormFields/licenseNumber";
import SearchButtonField from "../commonFormFields/searchBtn";
import MainFormHeader from "../commonFormFields/formMainHeading";
import VeterinaryOfficerField from "../commonFormFields/veterinaryOfficer";
import ArrivalUuidField from "../commonFormFields/arrivalUuid";
import TraderNameField from "../commonFormFields/traderName";
import NumberOfAliveAnimalsField from "../commonFormFields/numberOfAliveAnimals";
import AnimalTokenNumberField from "../commonFormFields/animalTokenNumber";
import PregnancyField from "../commonFormFields/pregnancyField";
import ApproximateAgeField from "../commonFormFields/approxAge";
import HealthStatDropdownField from "../commonFormFields/healthStatDropdown";
import OtherField from "../commonFormFields/other";
import RemarkField from "../commonFormFields/remark";
import SubmitAddButtonFields from "../commonFormFields/submitAddBtn";
import InspectionDate from "../commonFormFields/inspectionDate";
import InspectionDayField from "../commonFormFields/inspectionDay";
import { anteMortemInspectionMockData, bodyColor, breed, daysOfWeek, species } from "../../../constants/dummyData";
import useCurrentUser from "../../../hooks/useCurrentUser";
import useSubmitForm from "../../../hooks/useSubmitForm";
import { COLLECTION_POINT_ENDPOINT } from "../../../constants/apiEndpoints";

const AnteMortemInspectionPage = () => {
  const { t } = useTranslation();
  const [data, setData] = useState({});
  const userName = useCurrentUser();
  const [arrivalUuidVal, setArrivalUuidVal] = useState("");
  const [speciesOptions, setSpeciesOptions] = useState([]);
  const [breedOptions, setBreedOptions] = useState([]);
  const [sexOptions, setSexOptions] = useState([]);
  const [bodyColorOptions, setBodyColorOptions] = useState([]);

  const {
    control,
    setValue,
    handleSubmit,
    getValues,
    formState: { errors, isValid },
  } = useForm({ defaultValues: {
    importPermissionNumber: "",
    licenseNumber: "",
    veterinaryOfficer: "",
    anteMortemInspectionDate: new Date().toISOString().split('T')[0],
    anteMortemInspectionDay: daysOfWeek[new Date().getDay()],
    arrivalUuid: "",
    traderName: {},
    licenseNumber: "",
    numberOfAliveAnimals: 0,
    animalTokenNumber: 0,
    species: {},
    breed: {},
    sex: {},
    bodyColor: {},
    pregnancy: "",
    approximateAge: 0,
    gait: {},
    posture: {},
    bodyTemperature: {},
    pulseRate: {},
    appetite: {},
    eyes: {},
    nostrils: {},
    muzzle: {},
    opinion: {},
    animalStabling: {},
    other: "",
    remark: ""
  }, mode: "onChange" });

  useEffect(() => {
    setBreedOptions(breed);
    setSpeciesOptions(species);
    setBodyColorOptions(bodyColor);
  }, []);

  const { submitForm, isSubmitting, response, error } = useSubmitForm(COLLECTION_POINT_ENDPOINT);

  const fetchDataByReferenceNumber = async () => {
    return anteMortemInspectionMockData;
  };

  const handleSearch = async () => {
    const importPermissionNumber = getValues("importPermissionNumber");
    const licenseNumber = getValues("licenseNumber");
    if (importPermissionNumber && licenseNumber) {
      try {
        const result = await fetchDataByReferenceNumber();
        setData(result);
        console.log(result);
        Object.keys(result).forEach((key) => {
          setValue(key, result[key]);
          if (key === "arrivalUuid") {
            setArrivalUuidVal(result[key]);
          }
        });
        setValue('importPermissionNumber', importPermissionNumber);
        setValue('licenseNumber', licenseNumber);
      } catch (error) {
        console.error("Failed to fetch data", error);
      }
    }
  };

  const onSubmit = async (formData) => {
    try {
      const result = await submitForm(formData);
      console.log("Form successfully submitted:", result);
      alert("Form submission successful ! Your ArrivalUUID is: " + formData.arrivalUuid);
    } catch (error) {
      console.error("Error submitting form:", error);
      alert("Form submission failed !");
    }
  };

  return (
    <React.Fragment>
      <div className="bmc-card-full">
        <form onSubmit={handleSubmit(onSubmit)}>
          <MainFormHeader title={"ANTE_MORTEM_INSPECTION"} />
          <div className="bmc-row-card-header">
            <div className="bmc-card-row">
              <ImportPermissionNumberField control={control} setData={setData} data={data} />
              <LicenseNumberField control={control} setData={setData} data={data} />
              <SearchButtonField onSearch={handleSearch} />
            </div>
          </div>
          <div className="bmc-row-card-header">
            <div className="bmc-card-row">
              <VeterinaryOfficerField control={control} setData={setData} data={data} userName={userName} />
              <InspectionDate label="DEONAR_ANTE_MORTEM_INSPECTION_DATE" name="anteMortemInspectionDate" control={control} setData={setData} data={data} />
              <InspectionDayField label="DEONAR_ANTE_MORTEM_INSPECTION_DAY" name="anteMortemInspectionDay" control={control} setData={setData} data={data} />
              <ArrivalUuidField control={control} setData={setData} data={data} uuid={arrivalUuidVal} disabled={true} />
              <TraderNameField control={control} setData={setData} data={data} />
              <NumberOfAliveAnimalsField control={control} setData={setData} data={data} />
              <AnimalTokenNumberField control={control} setData={setData} data={data} />
              <HealthStatDropdownField name="species" label="DEONAR_SPECIES" control={control} setData={setData} data={data} options={speciesOptions} />
              <HealthStatDropdownField name="breed" label="DEONAR_BREED" control={control} setData={setData} data={data} options={breedOptions} />
              <HealthStatDropdownField name="sex" label="DEONAR_SEX" control={control} setData={setData} data={data} options={sexOptions} />
              <HealthStatDropdownField name="bodyColor" label="DEONAR_BODY_COLOR" control={control} setData={setData} data={data} options={bodyColorOptions} />
              <PregnancyField control={control} setData={setData} data={data} />
              <ApproximateAgeField control={control} setData={setData} data={data} />
              <HealthStatDropdownField name="gait" label="DEONAR_GAIT" control={control} setData={setData} data={data} />
              <HealthStatDropdownField name="posture" label="DEONAR_POSTURE" control={control} setData={setData} data={data} />
              <HealthStatDropdownField name="bodyTemperature" label="DEONAR_BODY_TEMPERATURE" control={control} setData={setData} data={data} />
              <HealthStatDropdownField name="pulseRate" label="DEONAR_PULSE_RATE" control={control} setData={setData} data={data} />
              <HealthStatDropdownField name="appetite" label="DEONAR_APPETITE" control={control} setData={setData} data={data} />
              <HealthStatDropdownField name="eyes" label="DEONAR_EYES" control={control} setData={setData} data={data} />
              <HealthStatDropdownField name="nostrils" label="DEONAR_NOSTRILS" control={control} setData={setData} data={data} />
              <HealthStatDropdownField name="muzzle" label="DEONAR_MUZZLE" control={control} setData={setData} data={data} />
              <HealthStatDropdownField name="opinion" label="DEONAR_OPINION" required={true} control={control} setData={setData} data={data} />
              <HealthStatDropdownField name="animalStabling" label="DEONAR_ANIMAL_STABLING" required={true} control={control} setData={setData} data={data} />
              <OtherField control={control} setData={setData} data={data} />
              <RemarkField control={control} setData={setData} data={data} />
              <SubmitAddButtonFields control={control} setData={setData} data={data} />
            </div>
          </div>
        </form>
      </div>
      <div className="bmc-row-card-header">
        <div className="bmc-card-row">
          <div className="bmc-table-container" style={{ padding: "1rem" }}>
              <table className="bmc-hover-table">
                <thead>
                  <tr>
                    <th scope="col">Col1</th>
                    <th scope="col">Col2</th>
                    <th scope="col">Col3</th>
                  </tr>
                </thead>
                <tbody>
                  <tr>
                  </tr>
                </tbody>
              </table>
          </div>
        </div>
      </div>
    </React.Fragment>
  );
};

export default AnteMortemInspectionPage;
