import React, { Fragment, useEffect, useMemo, useRef, useState } from "react";
import { useTranslation } from "react-i18next";
import { Controller, useForm } from "react-hook-form";
import MainFormHeader from "../commonFormFields/formMainHeading";
import useSubmitForm from "../../../hooks/useSubmitForm";
import { COLLECTION_POINT_ENDPOINT } from "../../../constants/apiEndpoints";
import { collectionDynamicColumns, columns, feeConfigs, createDynamicColumns, toastMessages } from "./utils";
import CustomTable from "../commonFormFields/customTable";
import TableCard from "../commonFormFields/tableCard";
import { Loader, RadioButtons, TextInput, Toast } from "@upyog/digit-ui-react-components";
import CollectionFeeCard from "../commonFormFields/collectionFeeCard";
import SubmitButtonField from "../commonFormFields/submitBtn";
import FeeConfirmationPage from "../commonFormFields/feeConfirmationPage";
import useDeonarCommon from "@upyog/digit-ui-libraries/src/hooks/deonar/useCommonDeonar";
import useCollectionPoint from "@upyog/digit-ui-libraries/src/hooks/deonar/useCollectionPoint";
// import { jsPDF } from "jspdf";
// import html2canvas from "html2canvas";
import SearchButtonField from "../commonFormFields/searchBtn";

const radioOptions = [
  { label: "Entry Collection Fee", value: "arrival", feeType: 1 },
  { label: "Stabling Fee (Gawal)", value: "stabling", feeType: 2 },
  { label: "Stabling Fee (DawanWala)", value: "trading", feeType: 9 },
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
  const [slaughterList, setSlaughterList] = useState([]);
  const [tableColumns, setTableColumns] = useState(collectionDynamicColumns[selectedRadioValue]);
  const [customTableColumns, setCustomTableColumns] = useState(createDynamicColumns(() => {}, selectedRadioValue));
  const [removalListData, setRemovalListData] = useState([]);
  const [penaltyListData, setPenaltyListData] = useState([]);
  const [weighingListData, setWeighingListData] = useState([]);
  const [selectedVehicleId, setSelectedVehicleId] = useState(null);
  const [feeCollectionResponse, setFeeCollectionResponse] = useState(null);
  const [isSubmitButtonDisabled, setIsSubmitButtonDisabled] = useState(true);
  const [isSubmissionComplete, setIsSubmissionComplete] = useState(false);

  // const [refreshCollectionFeeCard, setRefreshCollectionFeeCard] = useState(false);
  const [isDownloading, setIsDownloading] = useState(false);

  const [isLoader, setIsLoader] = useState(false);

  const [slaughterData, setSlaughterData] = useState([]);

  const [dynamicOptions, setDynamicOptions] = useState({
    slaughterUnit: [],
    unitShift: [],
  });

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

  const { fetchEntryFeeDetailsbyUUID, fetchStablingList, fetchTradingList, fetchDeonarCommon, fetchSlaughterUnit } = useDeonarCommon();
  const {
    fetchEntryCollectionFee,
    fetchStablingCollectionFee,
    fetchWashingCollectionFee,
    fetchParkingCollectionFee,
    fetchSlaughterCollectionFee,
    fetchParkingCollectionDetails,
    saveCollectionEntryFee,
    fetchSlaughterCollectionList,
    fetchRemovalCollectionFee,
    fetchRemovalList,
    fetchTradingCollectionFee,
    fetchPenaltiesList,
    fetchweighingList,
    fetchweighingFee,
  } = useCollectionPoint({ value: selectedRadioValue });
  // const { fetchEntryCollectionFee, fetchStablingCollectionFee } = useCollectionPoint({});
  const { data: entryData } = fetchEntryCollectionFee({ Search: { Search: selectedUUID } });
  const { data: fetchedData, isLoading } = fetchEntryFeeDetailsbyUUID({ forCollection: true });
  const { data: fetchedStablingData } = fetchStablingList({ forCollection: true });
  const { data: stablingData } = fetchStablingCollectionFee({ Search: { Search: selectedUUID } });
  const { data: parkingDetailsData } = fetchParkingCollectionDetails(
    {},
    {}, // Additional config if needed
    selectedRadioValue === "parking" // Pass the enabled condition here
  );
  const { data: SlaughterListData } = fetchSlaughterCollectionList({ forCollection: true });
  const { data: ParkingData } = fetchParkingCollectionFee({
    vehicleParking: {
      vehicleType: selectedVehicleId,
      vehicleNumber: selectedUUID,
    },
  });
  const { data: vehicleData } = fetchWashingCollectionFee({
    vehicleParking: {
      vehicleType: selectedVehicleId,
      vehicleNumber: selectedUUID,
    },
    selectedRadioValue,
  });
  const { data: removalFeeData } = fetchRemovalCollectionFee({ Search: { Search: selectedUUID } });
  const { data: RemovalListData } = fetchRemovalList({ forCollection: true });
  const { data: fetchedTradingData } = fetchTradingList({ forCollection: true });
  const { data: tradingFeeData } = fetchTradingCollectionFee({ Search: { Search: selectedUUID } });
  const { data: PenaltiesData } = fetchPenaltiesList({});
  const { data: weighingData } = fetchweighingList({});
  const { data: weighingFeeData } = fetchweighingFee({ Search: { Search: selectedUUID } });

  const useFetchOptions = (optionType) => {
    const { data } = fetchDeonarCommon({
      CommonSearchCriteria: {
        Option: optionType,
      },
    });
    return data
      ? data.CommonDetails.map((item) => ({
          name: item.name,
          id: item.id,
        }))
      : [];
  };

  const slaughterUnitData = useFetchOptions("slaughterunit");

  const useFetchShiftOptions = () => {
    const slaughterUnitId = slaughterUnitData[0]?.id;
    const { data } = fetchSlaughterUnit({ slaughterUnitId });

    return (
      data?.unit?.map((shift) => ({
        label: `${shift.openTime} - ${shift.closeTime}`,
      })) || []
    );
  };

  const slaughterUnitShiftData = useFetchShiftOptions({});

  useEffect(() => {
    if (slaughterUnitData && Array.isArray(slaughterUnitData)) {
      setDynamicOptions((prevOptions) => {
        const newOptions = slaughterUnitData.map((unit) => ({
          label: unit.name,
          value: unit.id,
        }));

        // Compare to avoid unnecessary state updates
        if (JSON.stringify(prevOptions.slaughterUnit) === JSON.stringify(newOptions)) {
          return prevOptions; // No update needed
        }

        return {
          ...prevOptions,
          slaughterUnit: newOptions,
        };
      });
    }
  }, [slaughterUnitData]); // Add slaughterUnitData to dependency array

  useEffect(() => {
    if (Array.isArray(slaughterUnitShiftData) && slaughterUnitShiftData.length > 0) {
      setDynamicOptions((prevOptions) => {
        const newOptions = slaughterUnitShiftData.map((shift) => ({
          label: shift.label, // This should be formatted as 'openTime - closeTime'
        }));

        // Compare to avoid unnecessary state updates
        if (JSON.stringify(prevOptions.unitShift) === JSON.stringify(newOptions)) {
          return prevOptions;
        }

        return {
          ...prevOptions,
          unitShift: newOptions,
        };
      });
    }
  }, [slaughterUnitShiftData]);

  const handleSearchData = fetchSlaughterCollectionFee();

  const handleSearch = () => {
    const selectedSlaughterUnit = dynamicOptions.slaughterUnit.find(
      (item) => item.value === dynamicOptions.slaughterUnit[0]?.value || item.id === dynamicOptions.slaughterUnit[0]?.id // Or however you determine the correct unit
    );

    const payload = {
      Search: {
        Search: selectedUUID, // Your original reference number
        slaughterUnit: selectedSlaughterUnit.label,
        openTime: dynamicOptions.unitShift[0].label.split(" - ")[0],
        closeTime: dynamicOptions.unitShift[0].label.split(" - ")[1],
      },
    };

    handleSearchData.mutate(payload, {
      onSuccess: (data) => {
        setSlaughterData(data);
      },
    });
  };

  useEffect(() => {
    setTableColumns(collectionDynamicColumns[radioOptions[0].value]);
    setCustomTableColumns(createDynamicColumns(handleUUIDClick, radioOptions[0].value));
  }, []);

  useEffect(() => {
    if (fetchedData) {
      setAnimalCount(fetchedData.SecurityCheckDetails || []);
      setTotalRecords();
    }
  }, [fetchedData]);

  useEffect(() => {
    if (fetchedStablingData) {
      setStablingListData(fetchedStablingData.SecurityCheckDetails || []);
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
    if (SlaughterListData) {
      setSlaughterList(SlaughterListData?.slaughterLists || []);
      setTotalRecords();
    }
  }, [SlaughterListData]);

  useEffect(() => {
    if (slaughterData) {
      const details = slaughterData?.Details || [];
      const mappedDetails = details.flatMap((detailItem) =>
        detailItem.details.map((item) => ({
          animalType: item?.animal,
          animalCount: item?.count,
          animalFee: item?.fee,
          totalFee: item?.totalFee,
          total: detailItem?.total,
        }))
      );
      setSlaughterList(mappedDetails); // This should now contain all the animal, count, fee, totalFee data
      setTotalRecords(mappedDetails.length); // Update the total records
    }
  }, [slaughterData]);

  useEffect(() => {
    if (RemovalListData) {
      setRemovalListData(RemovalListData.SecurityCheckDetails || []);
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

  const downloadFileName = `${radioOptions.find((option) => option.value === selectedRadioValue)?.label} Fee Collection Report`;

  // const generatePDF = (data) => {
  //   if (!data.length) return;

  //   // Determine the headers and rows based on the data structure
  //   let headers, rows;

  //   if (data[0].details) {
  //     // For complex structures like entry, stabling, slaughter, removal
  //     headers = ['Animal Type', 'Count', 'Fee', 'Total Fee'];
  //     rows = data.flatMap(item =>
  //       item.details.map(detail => ({
  //         'Animal Type': detail.animal,
  //         'Count': detail.count,
  //         'Fee': detail.fee,
  //         'Total Fee': detail.totalFee
  //       }))
  //     );
  //   } else if (data[0].vehiclenumber) {
  //     // For parking and washing
  //     headers = ['Vehicle Number', 'Vehicle Type', 'Parking Date', 'Parking Time', 'Departure Date', 'Departure Time', 'Total Hours', 'Total'];
  //     rows = data.map(item => ({
  //       'Vehicle Number': item.vehiclenumber,
  //       'Vehicle Type': item.vehicletype,
  //       'Parking Date': item.parkingdate,
  //       'Parking Time': item.parkingtime,
  //       'Departure Date': item.departuredate,
  //       'Departure Time': item.departuretime,
  //       'Total Hours': item.totalhours,
  //       'Total': item.total
  //     }));
  //   } else if (data[0].total) {
  //     // For weighing and penalty
  //     headers = Object.keys(data[0]);
  //     rows = data;
  //   } else {
  //     console.warn("Unsupported data structure for PDF generation");
  //     return;
  //   }

  //   const printWindow = window.open("", "", "height=600,width=800");

  //   const htmlContent = `
  //     <!DOCTYPE html>
  //     <html>
  //       <head>
  //         <title>${downloadFileName}</title>
  //         <style>
  //           body {
  //             font-family: Arial, sans-serif;
  //             margin: 20px;
  //           }
  //           table {
  //             width: 100%;
  //             border-collapse: collapse;
  //             margin-bottom: 20px;
  //           }
  //           th, td {
  //             border: 1px solid #ddd;
  //             padding: 12px 8px;
  //             text-align: left;
  //           }
  //           th {
  //             background-color: #f4f4f4;
  //             font-weight: bold;
  //           }
  //           tr:nth-child(even) {
  //             background-color: #f9f9f9;
  //           }
  //           .header {
  //             font-size: 24px;
  //             margin-bottom: 20px;
  //           }
  //           .footer {
  //             margin-top: 20px;
  //             font-size: 12px;
  //             color: #666;
  //           }
  //           @media print {
  //             body { margin: 0; padding: 20px; }
  //             .no-print { display: none; }
  //           }
  //         </style>
  //       </head>
  //       <body>
  //         <div class="header">${downloadFileName}</div>
  //         <table>
  //           <thead>
  //             <tr>${headers.map((header) => `<th>${header}</th>`).join("")}</tr>
  //           </thead>
  //           <tbody>
  //             ${rows
  //               .map(
  //                 (row) => `
  //               <tr>
  //                 ${headers.map((header) => `<td>${row[header] || ""}</td>`).join("")}
  //               </tr>
  //             `
  //               )
  //               .join("")}
  //           </tbody>
  //         </table>
  //         <div class="footer">Generated on ${new Date().toLocaleString()}</div>
  //         <div class="no-print">
  //           <button onclick="window.print();window.close()"
  //             style="padding: 10px 20px; background-color: #4a90e2; color: white;
  //             border: none; border-radius: 4px; cursor: pointer; margin-top: 20px;">
  //             Download PDF
  //           </button>
  //         </div>
  //       </body>
  //     </html>
  //   `;

  //   printWindow.document.write(htmlContent);
  //   printWindow.document.close();
  // };

  const generatePDF = (data) => {
    // Handle different data types
    let receiptData = {};

    // Convert array or object to a flat object if needed
    if (Array.isArray(data)) {
      receiptData = data[0] || {};
    } else if (typeof data === "object" && data !== null) {
      receiptData = data;
    } else {
      console.warn("Unsupported data structure for PDF generation");
      return;
    }

    const downloadFileName = "Payment Receipt";
    const printWindow = window.open("", "", "height=600,width=800");

    const htmlContent = `
      <!DOCTYPE html>
      <html>
        <head>
          <title>${downloadFileName}</title>
          <style>
            body {
              font-family: Arial, sans-serif;
              max-width: 600px;
              margin: 0 auto;
              padding: 20px;
              line-height: 1.6;
            }
            .receipt {
              border: 2px solid #333;
              padding: 20px;
              background-color: #f9f9f9;
            }
            .receipt-header {
              text-align: center;
              border-bottom: 1px solid #ddd;
              padding-bottom: 10px;
              margin-bottom: 20px;
            }
            .receipt-header h1 {
              margin: 0;
              color: #333;
            }
            .receipt-details {
              margin-bottom: 20px;
            }
            .receipt-details div {
              display: flex;
              justify-content: space-between;
              margin-bottom: 10px;
              border-bottom: 1px dotted #ddd;
              padding-bottom: 5px;
            }
            .receipt-details .label {
              font-weight: bold;
              color: #555;
            }
            .receipt-details .value {
              text-align: right;
              color: #333;
            }
            .receipt-footer {
              text-align: center;
              margin-top: 20px;
              font-size: 0.8em;
              color: #666;
            }
            @media print {
              body { margin: 0; padding: 10px; }
              .no-print { display: none; }
            }
          </style>
        </head>
        <body>
          <div class="receipt">
            <div class="receipt-header">
              <h1>${downloadFileName}</h1>
            </div>
            <div class="receipt-details">
              ${Object.entries(receiptData)
                .filter(([key]) => !["audit"].includes(key.toLowerCase()))
                .map(
                  ([key, value]) => `
                  <div>
                    <span class="label">${key.charAt(0).toUpperCase() + key.slice(1)}</span>
                    <span class="value">${formatReceiptValue(value)}</span>
                  </div>
                `
                )
                .join("")}
            </div>
            <div class="receipt-footer">
              Generated on ${new Date().toLocaleString()}
            </div>
          </div>
          <div class="no-print">
            <button onclick="window.print();window.close()" 
              style="display: block; width: 200px; margin: 20px auto; padding: 10px; 
              background-color: #4a90e2; color: white; border: none; border-radius: 4px; cursor: pointer;">
              Download Receipt
            </button>
          </div>
        </body>
      </html>
    `;

    printWindow.document.write(htmlContent);
    printWindow.document.close();
  };

  // Helper function to format receipt values
  const formatReceiptValue = (value) => {
    // Handle null or undefined
    if (value === null || value === undefined) return "N/A";

    // Handle objects (like audit)
    if (typeof value === "object") {
      // For nested objects, return a formatted string
      return Object.entries(value)
        .map(([k, v]) => `${k}: ${v}`)
        .join(", ");
    }

    // Convert other types to string
    return String(value);
  };
  const handlePDFDownload = (e) => {
    e.stopPropagation();
    if (isDownloading) return;
    try {
      setIsDownloading(true);
      // Use the tableData or feeCollectionResponse as the data for PDF
      const downloadData = feeCollectionResponse || tableData;
      // if (!downloadData) {
      //   console.warn("No data available for download");
      //   return;
      // }
      generatePDF(downloadData);
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
        return parkingDetails;
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
      console.log("VehicleId is not available for this option.");
      setSelectedVehicleId(null);
    }
    setSelectedUUID(entryUnitId);
    setIsModalOpen(!isModalOpen);

    const updatedData = getTableData();
    setTableData(updatedData);
  };

  useEffect(() => {
    if (selectedUUID) {
      const scrollAttempts = [100, 300, 500];
      scrollAttempts.forEach((delay) => {
        setTimeout(() => {
          const element = document.getElementById("collection-fee-section");
          if (element) {
            try {
              const yOffset = -10;
              const y = element.getBoundingClientRect().top + window.pageYOffset + yOffset;

              window.scrollTo({
                top: y,
                behavior: "smooth",
              });

              console.log(`Scroll successful after ${delay}ms`);
            } catch (error) {
              console.error(`Scroll attempt failed after ${delay}ms:`, error);
            }
          } else {
            console.log(`Element not found after ${delay}ms`);
          }
        }, delay);
      });
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
    setTableColumns(collectionDynamicColumns[value]);
    setCustomTableColumns(createDynamicColumns(handleUUIDClick, value));
    setTableData([]);
    setSelectedUUID("");
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
      case "arrival":
      case "stabling":
      case "trading":
      case "removal":
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

      case "parking":
      case "washing":
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

      default:
        return basePayload;
    }
  };

  const generateParkingPayload = (selectedUUID) => {
    return {
      vehicleParking: {
        vehicleType: selectedVehicleId,
        vehicleNumber: selectedUUID,
        In: false,
        Out: true,
      },
    };
  };

  const onSubmit = async () => {
    if (!selectedUUID) {
      setToast({ key: "error", message: "No UUID selected" });
      return;
    }
    setIsLoader(true);
    try {
      const payload = generatePayload(formData, selectedRadioValue, selectedUUID, tableData);
      const collectionResponse = await saveCollectionEntryFee(payload);

      if (collectionResponse?.Details && collectionResponse?.ResponseInfo?.status === "successful") {
        setFeeCollectionResponse(collectionResponse?.Details);
        setToast({ key: "success", message: "Entry fee saved successfully!" });
        setSubmittedData(formData);
        // setRefreshCollectionFeeCard(true);
        // reset(getDynamicDefaultValues(selectedRadioValue));
        // updateTableDataAfterPayment(selectedUUID);
        // setSelectedUUID(null);
        // setTableData([]);
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

  // useEffect(() => {
  //   if (refreshCollectionFeeCard) {
  //     setRefreshCollectionFeeCard(false);
  //   }
  // }, [refreshCollectionFeeCard]);

  // New function to update table data after payment
  const updateTableDataAfterPayment = (paidUUID) => {
    let updatedTableData;
    switch (selectedRadioValue) {
      case "arrival":
        updatedTableData = animalCount.filter((item) => item.entryUnitId !== paidUUID);
        setAnimalCount(updatedTableData);
        break;
      case "stabling":
        updatedTableData = stablingListData.filter((item) => item.entryUnitId !== paidUUID);
        setStablingListData(updatedTableData);
        break;
      case "trading":
        updatedTableData = tradingListData.filter((item) => item.entryUnitId !== paidUUID);
        setTradingListData(updatedTableData);
        break;
      case "parking":
        updatedTableData = parkingDetails.filter((item) => item.vehicleNumber !== paidUUID);
        setParkingDetails(updatedTableData);
        break;
      case "washing":
        updatedTableData = parkingDetails.filter((item) => item.vehicleNumber !== paidUUID);
        setParkingDetails(updatedTableData);
        break;
      case "slaughter":
        updatedTableData = slaughterList.filter((item) => item.entryUnitId !== paidUUID);
        setSlaughterList(updatedTableData);
        break;
      case "removal":
        updatedTableData = removalListData.filter((item) => item.entryUnitId !== paidUUID);
        setRemovalListData(updatedTableData);
        break;
      case "penalty":
        updatedTableData = penaltyListData.filter((item) => item.penaltyReference !== paidUUID);
        setPenaltyListData(updatedTableData);
        break;
      case "weighing":
        updatedTableData = weighingListData.filter((item) => item.entryUnitId !== paidUUID);
        setWeighingListData(updatedTableData);
        break;
    }
  };

  // Modify useEffect for table data to reset when selectedRadioValue changes
  useEffect(() => {
    const updatedTableData = getTableData();
    setTableData(updatedTableData);
  }, [
    selectedRadioValue,
    animalCount,
    stablingListData,
    tradingListData,
    parkingDetails,
    slaughterList,
    removalListData,
    penaltyListData,
    weighingListData,
  ]);

  const handleFieldChange = (fieldName, value) => {
    setFormData((prevData) => ({
      ...prevData,
      [fieldName]: value,
    }));
    setValue(fieldName, value, { shouldValidate: true, shouldDirty: true });
  };

  const fields1 = feeConfigs[selectedRadioValue]?.fields || [];
  const options = feeConfigs[selectedRadioValue]?.options || {};

  useEffect(() => {
    let formattedData = [];

    const formatDataBasedOnType = () => {
      if (!selectedUUID) return [];

      switch (selectedRadioValue) {
        case "arrival":
          if (entryData?.Details) {
            return entryData.Details.flatMap((item) =>
              item.details.map((detail) => ({
                animalType: detail.animal,
                animalCount: detail.count,
                animalFee: detail.fee,
                totalFee: detail.totalFee,
                total: item.total,
              }))
            );
          }
          break;

        case "stabling":
          if (stablingData?.Details) {
            return stablingData.Details.flatMap((item) =>
              item.details.map((detail) => ({
                animalType: detail.animal,
                animalCount: detail.count,
                animalFee: detail.fee,
                totalFee: detail.totalFee,
                total: item.total,
              }))
            );
          }
          break;
        case "washing":
          if (vehicleData) {
            const washingData = vehicleData?.VehicleVehicleWashingFeesResponse;
            if (washingData && typeof washingData === "object") {
              return [
                {
                  vehiclenumber: washingData.vehicleNumber,
                  vehicletype: washingData.vehicleType,
                  total: washingData.washingFee,
                },
              ];
            }
          }
          break;

        case "parking":
          if (ParkingData) {
            return ParkingData.vehicleParkingFeeResponseDetails.map((data) => {
              const matchingParkingDetail = parkingDetails.find((detail) => detail.vehicleNumber === data.vehicleNumber);
              const now = new Date();
              const currentDate = now.toISOString().split("T")[0];
              const currentTime = now.toTimeString().split(" ")[0];
              const calculateHours = (parkingDate, parkingTime, departureDate, departureTime) => {
                if (!parkingDate || !parkingTime) return 0;
                const parkingDateTime = new Date(`${parkingDate}T${parkingTime}`);
                const departureDateTime = new Date(`${departureDate}T${departureTime}`);
                const diffMs = departureDateTime - parkingDateTime;
                const hours = Math.round((diffMs / (1000 * 60 * 60)) * 100) / 100;
                return hours > 0 ? hours : 0;
              };
              const totalHours = calculateHours(matchingParkingDetail?.parkingDate, matchingParkingDetail?.parkingTime, currentDate, currentTime);
              return {
                vehiclenumber: data.vehicleNumber,
                vehicletype: data.vehicleType,
                parkingdate: matchingParkingDetail?.parkingDate || data.parkingdate,
                parkingtime: matchingParkingDetail?.parkingTime || data.parkingtime,
                departuredate: currentDate,
                departuretime: currentTime,
                totalhours: totalHours || data.totalhours,
                total: data.parkingFee,
              };
            });
          }
          break;

        case "slaughter":
          if (slaughterData?.Details) {
            return slaughterData?.Details?.flatMap((item) =>
              item?.details?.map((detail) => ({
                animalType: detail?.animal,
                animalCount: detail?.count,
                animalFee: detail?.fee,
                totalFee: detail?.totalFee,
                total: item?.total,
              }))
            );
          }
          break;

        case "removal":
          if (removalFeeData?.Details) {
            return removalFeeData?.Details.flatMap((item) =>
              item?.details?.map((detail) => ({
                animalType: detail?.animal,
                animalCount: detail?.count,
                animalFee: detail?.fee,
                totalFee: detail?.totalFee,
                total: item?.total,
              }))
            );
          }
          break;

        case "trading":
          if (tradingFeeData?.Details) {
            return tradingFeeData?.Details.flatMap((item) =>
              item?.details?.map((detail) => ({
                animalType: detail?.animal,
                animalCount: detail?.count,
                animalFee: detail?.fee,
                totalFee: detail?.totalFee,
                total: item?.total,
              }))
            );
          }
          break;

        case "penalty":
          if (PenaltiesData?.PenaltyLists) {
            const selectedPenalty = selectedUUID;
            const selectedPenalties = PenaltiesData?.PenaltyLists?.filter((item) => item.penaltyReference === selectedPenalty).map((value) => ({
              total: value.total,
              unit: value.unit === null ? 1 : value.unit,
            }));
            return selectedPenalties;
          }
          break;

        case "weighing":
          if (weighingFeeData?.Details) {
            return weighingFeeData?.Details.flatMap((item) =>
              item?.details?.map((detail) => ({
                animal: detail?.animal,
                unit: detail?.unit,
                fee: detail?.fee,
                subtotal: detail?.subtotal,
                skinunit: detail?.skinunit,
                skinfee: detail?.skinfee,
                total: item?.total,
              }))
            );
          }

          break;

        default:
          return [];
      }
      return [];
    };
    formattedData = formatDataBasedOnType();
    setTableData(formattedData);
  }, [
    entryData,
    vehicleData,
    ParkingData,
    selectedRadioValue,
    // slaughterData,
    selectedUUID,
    stablingData,
    removalFeeData,
    tradingFeeData,
    PenaltiesData,
    weighingFeeData,
  ]);

  useEffect(() => {
    if (toast) {
      const timer = setTimeout(() => {
        setToast(null);
      }, 3000);
      return () => clearTimeout(timer);
    }
  }, [toast]);

  const tableData1 = useMemo(() => getTableData(), []);

  const shouldDisplaySearchButton = (selectedUUID, context) => {
    return context === "slaughter" && selectedUUID;
  };

  const context = "slaughter";

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
                    // Add scroll-margin-top to account for fixed headers
                    style={{
                      scrollMarginTop: "100px",
                      position: "relative", // Ensure proper positioning
                    }}
                  >
                    <div className="bmc-card-row">
                      <h3 className="bmc-title">Collection Point</h3>
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
                  tableClassName={"deonar-scrollable-table"}
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
              <div className="bmc-row-card-header" style={{ display: "flex", flexDirection: "column", gap: "45px" }}>
                {selectedUUID ? (
                  <>
                    <div style={{ paddingBottom: "20px", display: "flex", gap: "12px", alignItems: "center" }}>
                      <h3 style={{ fontWeight: "600", fontSize: "20px" }}>Active Arrival UUID: </h3>
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
                    {fields1.length > 0 && (
                      <>
                        <CollectionFeeCard
                          // key={refreshCollectionFeeCard ? Date.now() : "static-key"}
                          label={`${radioOptions.find((option) => option.value === selectedRadioValue)?.label} Details`}
                          fields={feeConfigs[selectedRadioValue].fields}
                          // options={feeConfigs[selectedRadioValue].options}
                          options={{
                            ...feeConfigs[selectedRadioValue].options,
                            ...dynamicOptions,
                          }}
                          control={control}
                          allowEdit={true}
                          Tablecolumns={tableColumns}
                          isLoading={isLoading}
                          showTable={true}
                          tableData={tableData}
                          onFieldChange={handleFieldChange}
                        />

                        <div style={{ paddingBottom: "20px", display: "flex", gap: "12px", alignItems: "center", padding: "0 16px" }}>
                          <h3 style={{ fontWeight: "600", fontSize: "20px" }}>Total Amount Payable : </h3>
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
                          {selectedUUID && selectedRadioValue === "slaughter" && shouldDisplaySearchButton(selectedUUID, context) && (
                            <SearchButtonField onSearch={handleSearch} />
                          )}
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
            <div className="bmc-col-large-header" style={{ width: "100%" }}>
              <div className="bmc-row-card-header">
                {selectedUUID ? (
                  !isConfirmationPage && (
                    <div style={{ flex: 1, paddingLeft: "20px" }}>
                      {/* {feeCollectionResponse && ( */}
                      <button
                        type="button"
                        onClick={handlePDFDownload}
                        className="print-pdf-button"
                        style={{
                          padding: "6px 10px",
                          background: "#a82227",
                          borderRadius: "8px",
                          display: "flex",
                          alignItems: "center",
                          color: "white",
                          fontWeight: "600",
                          border: "none",
                        }}
                      >
                        Print/Download PDF
                      </button>
                      {/* )} */}
                      <FeeConfirmationPage
                        label={`${radioOptions.find((option) => option.value === selectedRadioValue)?.label} Confirmation`}
                        // fields={fields}
                        values={submittedData}
                        options={options}
                        Tablecolumns={tableColumns}
                        isLoading={isLoading}
                        showTable={true}
                        tableData={tableData}
                        feeCollectionResponse={feeCollectionResponse}
                      />
                    </div>
                  )
                ) : (
                  <div className="no-uuid-message">
                    <p style={{ fontSize: "20px" }}>{t("Please select an Deonar arrival ID to view fee confirmation")}</p>
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
