import React, { Fragment, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { useForm } from "react-hook-form";
import MainFormHeader from "../commonFormFields/formMainHeading";
import { CitizenColumns, generateTokenNumber } from "../collectionPoint/utils";
import useDeonarCommon from "@upyog/digit-ui-libraries/src/hooks/deonar/useCommonDeonar";
import CustomModal from "../commonFormFields/customModal";
import CustomTable from "../commonFormFields/customTable";
import TableCard from "../commonFormFields/tableCard";
import MultiColumnDropdown from "../commonFormFields/multiColumnDropdown";
import SubmitButtonField from "../commonFormFields/submitBtn";
import { Toast } from "@upyog/digit-ui-react-components";
import ConfirmationDialog from "../commonFormFields/confirmationDialog";
import PrintableReceipt from "./PrintableReceipt";
import useCollectionPoint from "@upyog/digit-ui-libraries/src/hooks/deonar/useCollectionPoint";

const CitizenGatePass = () => {
    const { t } = useTranslation();
    const [defaults, setDefaults] = useState({});
    const [selectedUUID, setSelectedUUID] = useState();
    const [animalCount, setAnimalCount] = useState([]);
    const [shopKeeper, setShopKeeper] = useState([]);
    const [gawaltable, setGawaltable] = useState([]);
    const [dawanwala, setDawanwala] = useState([]);
    const [shopkeeperOption, setShopkeeperOption] = useState([]);
    const [removalType, setRemovalType] = useState([]);
    const [totalRecords, setTotalRecords] = useState(0);
    const [isModalOpen, setIsModalOpen] = useState(false);
    const [isMobileView, setIsMobileView] = useState(window.innerWidth < 768);
    const [toast, setToast] = useState(null);
    const [isConfirmModalOpen, setIsConfirmModalOpen] = useState(false);
    const [isDirty, setIsDirty] = useState(false);
    const [isDownloading, setIsDownloading] = useState(false);
    const [selectedAnimalDetails, setSelectedAnimalDetails] = useState([]);
    const [citizenName, setCitizenName] = useState('')



    const {
        control,
        setValue,
        handleSubmit,
        getValues,
        formState: { errors, isValid },
    } = useForm({ defaultValues: defaults, mode: "onChange" });

    const { searchDeonarCommon, saveCitizensGatePasses } = useDeonarCommon();
    const { fetchCitizensList } = useCollectionPoint({});
    const fetchPayload = {
        "CitizenGatePassCriteria": {}
    }
    const { data: fetchedData, error } = fetchCitizensList(fetchPayload);


    useEffect(() => {
        if (error) {
            console.error("Error fetching data:", error);
        }

        if (fetchedData?.CitizenGatePassDetails) {
            console.log("Setting data:", fetchedData.CitizenGatePassDetails);
            setAnimalCount(fetchedData.CitizenGatePassDetails);
            setTotalRecords(fetchedData.CitizenGatePassDetails.length);
            setCitizenName(fetchedData.CitizenGatePassDetails[0].CitizenName)
        }
    }, [fetchedData, error]);

    const useFetchOptions = (optionType) => {
        const { data } = searchDeonarCommon({
            CommonSearchCriteria: {
                Option: optionType || [],
            },
        });

        return data
            ? data.CommonDetails.map((item) => ({
                  name: item.name || "Unknown",
                  value: item.id,
                  licenceNumber: item.licenceNumber,
                  mobileNumber: item.mobileNumber,
                  registrationNumber: item.registrationnumber,
                  traderType: item.tradertype,
                  animalType: item.animalType,
              }))
            : [];
    };

    const dawanwalaData = useFetchOptions("dawanwala");
    const shopkeeperData = useFetchOptions("citizen");

    const handleUUIDClick = (entryUnitId) => {
        const selectedEntry = animalCount.find(entry => entry.ArrivalId === entryUnitId);
        if (selectedEntry && selectedEntry.AnimalDetails) {
            setSelectedAnimalDetails(selectedEntry.AnimalDetails);


        };
        setSelectedUUID(entryUnitId);
        setIsModalOpen(!isModalOpen);
    };

    const toggleModal = () => {
        if (isDirty) {
            setIsConfirmModalOpen(true);
        } else {
            resetModal();
            setIsModalOpen(false);
        }
    };
    const handleConfirmClose = (confirm) => {
        setIsConfirmModalOpen(false);
        if (confirm) {
            resetModal();
            setIsModalOpen(false);
            setIsDirty(false);
        }
    };

    const fields = [
        { key: "entryUnitId", label: t("DEONAR_ARRIVAL_UUID"), isClickable: true },
        { key: "traderName", label: t("DEONAR_TRADER_NAME") },
        { key: "licenceNumber", label: t("DEONAR_LICENSE_NUMBER") },
        { key: "vehicleNumber", label: t("Deonar_Vehicle_Number") },
        { key: "dateOfArrival", label: t("ARRIVAL_DATE_FIELD") },
        { key: "timeOfArrival", label: t("ARRIVAL_TIME_FIELD") },
    ];

    useEffect(() => {
        const handleResize = () => {
            setIsMobileView(window.innerWidth < 768);
        };
        window.addEventListener("resize", handleResize);
        handleResize();
        return () => {
            window.removeEventListener("resize", handleResize);
        };
    }, []);

    const isAfterStablingVisibleColumns = [
        { Header: t("Animal Type"), accessor: "AnimalType" },
        {
            Header: t("Animal Token"),
            accessor: "Token",
        },
        {
            Header: t("Citizen's Name"), accessor: "CitizenName"
        }
    ];


    const onSubmit = async (formData) => {
        if (!isValid) {
            setToast({ key: "error", action: t("Please fill in all required fields.") });
            return;
        }

        try {
            // Find the selected entry from animalCount
            const selectedEntry = animalCount.find(entry => entry.ArrivalId === selectedUUID);
            
            if (!selectedEntry) {
                throw new Error("Selected entry not found");
            }

            // Construct the payload to match the fetchCitizensList response structure
            const submitPayload = {
                CitizenGatePassSaveDetails: {
                    ArrivalId: selectedEntry.ArrivalId,
                    CitizenName: selectedEntry.CitizenName,
                    DDReference: selectedEntry.DDReference,
                    Date: selectedEntry.Date,
                    Time: selectedEntry.Time,
                    AnimalDetails: selectedEntry.AnimalDetails
                }
            };

            saveCitizensGatePasses(submitPayload, {
                onSuccess: () => {
                    setToast({ key: "success" });
                    resetModal();
                    setIsModalOpen(false);
                },
                onError: (error) => {
                    console.error("Error submitting form:", error);
                    setToast({ key: "error", action: error.message });
                },
            });
        } catch (error) {
            console.error("Error submitting form:", error);
            setToast({ key: "error", action: error.message });
        }
    };


    const resetModal = () => {
        setSelectedUUID(undefined);
        setRemovalType([]);
        setShopkeeperOption([]);
        setIsDirty(false);
    };

    useEffect(() => {
        if (toast) {
            const timer = setTimeout(() => {
                setToast(null);
            }, 3000);
            return () => clearTimeout(timer);
        }
    }, [toast]);

    const generatePDF = (data) => {
        const printWindow = window.open("", "", "height=600,width=800");

        const receiptData = Array.isArray(data) ? data[0] : data;

        const htmlContent = `
      <!DOCTYPE html>
      <html>
        <head>
          <title>Pranav</title>
          <style>
            /* Your existing styles */
            .receipt-container {
              max-width: 800px;
              margin: 0 auto;
              padding: 20px;
              font-family: Arial, sans-serif;
            }
            .receipt-header {
              text-align: center;
              margin-bottom: 30px;
            }
            .detail-row {
              display: flex;
              justify-content: space-between;
              margin-bottom: 10px;
              padding-bottom: 5px;
              border-bottom: 1px dotted #ddd;
            }
            .label {
              font-weight: bold;
              color: #555;
            }
            .value {
              text-align: right;
            }
            .animal-details {
              margin: 15px 0;
              padding: 10px;
              background-color: #f9f9f9;
            }
            .authorized-signature {
              margin-top: 50px;
              border-top: 1px solid #000;
              width: 200px;
              float: right;
              text-align: center;
              padding-top: 10px;
            }
            @media print {
              .no-print { display: none; }
            }
          </style>
        </head>
        <body>
          <div id="receipt-root"></div>
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

        // Render the React component to string
        const ReactDOMServer = require("react-dom/server");
        const receiptHtml = ReactDOMServer.renderToString(<PrintableReceipt selectedUUID={selectedUUID} citizenName={citizenName}/>);

        printWindow.document.getElementById("receipt-root").innerHTML = receiptHtml;
        printWindow.document.close();
    };

    const handlePDFDownload = (e) => {
        e.stopPropagation();
        if (isDownloading) return;
        try {
            setIsDownloading(true);
            const downloadData = "";
            generatePDF(downloadData);
        } catch (error) {
            console.error("Error downloading PDF:", error);
        } finally {
            setIsDownloading(false);
        }
    };



    const CitizenColumns2 = (handleUUIDClick, t) => [
        {
            Header: t("ID"),
            accessor: "id",
            Cell: ({ row }) => row.index + 1,
            isVisible: false,
        },
        {
            Header: t("DEONAR_ARRIVAL_UUID"),
            accessor: "ArrivalId",
            Cell: ({ row }) => (
                <span
                    onClick={() => handleUUIDClick(row.original.ArrivalId)}
                    style={{ cursor: "pointer", color: "blue" }}
                >
                    {row.original.ArrivalId}
                </span>
            ),
            isVisible: true,
        },
        {
            Header: t("DEONAR_CITIZEN_NAME"),
            accessor: "CitizenName",
            isVisible: true,
        },
        {
            Header: t("DEONAR_DD_REFERENCE"),
            accessor: "DDReference",
            isVisible: true,

        },
        {
            Header: t("ARRIVAL_DATE_FIELD"),
            accessor: "Date",
            isVisible: true,
        },
        {
            Header: t("ARRIVAL_TIME_FIELD"),
            accessor: "Time",
            isVisible: true,
        }
    ];
    return (
        <React.Fragment>
            <div className="bmc-card-full">
                <form onSubmit={handleSubmit(onSubmit)}>
                    <MainFormHeader title={t("DEONAR_CITIZEN_RELIGIOUS_GATEPASS")} />
                    <div className="bmc-card-row">
                        <div className="bmc-row-card-header">
                            {isMobileView && animalCount.map((data, index) => <TableCard data={data} key={index} fields={fields} onUUIDClick={handleUUIDClick} />)}
                            <div className="bmc-card-row" style={{ overflowY: "auto", maxHeight: "511px" }}>

                                <CustomTable
                                    t={t}
                                    columns={CitizenColumns2(handleUUIDClick, t)}
                                    data={animalCount}
                                    manualPagination={false}
                                    // tableClassName={"deonar-scrollable-table"}
                                    totalRecords={totalRecords}
                                    autoSort={false}
                                //   isLoadingRows={isLoading}
                                />
                            </div>
                            {isModalOpen && (
                                <CustomModal isOpen={isModalOpen} onClose={toggleModal} selectedUUID={selectedUUID} style={{ width: "100%" }}>
                                    <Fragment>
                                        <div className="bmc-card-row" style={{ overflowY: "auto", maxHeight: "511px" }}>
                                            <div style={{ display: "flex", gap: "20px", justifyContent: "space-between", marginBottom: "40px" }}>
                                                <div style={{ float: 'right' }}>
                                                    <button
                                                        type="button"
                                                        onClick={handlePDFDownload}
                                                        className="print-pdf-button"
                                                        style={{
                                                            padding: "6px 10px",
                                                            // background: !feeCollectionResponse ? "grey" : "#a82227",
                                                            borderRadius: "8px",
                                                            display: "flex",
                                                            alignItems: "center",
                                                            color: "white",
                                                            fontWeight: "600",
                                                            border: "none",
                                                            background: 'rgb(168, 34, 39)'
                                                            // cursor: feeCollectionResponse ? "pointer" : "not-allowed",
                                                        }}
                                                    //   disabled={!feeCollectionResponse}
                                                    >
                                                        Print/Download PDF
                                                    </button>
                                                </div>

                                            </div>
                                            <CustomTable
                                                t={t}
                                                columns={isAfterStablingVisibleColumns}
                                                data={selectedAnimalDetails}
                                                totalRecords={totalRecords}
                                                autoSort={false}
                                                manualPagination={false}
                                            // tableClassName={"deonar-scrollable-table"}
                                            />
                                        </div>
                                    </Fragment>
                                    <SubmitButtonField />


                                </CustomModal>
                            )}
                        </div>
                    </div>
                    {isConfirmModalOpen && (
                        <ConfirmationDialog
                            message={t("You have unsaved changes. Do you want to continue?")}
                            onConfirm={() => handleConfirmClose(true)}
                            onCancel={() => handleConfirmClose(false)}
                        />
                    )}
                </form>
            </div>
            {toast && (
                <Toast
                    error={toast.key === "error"}
                    label={t(toast.key === "success" ? t("TRADING_DATA_SAVED_SUCCESSFULLY") : toast.action)}
                    onClose={() => setToast(null)}
                    style={{ maxWidth: "670px" }}
                />
            )}
        </React.Fragment>
    );
};

export default CitizenGatePass;

