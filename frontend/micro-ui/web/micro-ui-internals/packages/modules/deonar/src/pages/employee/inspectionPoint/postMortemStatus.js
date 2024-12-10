import React, { useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { useForm } from "react-hook-form";
import HealthStatDropdownField from "../commonFormFields/healthStatDropdown";
import OtherField from "../commonFormFields/other";
import RemarkField from "../commonFormFields/remark";
import SlaughterReceiptNumberField from "../commonFormFields/slaughterReceiptNumber";
import { abdominalCavity, daysOfWeek, pelvicCavity, specimenCollection, thoracicCavity, visibleMucusMembrane } from "../../../constants/dummyData";
import CustomModal from "../commonFormFields/customModal";
import CustomTable from "../commonFormFields/customTable";
import { useLocation } from "react-router-dom";
import MainFormHeader from "../commonFormFields/formMainHeading";
import { Header, CardLabel } from "@upyog/digit-ui-react-components";
import MultiColumnDropdown from "../commonFormFields/multiColumnDropdown";

const PostMortemStatusPage = () => {
  const location = useLocation();
  const { t } = useTranslation();
  const { entryUnitId, inspectionData } = location.state || {};
  const [data, setData] = useState({});
  const [speciesOptions, setSpeciesOptions] = useState([]);
  const [breedOptions, setBreedOptions] = useState([]);
  const [sexOptions, setSexOptions] = useState([]);
  const [bodyColorOptions, setBodyColorOptions] = useState([]);
  const [pregnancyOptions, setPregnancyOptions] = useState([]);
  const [visibleMucusMembraneOptions, setVisibleMucusMembraneOptions] = useState([]);
  const [thoracicCavityOptions, setThoracicCavityOptions] = useState([]);
  const [abdominalCavityOptions, setAbdominalCavityOptions] = useState([]);
  const [pelvicCavityOptions, setPelvicCavityOptions] = useState([]);
  const [specimenCollectionOptions, setSpecimenCollectionOptions] = useState([]);
  const [specialObservationOptions, setSpecialObservationOptions] = useState([
    { code: "1", name: "YES" },
    { code: "2", name: "NO" },
    { code: "3", name: "NAN" },
  ]);
  const [opinionOptions, setOpinionOptions] = useState([]);
  const [approxAgeOptions, setApproxAgeOptions] = useState([]);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [tableStoredData, setTableStoredData] = useState([]);
  const [tableStored, setTableStored] = useState([]);
  const [multiDropdownOptions, setMultiDropdownOptions] = useState([]);
  const [selectedOption, setSelectedOption] = useState([]);

  const Tablecolumns = [
    {
      Header: t("Deonar_Slaughter_Receipt_Number"),
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
      Header: t("Deonar_Pregnancy"),
      accessor: "pregnancy",
      disableSortBy: true,
    },
    {
      Header: t("Deonar_Approx_Age"),
      accessor: "approximateAge",
      disableSortBy: true,
    },
    {
      Header: t("Deonar_VISIBLE_MUCUS_MEMBRANE"),
      accessor: "visibleMucusMembrane",
      disableSortBy: true,
    },
    {
      Header: t("Deonar_THORACIC_CAVITY"),
      accessor: "thoracicCavity",
      disableSortBy: true,
    },
    {
      Header: t("Deonar_ABDOMINAL_CAVITY"),
      accessor: "abdominalCavity",
      disableSortBy: true,
    },
    {
      Header: t("Deonar_PELVIC_CAVITY"),
      accessor: "pelvicCavity",
      disableSortBy: true,
    },
    {
      Header: t("Deonar_Specimen_Collection"),
      accessor: "specimenCollection",
      disableSortBy: true,
    },
    {
      Header: t("Deonar_Special_Observation"),
      accessor: "specialObservation",
      disableSortBy: true,
    },
    {
      Header: t("Deonar_Opinion"),
      accessor: "opinion",
      disableSortBy: true,
    },
  ];

  const { data: data2, isLoading } = Digit.Hooks.useCustomMDMS("mh.mumbai", "deonar", [
    { name: "sex" },
    { name: "bodyColor" },
    { name: "species" },
    { name: "breed" },
    { name: "pregnancy" },
    { name: "approxAge" },
    { name: "opinion" },
  ]);

  const {
    control,
    setValue,
    reset,
    getValues,
    formState: { errors, isValid },
  } = useForm({
    defaultValues: {
      shopkeeperName: {},
      helkariName: {},
      slaughterReceiptNumber: "",
      veterinaryOfficerName: "",
      postMortemInspectionDate: new Date().toISOString().split("T")[0],
      postMortemInspectionDay: daysOfWeek[new Date().getDay()],
      numberOfAnimals: 0,
      animalTokenNumber: 0,
      species: "",
      breed: "",
      sex: "",
      bodyColor: "",
      pregnancy: "",
      approximateAge: "",
      visibleMucusMembrane: "",
      thoracicCavity: "",
      abdominalCavity: "",
      pelvicCavity: "",
      specimenCollection: "",
      specialObservation: "",
      opinion: "",
      other: "",
      remark: "",
    },
    mode: "onChange",
  });

  useEffect(() => {
    if (data2 && !isLoading) {
      setSexOptions(data2?.deonar?.sex);
      setBodyColorOptions(data2?.deonar?.bodyColor);
      setSpeciesOptions(data2?.deonar?.species);
      setBreedOptions(data2?.deonar?.breed);
      setPregnancyOptions(data2?.deonar?.pregnancy);
      setOpinionOptions(data2?.deonar?.opinion);
      setApproxAgeOptions(data2?.deonar?.approxAge);
    }
  }, [data2, isLoading]);

  useEffect(() => {
    setVisibleMucusMembraneOptions(visibleMucusMembrane);
    setThoracicCavityOptions(thoracicCavity);
    setAbdominalCavityOptions(abdominalCavity);
    setPelvicCavityOptions(pelvicCavity);
    setSpecimenCollectionOptions(specimenCollection);
    setSpecialObservationOptions(specialObservationOptions);
    // setAnimalTokenNumber(animalTokenNumberOptions);
  }, []);

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

  const toggleModal = () => {
    setIsModalOpen(!isModalOpen);
  };

  const openModal = () => {
    setIsModalOpen(true);
  };

  const handleSave = (e) => {
    e.preventDefault();
    const arrivalId = entryUnitId;
    const tokenNumber = selectedOption[0]?.Count;
    const animalType = selectedOption[0]?.AnimalType;
    const animalTypeId = selectedOption[0]?.animalTypeId;
    const InspectionType = "Post Mortem Inspection";
    console.log("Form result:", data, arrivalId, InspectionType, tokenNumber, animalType, animalTypeId);
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
      visibleMucusMembrane: formDataValues.visibleMucusMembrane?.name || "",
      thoracicCavity: formDataValues.thoracicCavity?.name || "",
      abdominalCavity: formDataValues.abdominalCavity?.name || "",
      pelvicCavity: formDataValues.pelvicCavity?.name || "",
      specimenCollection: formDataValues.specimenCollection?.name || "",
      specialObservation: formDataValues.specialObservation?.name || "",
      opinion: formDataValues.opinion?.name || "",
      other: formDataValues.other || "",
      remark: formDataValues.remark || "",
    };

    setTableStoredData((prev) => [...prev, formattedData]);
    reset();
    setIsModalOpen(false);
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

  useEffect(() => {
    if (inspectionData) {
      setTableStored([inspectionData]);
    }
  }, [inspectionData]);

  return (
    <React.Fragment>
      <div className="bmc-card-full">
        <MainFormHeader title={t("DEONAR_POST_MORTEM_INSPECTION")} />
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
              initSortId="S N"
              onAddClickFunction={openModal}
              config={myConfig}
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
                name="bodyColor"
                label={t("DEONAR_BODY_COLOR")}
                control={control}
                setData={setData}
                data={data}
                options={bodyColorOptions}
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
                name="approximateAge"
                label={t("DEONAR_APPROXIMATE_AGE")}
                control={control}
                setData={setData}
                options={approxAgeOptions}
              />
              <HealthStatDropdownField
                name="visibleMucusMembrane"
                label={t("DEONAR_VISIBLE_MUCUS_MEMBRANE")}
                control={control}
                setData={setData}
                data={data}
                options={visibleMucusMembraneOptions}
              />
              <HealthStatDropdownField
                name="thoracicCavity"
                label={t("DEONAR_THORACIC_CAVITY")}
                control={control}
                setData={setData}
                data={data}
                options={thoracicCavityOptions}
              />
            </div>
            <div className="bmc-card-row">
              <HealthStatDropdownField
                name="abdominalCavity"
                label={t("DEONAR_ABDOMINAL_CAVITY")}
                control={control}
                setData={setData}
                data={data}
                options={abdominalCavityOptions}
              />
              <HealthStatDropdownField
                name="pelvicCavity"
                label={t("DEONAR_PELVIC_CAVITY")}
                control={control}
                setData={setData}
                data={data}
                options={pelvicCavityOptions}
              />
              <HealthStatDropdownField
                name="specimenCollection"
                label={t("DEONAR_SPECIMEN_COLLECTION")}
                control={control}
                setData={setData}
                data={data}
                options={specimenCollectionOptions}
              />
              <HealthStatDropdownField
                name="specialObservation"
                label={t("DEONAR_SPECIAL_OBSERVATION")}
                control={control}
                setData={setData}
                data={data}
                options={specialObservationOptions}
              />
            </div>
            <div className="bmc-card-row">
              <Header>{t("Remarks")}</Header>
              <HealthStatDropdownField
                name="opinion"
                label={t("DEONAR_OPINION")}
                required={true}
                control={control}
                setData={setData}
                data={data}
                options={opinionOptions}
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

export default PostMortemStatusPage;
