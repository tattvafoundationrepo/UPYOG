import React, { useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import HealthStatDropdownField from "../commonFormFields/healthStatDropdown";
import OtherField from "../commonFormFields/other";
import RemarkField from "../commonFormFields/remark";
import SlaughterReceiptNumberField from "../commonFormFields/slaughterReceiptNumber";
import { animalStabling, daysOfWeek } from "../../../constants/dummyData";
import { useForm } from "react-hook-form";
import CustomModal from "../commonFormFields/customModal";
import CustomTable from "../commonFormFields/customTable";
import MainFormHeader from "../commonFormFields/formMainHeading";
import { useLocation } from "react-router-dom";
import { Header,CardLabel } from "@upyog/digit-ui-react-components";
import MultiColumnDropdown from "../commonFormFields/multiColumnDropdown";

const AnteMortemPreSlaughterStatusPage = () => {
  const { t } = useTranslation();
  const location = useLocation();
  const [data, setData] = useState({});
  const { entryUnitId, inspectionData } = location.state || {};
  const [tableStored, setTableStored] = useState([]);
  const [speciesOptions, setSpeciesOptions] = useState([]);
  const [breedOptions, setBreedOptions] = useState([]);
  const [sexOptions, setSexOptions] = useState([]);
  const [bodyColorOptions, setBodyColorOptions] = useState([]);
  const [pregnancyOptions, setPregnancyOptions] = useState([]);
  const [gaitOptions, setGaitOptions] = useState([]);
  const [postureOptions, setPostureOptions] = useState([]);
  const [pulseOptions, setPulseOptions] = useState([]);
  const [bodyTempOptions, setBodyTempOptions] = useState([]);
  const [appetiteOptions, setAppetiteOptions] = useState([]);
  const [eyesOptions, setEyesOptions] = useState([]);
  const [nostrilOptions, setNostrilOptions] = useState([]);
  const [muzzleOptions, setMuzzleOptions] = useState([]);
  const [opinionOptions, setOpinionOptions] = useState([]);
  const [stablingOptions, setStablingOptions] = useState([]);
  const [approxAgeOptions, setApproxAgeOptions] = useState([]);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [multiDropdownOptions, setMultiDropdownOptions] = useState([]);
  const [selectedOption, setSelectedOption] = useState([]);
  const [tableStoredData, setTableStoredData] = useState([]);

  const Tablecolumns = [
    {
      Header: t("DEONAR_SLAUGHTER_RECEIPT_NUMBER"),
      accessor: "slaughterReceiptNumber",
      disableSortBy: true,
    },
    {
      Header: t("Deonar_Animal_Token_Number"),
      accessor: "animalTokenNumber",
      disableSortBy: true,
    },
    {
      Header: t("Deonar_Species"),
      accessor: "species",
      disableSortBy: true,
    },
    {
      Header: t("Deonar_Breed"),
      accessor: "breed",
      disableSortBy: true,
    },
    {
      Header: t("Deonar_Sex"),
      accessor: "sex",
      disableSortBy: true,
    },
    {
      Header: t("Deonar_Body_Color"),
      accessor: "bodyColor",
      disableSortBy: true,
    },
    {
      Header: t("Deonar_Eyes"),
      accessor: "eyes",
      disableSortBy: true,
    },
    {
      Header: t("Deonar_Pregnancy"),
      accessor: "pregnancy",
      disableSortBy: true,
    },
    {
      Header: t("Deonar_Gait"),
      accessor: "gait",
      disableSortBy: true,
    },
    {
      Header: t("Deonar_Posture"),
      accessor: "posture",
      disableSortBy: true,
    },
    {
      Header: t("Deonar_Body_Temp"),
      accessor: "bodyTemperature",
      disableSortBy: true,
    },
    {
      Header: t("Deonar_Pulse_Rate"),
      accessor: "pulseRate",
      disableSortBy: true,
    },
    {
      Header: t("Deonar_Approx_Age"),
      accessor: "approximateAge",
      disableSortBy: true,
    },
    {
      Header: t("Deonar_Appetite"),
      accessor: "appetite",
      disableSortBy: true,
    },
    {
      Header: t("Deonar_Nostrils"),
      accessor: "nostrils",
      disableSortBy: true,
    },
    {
      Header: t("Deonar_Muzzle"),
      accessor: "muzzle",
      disableSortBy: true,
    },
    {
      Header: t("Deonar_Opinion"),
      accessor: "opinion",
      disableSortBy: true,
    },
    {
      Header: t("Deonar_Animal_Stabling"),
      accessor: "animalStabling",
      disableSortBy: true,
    },
  ];
  const { data: data2, isLoading } = Digit.Hooks.useCustomMDMS("mh.mumbai", "deonar", [
    { name: "sex" },
    { name: "bodyColor" },
    { name: "eyes" },
    { name: "species" },
    { name: "breed" },
    { name: "pregnancy" },
    { name: "approxAge" },
    { name: "gait" },
    { name: "posture" },
    { name: "bodyTemp" },
    { name: "pulseRate" },
    { name: "appetite" },
    { name: "nostrils" },
    { name: "muzzle" },
    { name: "opinion" },
  ]);

  const {
    control,
    reset,
    setValue,
    getValues,
    formState: { errors, isValid },
  } = useForm({
    defaultValues: {
      shopkeeperName: {},
      helkariName: {},
      slaughterReceiptNumber: "",
      veterinaryOfficerName: "",
      anteMortemInspectionDate: new Date().toISOString().split("T")[0],
      anteMortemInspectionDay: daysOfWeek[new Date().getDay()],
      numberOfAliveAnimals: 0,
      animalTokenNumber: "",
      species: "",
      breed: "",
      sex: "",
      bodyColor: "",
      pregnancy: "",
      approximateAge: 0,
      gait: "",
      posture: "",
      bodyTemperature: "",
      pulseRate: "",
      appetite: "",
      eyes: "",
      nostrils: "",
      muzzle: "",
      opinion: "",
      animalStabling: "",
      other: "",
      remark: "",
    },
    mode: "onChange",
  });
  useEffect(() => {
    if (data2 && !isLoading) {
      setSexOptions(data2?.deonar?.sex);
      setBodyColorOptions(data2?.deonar?.bodyColor);
      setEyesOptions(data2?.deonar?.eyes);
      setSpeciesOptions(data2?.deonar?.species);
      setBreedOptions(data2?.deonar?.breed);
      setPregnancyOptions(data2?.deonar?.pregnancy);
      setGaitOptions(data2?.deonar?.gait);
      setPostureOptions(data2?.deonar?.posture);
      setPulseOptions(data2?.deonar?.pulseRate);
      setBodyTempOptions(data2?.deonar?.bodyTemp);
      setAppetiteOptions(data2?.deonar?.appetite);
      setNostrilOptions(data2?.deonar?.nostrils);
      setMuzzleOptions(data2?.deonar?.muzzle);
      setOpinionOptions(data2?.deonar?.opinion);
      setApproxAgeOptions(data2?.deonar?.approxAge);
    }
  }, [data2, isLoading]);

  useEffect(() => {
    if (inspectionData) {
      setTableStored([inspectionData]);
      const options = inspectionData.animalDetails.map((detail, index) => ({
        AnimalType: detail.animalType,
        value: detail.animalType,
        label: detail.animalType + " - " + detail.count,
        Count: detail.count,
        animalTypeId: detail.animalTypeId,
      }));
      setMultiDropdownOptions(options);
      setValue("animalType", options[0]?.AnimalType || "");
      setValue("animalTokenNumber", options[0]?.Count || "");
    }
  }, [inspectionData, setValue]);

  const handleSelect = (e, selectedOptions) => {
    console.log("Selected options:", selectedOptions); // This should log the selected options
    setSelectedOption(selectedOptions); // Update the selectedOption state
  };

  useEffect(() => {
    setStablingOptions(animalStabling);
  }, []);

  const toggleModal = () => {
    setIsModalOpen(!isModalOpen);
  };

  const handleAdd = () => {
    const formDataValues = getValues();
    const formattedData = {
      slaughterReceiptNumber: formDataValues.slaughterReceiptNumber || "",
      animalTokenNumber: selectedOption[0]?.Count || "",
      species: formDataValues.species?.name || "",
      breed: formDataValues.breed?.name || "",
      sex: formDataValues.sex?.name || "",
      bodyColor: formDataValues.bodyColor?.name || "",
      pregnancy: formDataValues.pregnancy?.name || "",
      approximateAge: formDataValues.approximateAge?.name || "",
      gait: formDataValues.gait?.name || "",
      posture: formDataValues.posture?.name || "",
      bodyTemperature: formDataValues.bodyTemperature?.name || "",
      pulseRate: formDataValues.pulseRate?.name || "",
      appetite: formDataValues.appetite?.name || "",
      eyes: formDataValues.eyes?.name || "",
      nostrils: formDataValues.nostrils?.name || "",
      muzzle: formDataValues.muzzle?.name || "",
      opinion: formDataValues.opinion?.name || "",
      animalStabling: formDataValues.animalStabling?.name || "",
      other: formDataValues.other || "",
      remark: formDataValues.remark || "",
    };

    setTableStoredData((prev) => [...prev, formattedData]);
    reset();
    setIsModalOpen(false);
  };

  const openModal = () => {
    setIsModalOpen(!isModalOpen);
  };

  const handleSave = (e) => {
    e.preventDefault();
    const arrivalId = entryUnitId;
    const tokenNumber = selectedOption[0]?.Count;
    const animalType = selectedOption[0]?.AnimalType;
    const animalTypeId = selectedOption[0]?.animalTypeId;
    const InspectionType = "Before Slaughter Inspection";
    console.log("Form result:", data, arrivalId, InspectionType, tokenNumber, animalType, animalTypeId);
  };

  const TableDataColums = [
    {
      Header: t("Deonar_Entry_Unit_ID"),
      accessor: "entryUnitId",
    },
    {
      Header: t("Deonar_Trader_Name"),
      accessor: "traderName",
    },
    {
      Header: t("Deonar_Date_Of_Arrival"),
      accessor: "dateOfArrival",
    },
    {
      Header: t("Deonar_Time_Of_Arrival"),
      accessor: "timeOfArrival",
    },
    {
      Header: t("Deonar_Licence_Number"),
      accessor: "licenceNumber",
    },
    {
      Header: t("Deonar_Vehicle_Number"),
      accessor: "vehicleNumber",
    },
    {
      Header: t("DEONAR_STAKEHOLDER"),
      accessor: "stakeholderTypeName",
    },
  ];

  const myConfig = {
    elements: [
      {
        type: "p",
        text: "Add Animal",
        style: { textDecoration: "underline", cursor: "pointer" },
        onClick: "onAddClickFunction",
      },
    ],
  };

  useEffect(() => {
    if (inspectionData) {
      setTableStored([inspectionData]);
    }
  }, [inspectionData]);

  return (
    <React.Fragment>
      <div className="bmc-card-full">
        <MainFormHeader title={t("DEONAR_BEFORE_SLAUGHTER_ANTE_MORTEM_INSPECTION")} />
        <div className="bmc-row-card-header">
          <CustomTable
            t={t}
            columns={TableDataColums}
            data={tableStored}
            disableSort={false}
            autoSort={true}
            showTotalRecords={false}
            showSearchField={false}
            manualPagination={false}
            isPaginationRequired={false}
            initSortId="S N"
            getCellProps={(cellInfo) => ({
              style: {
                fontSize: "16px",
              },
            })}
          />
        </div>

        <div className="bmc-row-card-header">
          <div className="bmc-card-row">
            <CustomTable
              t={t}
              columns={Tablecolumns}
              tableClassName={"deonar-custom-scroll"}
              data={tableStoredData}
              disableSort={false}
              autoSort={true}
              manualPagination={false}
              isPaginationRequired={false}
              onAddClickFunction={openModal}
              config={myConfig}
              initSortId="S N"
              getCellProps={(cellInfo) => {
                return {
                  style: {
                    fontSize: "16px",
                  },
                };
              }}
            />
          </div>
          <div className="bmc-card-row" style={{ textAlign: "end", paddingBottom: "1rem", paddingTop: "1rem" }}>
            <button type="submit" onClick={handleSave} className="bmc-card-button" style={{ borderBottom: "3px solid black", marginRight: "1rem" }}>
              Save
            </button>
          </div>
        </div>
        <CustomModal isOpen={isModalOpen} onClose={toggleModal}>
          <form>
            <div className="bmc-card-row">
              <Header>{t("Animal_Token_Number")}</Header>
              <SlaughterReceiptNumberField control={control} setData={setData} data={data} />
              <div className="bmc-col3-card">
                <CardLabel>{t("Animal_Token_Number")}</CardLabel>
                <MultiColumnDropdown
                  displayKeys={["AnimalType", "label"]}
                  optionsKey="value"
                  defaultUnit="Options"
                  autoCloseOnSelect={true}
                  showColumnHeaders={true}
                  options={multiDropdownOptions}
                  headerMappings={{
                    AnimalType: t("Animal Type"),
                    label: t("Token Number"),
                  }}
                  onSelect={handleSelect}
                  selected={selectedOption}
                />
              </div>
            </div>
            <div className="bmc-card-row">
              <Header>{t("Animal_Characterstics")}</Header>
              <HealthStatDropdownField
                name="species"
                label={t("DEONAR_SPECIES")}
                control={control}
                setData={setData}
                data={data}
                options={speciesOptions}
              />
              <HealthStatDropdownField
                name="breed"
                label={t("DEONAR_BREED")}
                control={control}
                setData={setData}
                data={data}
                options={breedOptions}
              />
              <HealthStatDropdownField name="sex" label={t("DEONAR_SEX")} control={control} setData={setData} data={data} options={sexOptions} />

              <HealthStatDropdownField
                name="approximateAge"
                label={t("DEONAR_APPROXIMATE_AGE")}
                control={control}
                setData={setData}
                data={data}
                options={approxAgeOptions}
              />
            </div>
            <div className="bmc-card-row">
              <HealthStatDropdownField
                name="bodyColor"
                label={t("DEONAR_BODY_COLOR")}
                control={control}
                setData={setData}
                data={data}
                options={bodyColorOptions}
              />
              <HealthStatDropdownField name="eyes" label={t("DEONAR_EYES")} control={control} setData={setData} data={data} options={eyesOptions} />
              <HealthStatDropdownField
                name="nostrils"
                label={t("DEONAR_NOSTRILS")}
                control={control}
                setData={setData}
                data={data}
                options={nostrilOptions}
              />
              <HealthStatDropdownField
                name="muzzle"
                label={t("DEONAR_MUZZLE")}
                control={control}
                setData={setData}
                data={data}
                options={muzzleOptions}
              />
            </div>
            <div className="bmc-card-row">
              <HealthStatDropdownField
                name="pregnancy"
                label={t("DEONAR_PREGNANCY")}
                control={control}
                setData={setData}
                data={data}
                options={pregnancyOptions}
              />
              <HealthStatDropdownField
                name="bodyTemperature"
                label={t("DEONAR_BODY_TEMPERATURE")}
                control={control}
                setData={setData}
                data={data}
                options={bodyTempOptions}
              />
              <HealthStatDropdownField
                name="pulseRate"
                label={t("DEONAR_PULSE_RATE")}
                control={control}
                setData={setData}
                data={data}
                options={pulseOptions}
              />

              <HealthStatDropdownField
                name="posture"
                label={t("DEONAR_POSTURE")}
                control={control}
                setData={setData}
                data={data}
                options={postureOptions}
              />
            </div>
            <div className="bmc-card-row">
              <HealthStatDropdownField name="gait" label={t("DEONAR_GAIT")} control={control} setData={setData} data={data} options={gaitOptions} />
              <HealthStatDropdownField
                name="appetite"
                label={t("DEONAR_APPETITE")}
                control={control}
                setData={setData}
                data={data}
                options={appetiteOptions}
              />
            </div>
            <div className="bmc-card-row">
              <Header>{t("Remarks")}</Header>
              <HealthStatDropdownField
                name="opinion"
                label={t("DEONAR_OPINION")}
                control={control}
                setData={setData}
                data={data}
                options={opinionOptions}
              />
              <HealthStatDropdownField
                name="animalStabling"
                label={t("DEONAR_ANIMAL_STABLING")}
                control={control}
                setData={setData}
                data={data}
                options={stablingOptions}
              />
              <OtherField control={control} setData={setData} data={data} />
              <RemarkField control={control} setData={setData} data={data} />
            </div>

            <div className="bmc-card-row" style={{ textAlign: "end", paddingBottom: "1rem" }}>
              <button type="button" className="bmc-card-button" style={{ borderBottom: "3px solid black", outline: "none" }} onClick={handleAdd}>
                Add
              </button>
            </div>
          </form>
        </CustomModal>
      </div>
    </React.Fragment>
  );
};

export default AnteMortemPreSlaughterStatusPage;
