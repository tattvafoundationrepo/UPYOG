import React, { useEffect, useState } from "react";
import MainFormHeader from "../commonFormFields/formMainHeading";
import { useTranslation } from "react-i18next";
import { RadioButtons, Header, Toast, Loader, EditIcon } from "@upyog/digit-ui-react-components";
import CustomTable from "../commonFormFields/customTable";
import useDeonarCommon from "../../../../../../libraries/src/hooks/deonar/useCommonDeonar";
import { columns, useDebounce } from "../collectionPoint/utils";
import { useForm } from "react-hook-form";
import { abdominalCavity, pelvicCavity, specimenCollection, thoracicCavity, visibleMucusMembrane } from "../../../constants/dummyData";
import { InspectionTableHeader, BeforeSlauhterInspectionHeader } from "./tableHeader";
import { generateTokenNumber } from "../collectionPoint/utils";
import { AnimalInspectionModal, BeforeSlauhterInspectionModal, PostMortemInspectionModal } from "./inspectionModal";

const inspectionTypes = [
  { label: "Ante-Mortem Inspection", value: 1 },
  { label: "Re-Ante Mortem Inspection", value: 2 },
  { label: "Before Slaughter Inspection", value: 3 },
  { label: "Post-Mortem Inspection", value: 4 },
];

const AnteMortemInspectionPage = () => {
  const { t } = useTranslation();
  const [data, setData] = useState({});
  const [selectedUUID, setSelectedUUID] = useState("");
  const [arrivalDataCount, setArrivalDataCount] = useState([]);
  const [radioValueCheck, setRadioValueCheck] = useState(inspectionTypes[0].label);
  const [searchTerm, setSearchTerm] = useState("");
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
  const [approxAgeOptions, setApproxAgeOptions] = useState([]);
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
  const [inspectionType, setInspectionType] = useState(1);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [inspectionTableData, setInspectionTableData] = useState([]);
  const [modalInspectionType, setModalInspectionType] = useState(null);
  const [toast, setToast] = useState(null);
  const [selectedAnimal, setSelectedAnimal] = useState(null);
  const [isLoader, setIsLoader] = useState(false);
  const [inspectionId, setInspectionId] = useState(1);
  const [animalQuarters, setAnimalQuarters] = useState([{ name: "1" }, { name: "2" }, { name: "3" }, { name: "4" }]);
  const [isSubmitted, setIsSubmitted] = useState(false);

  const {
    control,
    setValue,
    reset,
    getValues,
    formState: { errors, isValid },
  } = useForm({
    defaultValues: {
      arrivalUuid: "",
      slaughterReceiptNumber: "",
      animalTokenNumber: "",
      species: "",
      breed: "",
      sex: "",
      bodyColor: "",
      pregnancy: "",
      gait: "",
      posture: "",
      pulseRate: "",
      bodyTemp: "",
      appetite: "",
      eyes: "",
      nostrils: "",
      muzzle: "",
      opinion: "",
      opinionId: "",
      approxAge: "",
      visibleMucusMembrane: "",
      thoracicCavity: "",
      abdominalCavity: "",
      pelvicCavity: "",
      specimenCollection: "",
      specialObservation: "",
      animalQuarters: "",
      other: "",
      resultremark: "",
    },
    mode: "onChange",
  });

  useEffect(() => {
    if (inspectionTableData && inspectionTableData.length > 0) {
      setValue("animalTokenNumber", inspectionTableData[0]?.animalTokenNumber || "");
      setValue("species", inspectionTableData[0]?.species || "");
      setValue("breed", inspectionTableData[0]?.breed || "");
      setValue("sex", inspectionTableData[0]?.sex || "");
      setValue("bodyColor", inspectionTableData[0]?.["bodyColor"] || "");
      setValue("pregnancy", inspectionTableData[0]?.pregnancy || "");
      setValue("gait", inspectionTableData[0]?.gait || "");
      setValue("posture", inspectionTableData[0]?.posture || "");
      setValue("pulseRate", inspectionTableData[0]?.["pulseRate"] || "");
      setValue("bodyTemp", inspectionTableData[0]?.["bodyTemp"] || "");
      setValue("appetite", inspectionTableData[0]?.appetite || "");
      setValue("eyes", inspectionTableData[0]?.eyes || "");
      setValue("nostrils", inspectionTableData[0]?.nostrils || "");
      setValue("muzzle", inspectionTableData[0]?.muzzle || "");
      setValue("opinion", inspectionTableData[0]?.opinion || "");
      setValue("opinionId", inspectionTableData[0]?.opinionId || "");
      setValue("approxAge", inspectionTableData[0]?.["approxAge"] || "");
      setValue("visibleMucusMembrane", inspectionTableData[0]?.["visibleMucusMembrane"] || "");
      setValue("thoracicCavity", inspectionTableData[0]?.["thoracicCavity"] || "");
      setValue("abdominalCavity", inspectionTableData[0]?.["abdominalCavity"] || "");
      setValue("pelvicCavity", inspectionTableData[0]?.["pelvicCavity"] || "");
      setValue("specimenCollection", inspectionTableData[0]?.["specimenCollection"] || "");
      setValue("specialObservation", inspectionTableData[0]?.["specialObservation"] || "");
      setValue("other", inspectionTableData[0]?.other || "");
      setValue("resultremark", inspectionTableData[0]?.resultremark || "");
      setValue("slaughterReceiptNumber", inspectionTableData[0]?.slaughterReceiptNumber || "");
      setValue("animalQuarters", inspectionTableData[0]?.animalQuarters);
    }
  }, [inspectionTableData, setValue]);

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
  ]);

  useEffect(() => {
    if (data2 && !isLoading) {
      setSexOptions(data2?.deonar?.sex?.map((option) => option.name) || []);
      setBodyColorOptions(data2?.deonar?.bodyColor?.map((option) => option.name) || []);
      setEyesOptions(data2?.deonar?.eyes?.map((option) => option.name) || []);
      setSpeciesOptions(data2?.deonar?.species?.map((option) => option.name) || []);
      setBreedOptions(data2?.deonar?.breed?.map((option) => option.name) || []);
      setPregnancyOptions(data2?.deonar?.pregnancy?.map((option) => option.name) || []);
      setGaitOptions(data2?.deonar?.gait?.map((option) => option.name) || []);
      setPostureOptions(data2?.deonar?.posture?.map((option) => option.name) || []);
      setPulseOptions(data2?.deonar?.pulseRate?.map((option) => option.name) || []);
      setBodyTempOptions(data2?.deonar?.bodyTemp?.map((option) => option.name) || []);
      setAppetiteOptions(data2?.deonar?.appetite?.map((option) => option.name) || []);
      setNostrilOptions(data2?.deonar?.nostrils?.map((option) => option.name) || []);
      setMuzzleOptions(data2?.deonar?.muzzle?.map((option) => option.name) || []);
      setApproxAgeOptions(data2?.deonar?.approxAge?.map((option) => option.name) || []);
    }
  }, [data2, isLoading]);

  useEffect(() => {
    setVisibleMucusMembraneOptions(visibleMucusMembrane.map((option) => option.name));
    setThoracicCavityOptions(thoracicCavity.map((option) => option.name));
    setAbdominalCavityOptions(abdominalCavity.map((option) => option.name));
    setPelvicCavityOptions(pelvicCavity.map((option) => option.name));
    setSpecimenCollectionOptions(specimenCollection.map((option) => option.name));
    setSpecialObservationOptions(specialObservationOptions.map((option) => option.name));
    setAnimalQuarters(animalQuarters.map((option) => option.name));
  }, []);

  const debouncedSearchTerm = useDebounce(searchTerm, 300);

  const { fetchEntryFeeDetailsbyUUID, fetchDeonarCommon, saveInspectionDetailsData } = useDeonarCommon();
  const { data: fetchedData } = fetchEntryFeeDetailsbyUUID({ inspectionid: inspectionId });
  const InspectionDetailsData = saveInspectionDetailsData();

  const useFetchOptions = () => {
    const opinionOptionID = inspectionTypes.find((item) => item.label === radioValueCheck)?.value;
    const { data } = fetchDeonarCommon({
      CommonSearchCriteria: {
        inspectionTypeId: opinionOptionID,
        Option: "opinion",
      },
    });

    return data
      ? data.CommonDetails.map((item) => ({
          name: item.name,
          value: item.id,
        }))
      : [];
  };

  const opinionOption = useFetchOptions();

  useEffect(() => {
    setOpinionOptions(opinionOption?.map((item) => item?.name || []));
  }, []);

  useEffect(() => {
    const selectedInspectionId = inspectionTypes.find((item) => item.label === radioValueCheck)?.value;
    if (selectedInspectionId && fetchedData && fetchedData.SecurityCheckDetails) {
      setInspectionId(selectedInspectionId);
      setArrivalDataCount(fetchedData.SecurityCheckDetails);
    }
  }, [radioValueCheck, fetchedData, inspectionId]);

  const toggleModal = () => {
    setIsModalOpen(!isModalOpen);
  };

  const openModal = (rowData) => {
    setIsModalOpen(true);
    setModalInspectionType(inspectionTypes[0].label);
    setSelectedAnimal(rowData);
  };

  const getTableData = Digit.Hooks.deonar.useGetInspectionPointData();

  const Tablecolumns = InspectionTableHeader({ inspectionType, openModal });

  const TableData = (data) => {
    const processedData = [];
    const processedAnimalTokens = new Set();

    data?.InspectionDetails?.forEach((inspection) => {
      const animalDetail = inspection?.animalDetail || {};
      const animalTypeId = animalDetail?.animalTypeId || null;
      const animalTokenNumber = animalDetail?.token || null;
      const animalType = animalDetail?.animalType || "Unknown";
      const opinionId = inspection.opinionId;
      const animal = generateTokenNumber(animalType, animalTokenNumber);
      if (processedAnimalTokens.has(animal)) {
        return;
      }

      processedData.push({
        animalTypeId: animalTypeId,
        animalType: animalType,
        animal: animal,
        editable: animalDetail?.editable || false,
        animalTokenNumber: animalTokenNumber,
        opinionId: opinionId,
        species: inspection.species || "",
        breed: inspection.breed || "",
        sex: inspection.sex || "",
        bodyColor: inspection.bodyColor || "",
        eyes: inspection.eyes || "",
        pregnancy: inspection.pregnancy || "",
        gait: inspection.gait || "",
        posture: inspection.posture || "",
        bodyTemp: inspection.bodyTemp || "",
        approxAge: inspection.approxAge || "",
        pulseRate: inspection.pulseRate || "",
        appetite: inspection.appetite || "",
        nostrils: inspection.nostrils || "",
        muzzle: inspection.muzzle || "",
        opinion: inspection.opinion || "",
        other: inspection.other || "",
        resultremark: inspection.resultremark || "",
        slaughterReceiptNumber: inspection.slaughterReceiptNumber || "",
        visibleMucusMembrane: inspection.visibleMucusMembrane || "",
        thoracicCavity: inspection.thoracicCavity || "",
        abdominalCavity: inspection.abdominalCavity || "",
        pelvicCavity: inspection.pelvicCavity || "",
        specimenCollection: inspection.specimenCollection || "",
        specialObservation: inspection.specialObservation || "",
        animalQuarters: inspection.animalQuarters || "",
      });
      processedAnimalTokens.add(animal);
    });

    setInspectionTableData(processedData);
  };

  const ArrivalData = arrivalDataCount.filter((row) => {
    const values = Object.values(row);
    return values.some((value) => String(value).toLowerCase().includes(debouncedSearchTerm.toLowerCase()));
  });

  const handleUUIDClick = (identifier) => {
    setSelectedUUID(identifier);
    setIsLoader(true);
    setIsSubmitted(false);
    let selectedRow = ArrivalData.find((row) => {
      return String(row.ddreference).trim().toLowerCase() === String(identifier).trim().toLowerCase();
    });

    if (!selectedRow) {
      selectedRow = ArrivalData.find((row) => {
        return String(row.entryUnitId).trim().toLowerCase() === String(identifier).trim().toLowerCase();
      });
    }
    if (!selectedRow) {
      console.error("No matching row found for identifier:", identifier);
      setIsLoader(false);
      return;
    }
    const { entryUnitId, ddreference } = selectedRow;
    const selectedInspectionType = inspectionTypes.find((item) => item.label === radioValueCheck);
    const inspectionTypeValue = selectedInspectionType ? selectedInspectionType.value : inspectionTypes[0].value;
    const payload = {
      entryUnitId: entryUnitId || identifier,
      inspectionAgainst: ddreference || identifier,
      inspectionType: inspectionTypeValue,
    };

    getTableData.mutate(payload, {
      onSuccess: (data) => {
        TableData(data);
        setIsLoader(false);
      },
      onError: (error) => {
        console.error("Error mutating table data:", error);
        setIsLoader(false);
      },
    });
  };

  const handleRadioChange = (value) => {
    setRadioValueCheck(value);
    setSelectedUUID("");
    setInspectionTableData([]);
    setIsSubmitted(false);
    reset();
    setSelectedAnimal(null);
    showToast("notify", `Switched to ${value}`);
    setOpinionOptions(inspectionTypes.find((item) => item.label === value)?.value);

    const selectedInspectionType = inspectionTypes.find((item) => item.label === value);
    if (selectedInspectionType) {
      setInspectionType(selectedInspectionType.value);

      if (selectedUUID && isLoader) {
        setSelectedUUID("");
        setInspectionTableData([]);
        getTableData.mutate(
          {
            entryUnitId: selectedUUID,
            inspectionType: selectedInspectionType.value,
          },
          {
            onSuccess: (data) => {
              TableData(data);
            },
            onError: (error) => {
              console.error("Error fetching new table data:", error);
              setIsLoader(false);
            },
          }
        );
      }
    }
  };

  const saveAnteMortemInspection = Digit.Hooks.deonar.useInspectionPointSave();

  const handleInspectionDetail = () => {
    const identifier = selectedUUID;
    let selectedRow = ArrivalData.find((row) => {
      return String(row.ddreference).trim().toLowerCase() === String(identifier).trim().toLowerCase();
    });

    if (!selectedRow) {
      selectedRow = ArrivalData.find((row) => {
        return String(row.entryUnitId).trim().toLowerCase() === String(identifier).trim().toLowerCase();
      });
    }
    if (!selectedRow) {
      console.error("No matching row found for identifier:", identifier);
      setIsLoader(false);
      return;
    }
    const { entryUnitId, ddreference } = selectedRow;
    const selectedInspectionType = inspectionTypes.find((item) => item.label === radioValueCheck);
    const inspectionTypeValue = selectedInspectionType ? selectedInspectionType.value : inspectionTypes[0].value;
    const payload = {
      entryUnitId: entryUnitId || identifier,
      inspectionAgainst: ddreference || identifier,
      inspectionType: inspectionTypeValue,
    };
    InspectionDetailsData.mutate(payload, {
      onSuccess: (data) => {
        showToast("success", t("YOU HAVE SAVED THE INSPECTION DATA REPORT SUCCESSFULLY"));
        setSelectedUUID(null);
        setInspectionTableData([]);
        setIsSubmitted(true);
        console.log(data, "Inspection details data");
      },
      onError: (error) => {
        console.error("Error fetching new table data:", error);
        setIsLoader(false);
      },
    });
  };

  const showToast = (type, action, duration = 5000) => {
    setToast({ key: type, action });
    setTimeout(() => {
      setToast(null);
    }, duration);
  };

  const handleUpdateData = () => {
    setIsLoader(true);
    const identifier = selectedUUID;
    let selectedRow = ArrivalData.find((row) => {
      return String(row.ddreference).trim().toLowerCase() === String(identifier).trim().toLowerCase();
    });

    if (!selectedRow) {
      selectedRow = ArrivalData.find((row) => {
        return String(row.entryUnitId).trim().toLowerCase() === String(selectedUUID).trim().toLowerCase();
      });
    }

    if (!selectedRow) {
      console.error("No matching row found for selectedUUID:", selectedUUID);
      setIsLoader(false);
      return;
    }
    const { entryUnitId, ddreference } = selectedRow;
    const selectedInspectionType = inspectionTypes.find((item) => item.label === radioValueCheck);
    const inspectionTypeValue = selectedInspectionType ? selectedInspectionType.value : inspectionTypes[0].value;

    const payload = {
      InspectionDetails: {
        entryUnitId: entryUnitId || selectedUUID,
        inspectionAgainst: ddreference || selectedUUID,
        inspectionType: inspectionTypeValue,
        inspectionId: inspectionTypes.find((item) => item.label === radioValueCheck)?.value,
        ...selectedAnimal,
      },
    };
    const currentValues = getValues();
    Object.keys(currentValues).forEach((key) => {
      if (currentValues[key] && currentValues[key] !== selectedAnimal[key]) {
        payload.InspectionDetails[key] = currentValues[key];
      }
    });

    saveAnteMortemInspection.mutate(payload, {
      onSuccess: (response) => {
        showToast("success", t("DEONAR_INSPECTION_DATA_UPDATED_SUCCESSFULLY"));
        setInspectionTableData((prevData) => {
          return prevData.map((item) => {
            if (item.animal === selectedAnimal.animal) {
              return { ...item, ...payload.InspectionDetails };
            }
            return item;
          });
        });
        setIsLoader(false);
      },
      onError: (error) => {
        showToast("error", t("DEONAR_INSPECTION_DATA_NOT_UPDATED_SUCCESSFULLY"));
        setIsLoader(false);
      },
    });
  };

  const TableMainColumns = BeforeSlauhterInspectionHeader({ inspectionType, handleUUIDClick });

  return (
    <React.Fragment>
      <div className="bmc-card-full">
        <MainFormHeader title={t("INSPECTION_POINT")} />
        <div className="bmc-card-row">
          <div style={{ display: "flex", gap: "10px" }}>
            <div style={{ display: "flex", gap: "10px", padding: "0 1px", flexDirection: "column", width: "20%" }}>
              <div className="bmc-col-small-header" style={{ width: "100%" }}>
                <div
                  id="collection-fee-section"
                  className="bmc-row-card-header"
                  // Add scroll-margin-top to account for fixed headers
                  style={{
                    scrollMarginTop: "100px",
                    position: "relative", // Ensure proper positioning
                  }}
                >
                  <div className="bmc-card-row" style={{ overflowY: "auto", maxHeight: "511px" }}>
                    <h3 className="bmc-title">{t("Inspection Point")}</h3>
                    <RadioButtons
                      t={t}
                      label={t("Inspection Point")}
                      options={inspectionTypes.map((type) => type.label)}
                      style={{ display: "flex", marginTop: "45px", flexDirection: "column" }}
                      selectedOption={radioValueCheck}
                      onSelect={handleRadioChange}
                    />
                  </div>
                </div>
              </div>
            </div>

            <div className="bmc-row-card-header" style={{ width: "80%" }}>
              <CustomTable
                t={t}
                columns={TableMainColumns}
                data={ArrivalData}
                disableSort={false}
                autoSort={false}
                manualPagination={false}
                // tableClassName={"deonar-scrollable-table"}
              />
            </div>
          </div>
        </div>

        {/* <div className="bmc-row-card-header">
          <div className="bmc-card-row">
            {selectedUUID && !isSubmitted ? (
              <div style={{ paddingBottom: "20px", display: "flex", gap: "12px", alignItems: "center" }}>
                <h3 style={{ fontWeight: "600", fontSize: "20px" }}>{t("Active Arrival UUID")}:</h3>
                <span style={{ fontWeight: "bold", backgroundColor: "rgb(204, 204, 204)", borderRadius: "10px", padding: "8px", fontSize: "22px" }}>
                  {selectedUUID}
                </span>
              </div>
            ) : (
              <Header style={{ color: "red" }}>{`${t("Arrival UUID - Please Select Arrival UUID from Above Table.")}`}</Header>
            )}
          </div>
          <div className="bmc-card-row">
            {isLoader && radioValueCheck ? (
              <Loader />
            ) : inspectionTableData && radioValueCheck && inspectionTableData.length === 0 && !isSubmitted ? (
              <div className="">
                <strong>{t("Data is not Available.")}</strong>
              </div>
            ) : (
              <React.Fragment>
                {inspectionTableData && radioValueCheck && inspectionTableData.length > 0 && (
                  <CustomTable
                    t={t}
                    columns={[
                      {
                        Header: t("Edit"),
                        accessor: "edit",
                        Cell: ({ row }) => {
                          const editable = row.original.editable;
                          return editable ? (
                            <span onClick={() => openModal(row.original)}>
                              <EditIcon style={{ cursor: "pointer" }} />
                            </span>
                          ) : null;
                        },
                      },
                      ...Tablecolumns,
                    ]}
                    tableClassName={"deonar-custom-scroll"}
                    data={inspectionTableData?.length ? inspectionTableData : null}
                    disableSort={false}
                    autoSort={false}
                    manualPagination={false}
                    onAddClickFunction={() => openModal(inspectionTypes[0].label)}
                    showAddButton={true}
                    buttonText={t("Submit")}
                    onAddClick={handleInspectionDetail}
                  />
                )}
              </React.Fragment>
            )}
          </div>
        </div> */}

        <div className="bmc-row-card-header">
          <div className="bmc-card-row">
            <div style={{ paddingBottom: "20px", display: "flex", gap: "12px", alignItems: "center" }}>
              <h3 style={{ fontWeight: "600", fontSize: "20px" }}>{!isSubmitted && t("Active Arrival UUID")}</h3>
              {!isSubmitted && (
                <span style={{ fontWeight: "bold", backgroundColor: "rgb(204, 204, 204)", borderRadius: "10px", padding: "8px", fontSize: "22px" }}>
                  {selectedUUID || t("Arrival UUID - Please Select Arrival UUID from Above Table.")}
                </span>
              )}
            </div>
          </div>

          <div className="bmc-card-row" style={{ overflowY: "auto", maxHeight: "511px" }}>
            {isLoader && radioValueCheck ? (
              <Loader />
            ) : (
              <React.Fragment>
                {inspectionTableData && radioValueCheck && inspectionTableData.length > 0 && (
                  <CustomTable
                    t={t}
                    columns={[
                      {
                        Header: t("Edit"),
                        accessor: "edit",
                        Cell: ({ row }) => {
                          const editable = row.original.editable;
                          return editable ? (
                            <span onClick={() => openModal(row.original)}>
                              <EditIcon style={{ cursor: "pointer" }} />
                            </span>
                          ) : null;
                        },
                      },
                      ...Tablecolumns,
                    ]}
                    // tableClassName={"deonar-scrollable-table"}
                    data={inspectionTableData}
                    disableSort={false}
                    autoSort={false}
                    manualPagination={false}
                    onAddClickFunction={() => openModal(inspectionTypes[0].label)}
                    showAddButton={true}
                    buttonText={t("Submit")}
                    onAddClick={handleInspectionDetail}
                  />
                )}
                {!isSubmitted && !inspectionTableData?.length && (
                  <div>
                    <strong>{t("Data is not Available.")}</strong>
                  </div>
                )}
                {isSubmitted && (
                  <strong style={{ color: "green", fontWeight: "bold" }}>
                    {t("YOU HAVE SAVED THE INSPECTION DATA REPORT SUCCESSFULLY FOR THIS ARRIVAL UUID")}
                  </strong>
                )}
              </React.Fragment>
            )}
          </div>
        </div>
      </div>

      {modalInspectionType === inspectionTypes[0].label && radioValueCheck === "Ante-Mortem Inspection" && (
        <AnimalInspectionModal
          isModalOpen={isModalOpen}
          toggleModal={toggleModal}
          control={control}
          handleUpdateValue={handleUpdateData}
          inspectionTableData={inspectionTableData}
          speciesOptions={speciesOptions}
          breedOptions={breedOptions}
          sexOptions={sexOptions}
          approxAgeOptions={approxAgeOptions}
          bodyColorOptions={bodyColorOptions}
          pregnancyOptions={pregnancyOptions}
          opinionOptions={opinionOption}
          eyesOptions={eyesOptions}
          nostrilOptions={nostrilOptions}
          muzzleOptions={muzzleOptions}
          bodyTempOptions={bodyTempOptions}
          pulseOptions={pulseOptions}
          postureOptions={postureOptions}
          gaitOptions={gaitOptions}
          appetiteOptions={appetiteOptions}
          selectedAnimal={selectedAnimal}
        />
      )}

      {modalInspectionType === inspectionTypes[0].label && radioValueCheck === "Re-Ante Mortem Inspection" && (
        <AnimalInspectionModal
          isModalOpen={isModalOpen}
          toggleModal={toggleModal}
          control={control}
          handleUpdateValue={handleUpdateData}
          inspectionTableData={inspectionTableData}
          speciesOptions={speciesOptions}
          breedOptions={breedOptions}
          sexOptions={sexOptions}
          approxAgeOptions={approxAgeOptions}
          bodyColorOptions={bodyColorOptions}
          pregnancyOptions={pregnancyOptions}
          opinionOptions={opinionOption}
          eyesOptions={eyesOptions}
          nostrilOptions={nostrilOptions}
          muzzleOptions={muzzleOptions}
          bodyTempOptions={bodyTempOptions}
          pulseOptions={pulseOptions}
          postureOptions={postureOptions}
          gaitOptions={gaitOptions}
          appetiteOptions={appetiteOptions}
          selectedAnimal={selectedAnimal}
        />
      )}

      {modalInspectionType === inspectionTypes[0].label && radioValueCheck === "Before Slaughter Inspection" && (
        <BeforeSlauhterInspectionModal
          isModalOpen={isModalOpen}
          toggleModal={toggleModal}
          control={control}
          handleUpdateValue={handleUpdateData}
          inspectionTableData={inspectionTableData}
          speciesOptions={speciesOptions}
          breedOptions={breedOptions}
          sexOptions={sexOptions}
          approxAgeOptions={approxAgeOptions}
          bodyColorOptions={bodyColorOptions}
          pregnancyOptions={pregnancyOptions}
          opinionOptions={opinionOption}
          eyesOptions={eyesOptions}
          nostrilOptions={nostrilOptions}
          muzzleOptions={muzzleOptions}
          bodyTempOptions={bodyTempOptions}
          pulseOptions={pulseOptions}
          postureOptions={postureOptions}
          gaitOptions={gaitOptions}
          appetiteOptions={appetiteOptions}
          selectedAnimal={selectedAnimal}
        />
      )}

      {modalInspectionType === inspectionTypes[0].label && radioValueCheck === "Post-Mortem Inspection" && (
        <PostMortemInspectionModal
          isModalOpen={isModalOpen}
          toggleModal={toggleModal}
          control={control}
          handleUpdateValue={handleUpdateData}
          inspectionTableData={inspectionTableData}
          speciesOptions={speciesOptions}
          breedOptions={breedOptions}
          sexOptions={sexOptions}
          approxAgeOptions={approxAgeOptions}
          bodyColorOptions={bodyColorOptions}
          pregnancyOptions={pregnancyOptions}
          opinionOptions={opinionOption}
          visibleMucusMembraneOptions={visibleMucusMembraneOptions}
          thoracicCavityOptions={thoracicCavityOptions}
          abdominalCavityOptions={abdominalCavityOptions}
          pelvicCavityOptions={pelvicCavityOptions}
          specimenCollectionOptions={specimenCollectionOptions}
          specialObservationOptions={specialObservationOptions}
          animalQuarters={animalQuarters}
          selectedAnimal={selectedAnimal}
        />
      )}
      {toast && <Toast error={toast.key === t("error")} label={t(toast.action)} onClose={() => setToast(null)} style={{ maxWidth: "670px" }} />}
    </React.Fragment>
  );
};

export default AnteMortemInspectionPage;
