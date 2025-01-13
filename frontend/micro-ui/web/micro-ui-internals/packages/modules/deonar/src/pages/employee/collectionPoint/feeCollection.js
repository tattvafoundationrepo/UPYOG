import React, { Fragment, useEffect, useRef, useState } from "react";
import { useTranslation } from "react-i18next";
import { useForm } from "react-hook-form";
import MainFormHeader from "../commonFormFields/formMainHeading";
import {
  collectionDynamicColumns,
  feeConfigs,
  createDynamicColumns,
  formatFeeDataCollection,
  createDataCache,
  scrollToElementFee,
  generatePDF,
} from "./utils.js";

import CustomTable from "../commonFormFields/customTable";
import TableCard from "../commonFormFields/tableCard";
import { RadioButtons, Toast } from "@upyog/digit-ui-react-components";
import CollectionFeeCard from "../commonFormFields/collectionFeeCard";
import SubmitButtonField from "../commonFormFields/submitBtn";
import useDeonarCommon from "@upyog/digit-ui-libraries/src/hooks/deonar/useCommonDeonar";
import useCollectionPoint from "@upyog/digit-ui-libraries/src/hooks/deonar/useCollectionPoint";
import EntryFeeReceipt from "../reciept/feeReceipt";

const radioOptions = [
  { label: "Entry Collection Fee", value: "arrival", feeType: 1 },
  { label: "Stabling Fee", value: "stabling", feeType: 2 },
  // { label: "Trading Fee", value: "trading", feeType: 9 },
  { label: "Removal Collection Fee", value: "removal", feeType: 3 },
  { label: "Slaughter Recovery Fee", value: "slaughter", feeType: 4 },
  { label: "Vehicle Washing Fee", value: "washing", feeType: 6 },
  { label: "Parking Fee", value: "parking", feeType: 7 },
  { label: "Weighing Charge", value: "weighing", feeType: 5 },
  { label: "Penalty Charge", value: "penalty", feeType: 8 },
];

const FeeCollection = () => {
  const { t } = useTranslation();
  const [formData, setFormData] = useState({});
  const [toast, setToast] = useState(null);
  const [defaults, setDefaults] = useState({});
  const [selectedUUID, setSelectedUUID] = useState(null);
  const [animalCount, setAnimalCount] = useState([]);
  const [stablingListData, setStablingListData] = useState([]);
  const [tradingListData, setTradingListData] = useState([]);
  const [totalRecords, setTotalRecords] = useState(0);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [isMobileView, setIsMobileView] = useState(window.innerWidth < 768);
  const [selectedRadioValue, setSelectedRadioValue] = useState(radioOptions[0].value);
  const [isConfirmationPage, setIsConfirmationPage] = useState(false);
  const [submittedData, setSubmittedData] = useState({});
  const [tableData, setTableData] = useState([]);
  const [parkingDetails, setParkingDetails] = useState([]);
  const [washingDetails, setWashingDetails] = useState([]);
  const [slaughterList, setSlaughterList] = useState([]);
  const [tableColumns, setTableColumns] = useState(collectionDynamicColumns(() => {}, selectedRadioValue));
  const [customTableColumns, setCustomTableColumns] = useState(createDynamicColumns(() => {}, selectedRadioValue));
  const [removalListData, setRemovalListData] = useState([]);
  const [penaltyListData, setPenaltyListData] = useState([]);
  const [weighingListData, setWeighingListData] = useState([]);
  const [selectedVehicleId, setSelectedVehicleId] = useState(null);
  const [feeCollectionResponse, setFeeCollectionResponse] = useState(null);
  const [isSubmitButtonDisabled, setIsSubmitButtonDisabled] = useState(true);
  const [isSubmissionComplete, setIsSubmissionComplete] = useState(false);
  const [isEntrySelected, setIsEntrySelected] = useState(false);
  const [isStablingSelected, setIsStablingSelected] = useState(false);
  const [isTradingSelected, setIsTradingSelected] = useState(false);
  const [isRemovalSelected, setIsRemovalSelected] = useState(false);
  const [isWeighingSelected, setIsWeighingSelected] = useState(false);
  const [isSlaughterSelected, setIsSlaughterSelected] = useState(false);
  const [isParkingSelected, setIsParkingSelected] = useState(false);
  const [isWashingSelected, setIsWashingSelected] = useState(false);
  const [isRemovalFeeSelected, SetIsRemovalFeeSelected] = useState(false);
  const [isDownloading, setIsDownloading] = useState(false);
  const [isLoader, setIsLoader] = useState(false);

  const dataCache = createDataCache();

  const getDynamicDefaultValues = (selectedRadioValue) => {
    const baseDefaultValues = {
      paymentMethod: null, // Changed to null to work with dropdown
      transactionId: "",
    };
    const additionalFields = feeConfigs[selectedRadioValue]?.fields || [];
    additionalFields.forEach((field) => {
      if (field.name) {
        baseDefaultValues[field.name] = "";
      }
    });
    return baseDefaultValues;
  };

  const {
    control,
    setValue,
    handleSubmit,
    reset,
    getValues,
    formState: { errors, isValid },
    register,
    watch,
  } = useForm({
    defaultValues: getDynamicDefaultValues(selectedRadioValue),
    mode: "onChange",
  });

  const watchedFields = watch();

  useEffect(() => {
    if (tableData.length > 0) {
      setValue("amount", tableData[0].total, { shouldValidate: true });
    }
  }, [tableData, setValue]);

  useEffect(() => {
    const requiredFields = feeConfigs[selectedRadioValue]?.fields?.filter((field) => field.required) || [];
    const dynamicRequiredFields = [...requiredFields];
    const allRequiredFieldsFilled = dynamicRequiredFields.every((field) => {
      const value = watchedFields[field.name];
      if (field.required) {
        return value !== undefined && value !== null && value !== "";
      }
      return true;
    });
    const shouldDisableButton = !(allRequiredFieldsFilled && selectedUUID);
    setIsSubmitButtonDisabled(shouldDisableButton);
  }, [watchedFields, selectedRadioValue, selectedUUID, tableData]);

  useEffect(() => {
    const dynamicDefaults = getDynamicDefaultValues(selectedRadioValue);
    reset(dynamicDefaults);
    setIsSubmissionComplete(false);
  }, [selectedRadioValue, reset, isSubmissionComplete]);

  const collectionFeeCardRef = useRef(null);

  const { fetchTradingList } = useDeonarCommon();
  const {
    fetchEntryCollectionFee,
    fetchStablingCollectionFee,
    fetchWashingCollectionFee,
    fetchParkingCollectionFee,
    fetchSlaughterCollectionFee,
    fetchParkingCollectionDetails,
    fetchWashingCollectionDetails,
    saveCollectionEntryFee,
    fetchRemovalCollectionFee,
    fetchTradingCollectionFee,
    fetchPenaltiesList,
    fetchweighingList,
    fetchweighingFee,
    fectchCollectionStablingList,
    fetchCollectionEntryList,
    fetchRemovalCollectionList,
    fetchCollectionSlaughterList,
  } = useCollectionPoint({ value: selectedRadioValue });

  const selectedEntryliceneceNumberItem = animalCount.find((item) => item.entryUnitId === selectedUUID)?.licenceNumber;
  const selectedEntryStakeholder = animalCount.find((item) => item.entryUnitId === selectedUUID)?.stakeholderId;

  const { data: entryData } = fetchEntryCollectionFee({ Search: { Search: selectedUUID } });
  const { data: fetchedData, isLoading } = fetchCollectionEntryList(
    { forCollection: true }
    // { executeOnRadioSelect: isEntrySelected, executeOnLoad: false, enabled: isEntrySelected }
  );
  const { data: fetchedStablingData } = fectchCollectionStablingList(
    { forCollection: true },
    { executeOnRadioSelect: isStablingSelected, executeOnLoad: false, enabled: isStablingSelected }
  );

  const selectedItem = stablingListData.find((item) => item.entryUnitId === selectedUUID)?.licenceNumber;
  const selectedStableStakeholder = stablingListData.find((item) => item.entryUnitId === selectedUUID)?.stakeholderId;

  const { data: stablingData } = fetchStablingCollectionFee(
    { Search: { Search: selectedUUID, liceneceNumber: selectedItem } },
    { executeOnRadioSelect: isStablingSelected, executeOnLoad: false, enabled: isStablingSelected }
  );

  const { data: parkingDetailsData } = fetchParkingCollectionDetails(
    {},
    {}, // Additional config if needed
    selectedRadioValue === "parking" // Pass the enabled condition here
  );
  const { data: washingDetailsData } = fetchWashingCollectionDetails(
    {},
    {}, // Additional config if needed
    selectedRadioValue === "washing" // Pass the enabled condition here
  );
  const { data: SlaughterListData } = fetchCollectionSlaughterList(
    { forCollection: true },
    { executeOnLoad: false, executeOnRadioSelect: isSlaughterSelected, enabled: isSlaughterSelected }
  );

  const { data: slaughterFeeData } = fetchSlaughterCollectionFee(
    { Search: { Search: selectedUUID } },
    { executeOnLoad: false, executeOnRadioSelect: isSlaughterSelected, enabled: isSlaughterSelected }
  );

  const isEmptyVehicle = (vehicleType, vehicleNumber) => {
    return vehicleType === null && vehicleNumber === "";
  };

  const { data: ParkingData } = fetchParkingCollectionFee(
    {
      vehicleParking: {
        vehicleType: selectedVehicleId,
        vehicleNumber: selectedUUID,
      },
    },
    {
      executeOnLoad: false,
      executeOnRadioSelect: isParkingSelected,
      enabled: isParkingSelected && !isEmptyVehicle(selectedVehicleId, selectedUUID),
    }
  );

  const { data: vehicleData } = fetchWashingCollectionFee(
    {
      vehicleWashing: {
        vehicleType: selectedVehicleId,
        vehicleNumber: selectedUUID,
      },
    },
    {
      executeOnLoad: false,
      executeOnRadioSelect: isWashingSelected,
      enabled: isWashingSelected && !isEmptyVehicle(selectedVehicleId, selectedUUID),
    }
  );

  const selectedRemovalliceneceNumberItem = removalListData.find((item) => item.entryUnitId === selectedUUID)?.licenceNumber;
  const selectedRemovalMobileNumber = removalListData.find((item) => item.entryUnitId === selectedUUID)?.mobileNumber;
  const selectedRemovalStakeholder = removalListData.find((item) => item.entryUnitId === selectedUUID)?.stakeholderId;

  const { data: removalFeeData } = fetchRemovalCollectionFee(
    { Search: { Search: selectedUUID, liceneceNumber: selectedRemovalliceneceNumberItem, mobileNumber: selectedRemovalMobileNumber } },
    {
      executeOnLoad: false,
      executeOnRadioSelect: isRemovalFeeSelected,
      enabled: isRemovalFeeSelected,
    }
  );
  const { data: RemovalListData } = fetchRemovalCollectionList(
    { forCollection: true },
    { executeOnLoad: false, executeOnRadioSelect: isRemovalSelected, enabled: isRemovalSelected }
  );
  const { data: fetchedTradingData } = fetchTradingList(
    { forCollection: true },
    { executeOnLoad: false, executeOnRadioSelect: isTradingSelected, enabled: isTradingSelected }
  );
  const { data: tradingFeeData } = fetchTradingCollectionFee(
    { Search: { Search: selectedUUID } },
    { executeOnLoad: false, executeOnRadioSelect: isTradingSelected, enabled: isTradingSelected }
  );
  const { data: PenaltiesData } = fetchPenaltiesList({});
  const { data: weighingData } = fetchweighingList({ executeOnLoad: false, executeOnRadioSelect: isWeighingSelected, enabled: isWeighingSelected });
  const { data: weighingFeeData } = fetchweighingFee(
    { Search: { Search: selectedUUID } },
    { executeOnLoad: false, executeOnRadioSelect: isWeighingSelected, enabled: isWeighingSelected }
  );

  useEffect(() => {
    setTableColumns(collectionDynamicColumns(handleUUIDClick, radioOptions[0].value, t));
    setCustomTableColumns(createDynamicColumns(handleUUIDClick, radioOptions[0].value, t));
  }, []);

  useEffect(() => {
    if (fetchedData) {
      setAnimalCount(fetchedData.CollectionStablingList || []);
      setTotalRecords();
    }
  }, [fetchedData]);

  useEffect(() => {
    if (fetchedStablingData) {
      setStablingListData(fetchedStablingData.CollectionStablingList || []);
      setTotalRecords();
    }
  }, [fetchedStablingData]);

  useEffect(() => {
    if (fetchedTradingData) {
      setTradingListData(fetchedTradingData.SecurityCheckDetails || []);
      setTotalRecords();
    }
  }, [fetchedTradingData]);

  useEffect(() => {
    if (parkingDetailsData) {
      setParkingDetails(parkingDetailsData.VehicleParkedCheckDetails || []);
      setTotalRecords();
    }
  }, [parkingDetailsData]);

  useEffect(() => {
    if (washingDetailsData) {
      setWashingDetails(washingDetailsData.VehicleWashedCheckDetails || []);
      setTotalRecords();
    }
  }, [washingDetailsData]);

  useEffect(() => {
    if (SlaughterListData) {
      setSlaughterList(SlaughterListData?.CollectionStablingList || []);
      setTotalRecords();
    }
  }, [SlaughterListData]);

  useEffect(() => {
    if (RemovalListData) {
      setRemovalListData(RemovalListData.CollectionStablingList || []);
      setTotalRecords();
    }
  }, [RemovalListData]);

  useEffect(() => {
    if (PenaltiesData) {
      setPenaltyListData(PenaltiesData.PenaltyLists || []);
      setTotalRecords();
    }
  }, [PenaltiesData]);

  useEffect(() => {
    if (weighingData) {
      setWeighingListData(weighingData.slaughterLists || []);
      setTotalRecords();
    }
  }, [weighingData]);

  const handlePDFDownload = (e) => {
    e.stopPropagation();
    if (isDownloading) return;

    try {
      setIsDownloading(true);
      const downloadData = feeCollectionResponse || tableData;
      const downloadFileName = `${radioOptions.find((option) => option.value === selectedRadioValue)?.label} Fee Collection Report`;

      generatePDF(downloadData, downloadFileName, require("react-dom/server"), EntryFeeReceipt, t);
    } catch (error) {
      console.error("Error downloading PDF:", error);
    } finally {
      setIsDownloading(false);
    }
  };

  const getTableData = () => {
    switch (selectedRadioValue) {
      case "arrival":
        return animalCount;
      case "stabling":
        return stablingListData;
      case "trading":
        return tradingListData;
      case "parking":
        return parkingDetails;
      case "washing":
        return washingDetails;
      case "slaughter":
        return slaughterList;
      case "removal":
        return removalListData;
      case "penalty":
        return penaltyListData;
      case "weighing":
        return weighingListData;
      default:
        return [];
    }
  };

  const handleUUIDClick = (entryUnitId, vehicleId, tableType) => {
    if (tableType === "parking" || tableType === "washing") {
      setSelectedVehicleId(vehicleId);
    } else {
      setSelectedVehicleId(null);
    }
    setSelectedUUID(entryUnitId);
    setIsModalOpen(!isModalOpen);

    // const updatedData = getTableData();
    // if (updatedData && updatedData.length > 0) {
    //   setTableData(updatedData);
    // }
  };

  useEffect(() => {
    if (selectedUUID) {
      scrollToElementFee("collection-fee-section");
    }
  }, [selectedUUID]);

  const fields = [
    { type: "input", label: "Amount Received", name: "amount", required: true },
    // { type: "dropdown", label: "Payment Method", name: "paymentMethod", required: true },
    {
      type: "input",
      label: "Reference Number",
      name: "transactionId",
      required: false,
    },
  ];

  const handleRadioChange = (value) => {
    setSelectedRadioValue(value);
    setIsConfirmationPage(false);
    setTableColumns(collectionDynamicColumns(handleUUIDClick, value, t));

    setCustomTableColumns(createDynamicColumns(handleUUIDClick, value, t));
    setTableData([]);
    setSelectedUUID("");
    setIsEntrySelected(value === "arrival");
    setIsStablingSelected(value === "stabling");
    setIsTradingSelected(value === "trading");
    setIsRemovalSelected(value === "removal");
    setIsWeighingSelected(value === "weighing");
    setIsSlaughterSelected(value === "slaughter");
    setIsParkingSelected(value === "parking");
    setIsWashingSelected(value === "washing");
    SetIsRemovalFeeSelected(value === "removal");
    // SetIsShiftSelected(value === "slaughter");
  };

  const generatePayload = (formData, selectedRadioValue, selectedUUID, tableData) => {
    const feeType = radioOptions.find((option) => option.value === selectedRadioValue)?.feeType;
    const basePayload = {
      FeeDetail: {
        uuid: selectedUUID,
        feetype: feeType,
        paidby: selectedUUID,
        method: formData.paymentMethod?.value || "Card",
        referenceno: formData.transactionId || selectedUUID,
        slaughtertype: formData.slaughterType?.value,
        slaughterunit: formData.slaughterUnit?.value,
        feevalue: tableData.length > 0 ? tableData[0].total : 0,
      },
    };

    // Add type-specific fields based on the selected radio value
    switch (selectedRadioValue) {
      case "trading":
      case "slaughter":
        const matchingItems = animalCount.filter((item) => item.entryUnitId === selectedUUID);
        return {
          ...basePayload,
          FeeDetail: {
            ...basePayload.FeeDetail,
            paidby: matchingItems.length > 0 ? matchingItems[0].selectedUUID : null,
            details: tableData.map((item) => ({
              animal: item.animalType,
              count: item.animalCount,
              fee: item.animalFee,
              totalFee: item.totalFee,
            })),
          },
        };

      case "arrival":
        return {
          ...basePayload,
          FeeDetail: {
            uuid: selectedUUID,
            feetype: feeType,
            paidby: selectedUUID,
            method: formData.paymentMethod?.value || "Card",
            referenceno: formData.transactionId || selectedUUID,
            feevalue: tableData.length > 0 ? tableData[0].total : 0,
            stakeholderId: selectedEntryStakeholder,
            licenceNumber: selectedEntryliceneceNumberItem,
          },
        };

      case "removal":
        return {
          ...basePayload,
          FeeDetail: {
            uuid: selectedUUID,
            feetype: feeType,
            paidby: selectedUUID,
            method: formData.paymentMethod?.value || "Card",
            referenceno: formData.transactionId || selectedUUID,
            feevalue: tableData.length > 0 ? tableData[0].total : 0,
            stakeholderId: selectedRemovalStakeholder,
          },
        };

      case "stabling":
        return {
          ...basePayload,
          FeeDetail: {
            uuid: selectedUUID,
            feetype: feeType,
            paidby: selectedUUID,
            method: formData.paymentMethod?.value || "Card",
            referenceno: formData.transactionId || selectedUUID,
            feevalue: tableData.length > 0 ? tableData[0].total : 0,
            stakeholderId: selectedStableStakeholder,
            licenceNumber: selectedItem,
          },
        };

      case "parking":
        return {
          ...basePayload,
          FeeDetail: {
            ...basePayload.FeeDetail,
            vehicleParking: {
              vehicleType: selectedVehicleId,
              vehicleNumber: selectedUUID,
              parkingDate: tableData[0]?.parkingdate,
              parkingTime: tableData[0]?.parkingtime,
              departureDate: tableData[0]?.departuredate,
              departureTime: tableData[0]?.departuretime,
              totalHours: tableData[0]?.totalhours,
            },
          },
        };

      case "washing":
        return {
          ...basePayload,
          FeeDetail: {
            ...basePayload.FeeDetail,
            vehicleWashing: {
              vehicleType: selectedVehicleId,
              vehicleNumber: selectedUUID,
              washingDate: tableData[0]?.washingDate,
              washingTime: tableData[0]?.washingTime,
              departureDate: tableData[0]?.departuredate,
              departureTime: tableData[0]?.departuretime,
            },
          },
        };

      default:
        return basePayload;
    }
  };

  const onSubmit = async () => {
    if (!selectedUUID) {
      setToast({ key: "error", message: "No UUID selected" });
      return;
    }

    setIsLoader(true);
    try {
      const payload = generatePayload(formData, selectedRadioValue, selectedUUID, tableData, radioOptions);

      const collectionResponse = await saveCollectionEntryFee(payload);

      if (collectionResponse?.Details && collectionResponse?.ResponseInfo?.status === "successful") {
        setFeeCollectionResponse(collectionResponse?.Details);
        setToast({ key: "success", message: "Entry fee saved successfully!" });
        setSubmittedData(formData);

        // Clear cache for this entry
        dataCache.clearData(`${selectedRadioValue}-${selectedUUID}`);
      } else {
        setToast({ key: "error", message: "An unexpected response was received." });
      }
    } catch (error) {
      console.error("Error occurred during save process:", error);
      setToast({ key: "error", message: "An error occurred while saving the fee." });
    } finally {
      setIsLoader(false);
    }
  };

  const handleFieldChange = (fieldName, value) => {
    setFormData((prevData) => ({
      ...prevData,
      [fieldName]: value,
    }));
    setValue(fieldName, value, { shouldValidate: true, shouldDirty: true });
  };

  const fields1 = feeConfigs[selectedRadioValue]?.fields || [];

  useEffect(() => {
    if (!selectedUUID) return;

    let isSubscribed = true; // For cleanup

    const formatDataBasedOnType = async () => {
      try {
        const currentData = {
          arrival: entryData,
          stabling: stablingData,
          washing: vehicleData,
          parking: ParkingData,
          slaughter: slaughterFeeData,
          removal: removalFeeData,
          trading: tradingFeeData,
          penalty: PenaltiesData,
          weighing: weighingFeeData,
        }[selectedRadioValue];

        // Only proceed if we have data
        if (!currentData) return;

        const formattedData = formatFeeDataCollection(currentData, selectedRadioValue);

        if (formattedData && formattedData.length > 0 && isSubscribed) {
          setTableData(formattedData);
          dataCache.setData(`${selectedRadioValue}-${selectedUUID}`, formattedData);
        }
      } catch (error) {
        console.error("Error formatting data:", error);
      }
    };

    // Clear existing table data before fetching new data
    setTableData([]);

    // Check cache first
    const cachedData = dataCache.getData(`${selectedRadioValue}-${selectedUUID}`);
    if (cachedData) {
      setTableData(cachedData);
    } else {
      formatDataBasedOnType();
    }

    // Cleanup function
    return () => {
      isSubscribed = false;
    };
  }, [
    selectedUUID,
    selectedRadioValue,
    entryData,
    stablingData,
    vehicleData,
    ParkingData,
    slaughterFeeData,
    removalFeeData,
    tradingFeeData,
    PenaltiesData,
    weighingFeeData,
  ]);

  useEffect(() => {
    return () => {
      dataCache.clearAll();
      setTableData([]);
    };
  }, []);

  useEffect(() => {
    if (toast) {
      const timer = setTimeout(() => {
        setToast(null);
      }, 3000);
      return () => clearTimeout(timer);
    }
  }, [toast]);

  return (
    <React.Fragment>
      <div className="bmc-card-full">
        <form onSubmit={handleSubmit(onSubmit)}>
          <MainFormHeader title={"FEE_COLLECTION"} />
          <div className="bmc-card-row">
            <div style={{ display: "flex", gap: "10px" }}>
              <div style={{ display: "flex", gap: "10px", padding: "0 1px", flexDirection: "column", width: "25%" }}>
                <div className="bmc-col-small-header" style={{ width: "100%" }}>
                  <div
                    id="collection-fee-section"
                    className="bmc-row-card-header"
                    ref={collectionFeeCardRef}
                    style={{
                      scrollMarginTop: "100px",
                      position: "relative", // Ensure proper positioning
                    }}
                  >
                    <div className="bmc-card-row">
                      <h3 className="bmc-title">{t("Collection Point")}</h3>
                      <RadioButtons
                        t={t}
                        label="Collection Point"
                        options={radioOptions.map((type) => type.label)}
                        selectedOption={radioOptions.find((option) => option.value === selectedRadioValue)?.label}
                        onSelect={(label) => handleRadioChange(radioOptions.find((option) => option.label === label)?.value)}
                        style={{ display: "flex", marginTop: "10px", flexDirection: "column" }}
                      />
                    </div>
                  </div>
                </div>
              </div>

              <div className="bmc-row-card-header" style={{ width: "75%" }}>
                {isMobileView &&
                  animalCount.map((data, index) => <TableCard data={data} key={index} fields={fields} onUUIDClick={handleUUIDClick} />)}
                <CustomTable
                  t={t}
                  columns={customTableColumns}
                  data={getTableData()}
                  searchPlaceholder={t("Search")}
                  manualPagination={false}
                  totalRecords={totalRecords}
                  sortBy={["entryUnitId:asc"]}
                  autoSort={false}
                  isLoadingRows={isLoading}
                />
              </div>
            </div>
          </div>

          <div style={{ display: "flex", gap: "10px", float: "inline-end", width: "100%" }}>
            <div className="bmc-col-large-header" style={{ width: "100%" }}>
              <div className="bmc-row-card-header" style={{ display: "flex", flexDirection: "column" }}>
                {selectedUUID ? (
                  <>
                    <div style={{ paddingBottom: "20px", display: "flex", gap: "12px", alignItems: "center", justifyContent: "space-between" }}>
                      <div style={{ display: "flex", alignItems: "center", gap: "20px" }}>
                        <h3 style={{ fontWeight: "600", fontSize: "20px" }}>{t("Active Arrival UUID")}: </h3>
                        <span
                          style={{
                            fontWeight: "bold",
                            backgroundColor: "rgb(204, 204, 204)",
                            borderRadius: "10px",
                            padding: "8px",
                            fontSize: "22px",
                          }}
                        >
                          {selectedUUID}
                        </span>
                      </div>

                      <div>
                        <button
                          type="button"
                          onClick={handlePDFDownload}
                          className="print-pdf-button"
                          style={{
                            padding: "6px 10px",
                            background: !feeCollectionResponse ? "grey" : "#a82227",
                            borderRadius: "8px",
                            display: "flex",
                            alignItems: "center",
                            color: "white",
                            fontWeight: "600",
                            border: "none",
                            cursor: feeCollectionResponse ? "pointer" : "not-allowed",
                          }}
                          disabled={!feeCollectionResponse}
                        >
                          {t("Print/Download PDF")}
                        </button>
                      </div>
                    </div>
                    {fields1.length > 0 && (
                      <>
                        <CollectionFeeCard
                          t={t}
                          label={`${radioOptions.find((option) => option.value === selectedRadioValue)?.label}`}
                          fields={feeConfigs[selectedRadioValue].fields}
                          options={{
                            ...feeConfigs[selectedRadioValue].options,
                          }}
                          control={control}
                          allowEdit={true}
                          Tablecolumns={tableColumns}
                          isLoading={isLoading}
                          showTable={true}
                          tableData={tableData}
                          onFieldChange={handleFieldChange}
                          feeType={selectedRadioValue}
                        />

                        <div style={{ paddingBottom: "20px", display: "flex", gap: "12px", alignItems: "center", padding: "0 16px" }}>
                          <h3 style={{ fontWeight: "600", fontSize: "20px" }}>{t("Total Amount Payable")} : </h3>
                          <span
                            style={{
                              fontWeight: "bold",
                              backgroundColor: "rgb(204, 204, 204)",
                              borderRadius: "10px",
                              padding: "8px",
                              display: "flex",
                              gap: "10px",
                              fontSize: "22px",
                            }}
                          >
                            <div>₹</div>
                            <div>{tableData.length > 0 ? tableData[0].total : 0}</div>
                          </span>
                          {selectedUUID && (
                            <SubmitButtonField
                              control={control}
                              isLoading={isLoader}
                              disabled={
                                isSubmitButtonDisabled || isSubmissionComplete || !(formData?.transactionId === "N/A" || !!formData?.transactionId)
                              }
                            />
                          )}
                        </div>
                      </>
                    )}
                  </>
                ) : (
                  <div className="no-uuid-message">
                    <p style={{ fontSize: "20px" }}>{t("Please select an Deonar arrival ID for payment purpose")}</p>
                  </div>
                )}
              </div>
            </div>
          </div>
        </form>
      </div>
      {toast && <Toast error={toast.key === "error"} label={t(toast.message)} onClose={() => setToast(null)} style={{ maxWidth: "670px" }} />}
    </React.Fragment>
  );
};

export default FeeCollection;
