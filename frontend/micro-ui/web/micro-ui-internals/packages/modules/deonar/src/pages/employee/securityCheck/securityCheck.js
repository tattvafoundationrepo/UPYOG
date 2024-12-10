import React, { useState, useEffect, useRef } from "react";
import { useTranslation } from "react-i18next";
import { Controller, useForm } from "react-hook-form";
import ImportPermissionNumberField from "../commonFormFields/importPermissionNumber";
import ImportPermissionDateField from "../commonFormFields/importPermissionDate";
import VehicleNumberField from "../commonFormFields/vehicleNumber";
import SubmitButtonField from "../commonFormFields/submitBtn";
import MainFormHeader from "../commonFormFields/formMainHeading";
import useCreateUuid from "../../../hooks/useCreateUuid";
import useDate from "../../../hooks/useCurrentDate";
import useSubmitForm from "../../../hooks/useSubmitForm";
import { COLLECTION_POINT_ENDPOINT } from "../../../constants/apiEndpoints";
import useDeonarCommon from "../../../../../../libraries/src/hooks/deonar/useCommonDeonar";
import { CardLabel, Toast, RemoveIcon } from "@upyog/digit-ui-react-components";
import MultiColumnDropdown from "../commonFormFields/multiColumnDropdown";
import GenericFormInput from "../commonFormFields/genericFormInput";
import DynamicRowGenerator from "../commonFormFields/generateTokenRow";
import CustomTable from "../commonFormFields/customTable";
import CustomModal from "../commonFormFields/customModal";
import { generateTokenNumber } from "../collectionPoint/utils";

const SecurityCheckPage = ({ optionalFields = false, maxAllowedCount = 100 }) => {
  const { t } = useTranslation();
  const [data, setData] = useState({});
  const [importTypeOptions, setImportTypeOptions] = useState([]);
  const [traderOptions, setTraderOptions] = useState([]);
  const [toast, setToast] = useState(null);
  const [arrivalUuid, setArrivalUuid] = useState("");
  const [options, setOptions] = useState([]);
  const [selectedOption, setSelectedOption] = useState([]);
  const [animalTypes, setAnimalTypes] = useState([]);
  const [formModified, setFormModified] = useState(false);
  const [previousOption, setPreviousOption] = useState(null);

  const [selectedTrader, setSelectedTrader] = useState(null);
  const [isModalOpen, setIsModalOpen] = useState(false);

  const [tableData, setTableData] = useState([]);
  const [currentTokens, setCurrentTokens] = useState([]);
  const [isAnimalAdded, setIsAnimalAdded] = useState(false);

  const {
    control,
    setValue,
    handleSubmit,
    getValues,
    reset,
    formState: { errors, isValid, dirtyFields },
  } = useForm({
    defaultValues: {
      arrivalUuid: "",
      importPermissionNumber: "",
      importPermissionDate: useDate(),
      traderName: "",
      licenceNumber: "",
      vehicleNumber: "",
      receiptMode: "",
      paymentReferenceNumber: "",
    },
    mode: "onChange",
  });

  const { submitForm, isSubmitting, response, error } = useSubmitForm(COLLECTION_POINT_ENDPOINT);

  const { searchDeonarCommon, fetchDeonarCommon } = useDeonarCommon();
  const { saveDeonarDetails, isSaving, saveError } = useDeonarCommon();

  const { data: stakeHolderdata, isLoading } = fetchDeonarCommon({
    CommonSearchCriteria: {
      Option: "stakeholder",
    },
  });

  const { data: traderData, error: traderError, refetch } = searchDeonarCommon({
    CommonSearchCriteria: {
      Option: "trader",
    },
    enabled: false,
  });

  const val = useRef(useCreateUuid(5));

  useEffect(() => {
    const generatedUuid = useCreateUuid();
    setArrivalUuid(generatedUuid);
    setValue("arrivalUuid", generatedUuid);
  }, [setValue]);

  useEffect(() => {
    if (traderData && Array.isArray(traderData.CommonDetails)) {
      const traderOptions = traderData.CommonDetails.map((item) => ({
        name: item.name,
        value: item.id,
      }));
      setTraderOptions(traderOptions);
    } else {
      setTraderOptions([]);
    }
  }, [traderData]);

  useEffect(() => {
    if (stakeHolderdata) {
      setImportTypeOptions(
        stakeHolderdata.CommonDetails.map((item) => ({
          name: item.name,
          value: item.id,
        }))
      );
    }
  }, [stakeHolderdata]);

  const handleAnimalUpdate = (rows) => {
    const tokens = rows.reduce((acc, row) => {
      const existing = acc.find((token) => token.animalId === row.dropdownValue.value);
      if (existing) {
        existing.count = Number(row.inputValue);
      } else {
        acc.push({
          animalId: row.dropdownValue.value,
          animalName: row.dropdownValue.label,
          count: Number(row.inputValue),
        });
      }
      setIsAnimalAdded(rows.length > 0);
      return acc;
    }, []);

    setCurrentTokens(tokens);
  };

  const mapAnimalTypesToOptions = (animalTypes) => {
    return animalTypes.map((animal) => ({
      label: animal?.name,
      value: animal?.id,
    }));
  };

  useEffect(() => {
    if (traderData && Array.isArray(traderData.CommonDetails)) {
      const traderNames = traderData.CommonDetails.map((item) => ({
        label: item.name,
        value: item.id,
        licenceNumber: item.licenceNumber,
        mobileNumber: item.mobileNumber,
        tradertype: item.tradertype,
        Registration_Number: item.registrationnumber,
        animalTypes: item.animalType || [],
      }));
      setOptions(traderNames);
    }
  }, [traderData]);

  useEffect(() => {
    const isFormModified = Object.keys(dirtyFields).length > 0;
    setFormModified(isFormModified);
  }, [JSON.stringify(dirtyFields)]);

  const handleSelect = (e, selectedOptions) => {
    const selectedArray = Array.isArray(selectedOptions) ? selectedOptions : [selectedOptions];
    if (formModified) {
      const confirmSwitch = window.confirm("You have unsaved changes. Switching the trader will discard all data. Do you want to continue?");
      if (!confirmSwitch) {
        return;
      }
      reset();
    }
    if (selectedArray.length > 0) {
      const selectedTrader = selectedArray[0];
      setSelectedTrader(selectedTrader);
      setSelectedOption(selectedArray);
      setPreviousOption(selectedArray);
      setValue("licenceNumber", selectedTrader.licenceNumber);
      setAnimalTypes(selectedTrader.animalTypes || []);
      setFormModified(false);
    } else {
      setSelectedTrader(null);
      setSelectedOption([]);
      setValue("licenceNumber", "");
      setAnimalTypes([]);
    }
  };

  const fetchTraderLicenceNumber = async () => {
    if (!selectedOption || selectedOption.length === 0 || !selectedOption[0].value) {
      return "";
    }
    const selectedTrader = options.find((item) => item.value === selectedOption[0].value);
    return selectedTrader ? selectedTrader.licenceNumber : "";
  };

  const openModal = () => {
    console.log("open modal");
    setIsModalOpen(true);
  };
  const toggleModal = () => {
    setIsModalOpen(!isModalOpen);
  };

  const myConfig = {
    elements: [
      {
        type: "p",
        text: t("Add Animal"),
        style: { textDecoration: "underline", cursor: "pointer" },
        onClick: "onAddClickFunction",
      },
    ],
  };

  const removeAnimalRow = (row) => () => {
    const updatedTableData = tableData.filter((item) => item.tokenNumber !== row.tokenNumber);
    const updatedTableDataWithRecalculatedTokens = updatedTableData.map((item, index) => {
      const updatedTokenNumber = generateTokenNumber(item.animalName, index + 1);
      return { ...item, tokenNumber: updatedTokenNumber };
    });

    setTableData(updatedTableDataWithRecalculatedTokens);
  };

  const Tablecolumns = [
    {
      Header: t("Deonar_Animal_Token_Number"),
      accessor: "tokenNumber",
      disableSortBy: true,
    },
    {
      Header: t("Deonar_Animal_Type"),
      accessor: "animalName",
      disableSortBy: true,
    },
    {
      Header: t(t("Actions")),
      accessor: "action",
      Cell: ({ row }) => (
        <span
          onClick={removeAnimalRow(row.original)}
          style={{
            cursor: "pointer",
          }}
        >
          <RemoveIcon />
        </span>
      ),
    },
  ];

  const customHandleAddRow = (currentRow, rows, setRows, setCurrentRow, onRowsUpdate) => {
    const newTokens = currentTokens.flatMap((token) =>
      Array.from({ length: token.count }, (_, index) => ({
        tokenNumber: generateTokenNumber(token.animalName, index + 1),
        animalName: token.animalName,
        count: 1,
        animalId: token.animalId,
      }))
    );

    setTableData((prevData) => {
      const existingTokenNumbers = prevData.map((item) => item.tokenNumber);
      const filteredNewTokens = newTokens.filter((token) => !existingTokenNumbers.includes(token.tokenNumber));
      return [...prevData, ...filteredNewTokens];
    });
    setCurrentTokens([]);
    setIsModalOpen(false);
    setIsAnimalAdded(true);
    onRowsUpdate(rows);
  };

  const customHandleGenerateRows = (currentRow, rows, setRows, setCurrentRow, onRowsUpdate) => {
    const inputValue = parseInt(currentRow.inputValue);
    if (!currentRow.dropdownValue || isNaN(inputValue) || inputValue <= 0) {
      alert("Please select an animal type and enter a valid number.");
      return;
    }
    const currentAnimalCount = tableData.filter((token) => token.animalId === currentRow.dropdownValue.value).length;
    const newTokens = Array.from({ length: inputValue }, (_, index) => ({
      tokenNumber: generateTokenNumber(currentRow.dropdownValue.label, currentAnimalCount + index + 1),
      animalName: currentRow.dropdownValue.label,
      count: 1,
      animalId: currentRow.dropdownValue.value,
    }));
    setTableData((prevData) => [...prevData, ...newTokens]);
    const newRow = {
      dropdownValue: currentRow.dropdownValue,
      inputValue: inputValue,
    };
    const updatedRows = [...rows, newRow];
    setRows(updatedRows);
    setCurrentRow({ dropdownValue: "", inputValue: "" });
    setIsAnimalAdded(true);
    setIsModalOpen(false);
    // Call the original onRowsUpdate function
    onRowsUpdate(updatedRows);
  };

  const onSubmit = async (formData) => {
    try {
      if (!selectedTrader) {
        setToast({ key: "error", action: "Please select a trader before submitting" });
        return;
      }
      setIsAnimalAdded(true);
      const stakeholderId = selectedTrader.value;
      const currentTime = new Date();
      const formattedTime = currentTime.toTimeString().split(" ")[0];

      const animalData1 = tableData.reduce((acc, item) => {
        const existingAnimal = acc.find((a) => a.animalTypeId === item.animalId);
        const numericToken = parseInt(item.tokenNumber.replace(/\D/g, ""), 10);
        if (existingAnimal) {
          existingAnimal.count.push(numericToken);
        } else {
          acc.push({
            animalTypeId: item.animalId,
            count: [numericToken],
          });
        }
        return acc;
      }, []);

      const payload = {
        ArrivalDetails: {
          id: 1,
          arrivalId: formData.arrivalUuid || arrivalUuid,
          importPermission: formData.importPermissionNumber || "IMP456789",
          stakeholderId: stakeholderId,
          permissionDate: formData.importPermissionDate,
          timeOfArrival: formattedTime || "12:30:00",
          vehicleNumber: formData.vehicleNumber || "MH12XY1234",
          licenceNumber: formData.licenceNumber || "TRD456789",
          createdAt: Date.now() / 1000,
          updatedAt: Date.now() / 1000,
          createdBy: 1001,
          updatedBy: 1002,
        },
        AnimalDetails: animalData1,
      };

      saveDeonarDetails(payload, {
        onSuccess: () => {
          const uuid = formData.arrivalUuid || arrivalUuid;
          console.log("Toast Action:", `Data saved successfully. Arrival UUID is: ${uuid}`);
          setToast({
            key: "success",
            action: `Data saved successfully. Arrival UUID is:`,
            uuid, // Pass UUID to the toast
          });
          reset();

          setTableData([]);
          setSelectedTrader(null);
          setSelectedOption([]);
          setAnimalTypes([]);
          setIsAnimalAdded(false);
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

  const copyToClipboard = (text) => {
    navigator.clipboard.writeText(text).then(
      () => {
        setToast(null); // Clear the current toast
        setTimeout(() => {
          setToast({
            key: "success",
            action: "UUID copied to clipboard.",
          });
        }, 100); // Add a small delay to ensure UI updates properly
      },
      (err) => {
        console.error("Failed to copy text: ", err);
        setToast({
          key: "error",
          action: "Failed to copy UUID to clipboard.",
        });
      }
    );
  };

  // useEffect(() => {
  //   setFormModified(Object.keys(isAnimalAdded).length > 0);
  // }, [isAnimalAdded]);

  const resetModal = () => {
    setDawanwalaOption([]);
    setIsDirty(false);
  };

  return (
    <React.Fragment>
      <div className="bmc-card-full">
        <form onSubmit={handleSubmit(onSubmit)}>
          <MainFormHeader title={"DEONAR_ARRIVAL_DETAILS"} />
          <div className="bmc-card-row" style={{ display: "flex", gap: "10px" }}>
            <div className="bmc-row-card-header" style={{ display: "flex", flexDirection: "column", width: "50%" }}>
              <div className="bmc-card-row">
                <div className="bmc-title">{t("ARRIVAL_DETAILS")}</div>
                <CardLabel className="bmc-label">
                  {t("DEONAR_TRADER")}&nbsp;{error && <sup style={{ color: "red", fontSize: "x-small" }}>{error}</sup>}
                </CardLabel>
                <MultiColumnDropdown
                  options={options}
                  selected={selectedOption}
                  onSelect={handleSelect}
                  // defaultLabel="Select or Search"
                  displayKeys={["label", "licenceNumber", "mobileNumber", "Registration_Number"]}
                  placeholder="Select Trader"
                  optionsKey="value"
                  defaultUnit="Options"
                  autoCloseOnSelect={true}
                  showColumnHeaders={true}
                  headerMappings={{
                    label: "Name",
                    licenceNumber: "License Number",
                    mobileNumber: "Mobile Number",
                    Registration_Number: "Registration Number",
                  }}
                />
              </div>
              <div className="bmc-card-row" style={{ display: "flex", flexDirection: "column", gap: "10px", marginTop: "20px" }}>
                <GenericFormInput
                  placeholder={t("LICENSE_NUMBER")}
                  control={control}
                  fetchData={fetchTraderLicenceNumber}
                  name={"licenceNumber"}
                  key={"licenceNumber"}
                />
                <VehicleNumberField control={control} setData={setData} data={data} style={{ width: "100%" }} />
              </div>
              <div className="bmc-card-row" style={{ display: "flex", flexDirection: "column", gap: "10px" }}>
                <ImportPermissionDateField control={control} setData={setData} data={data} style={{ width: "100%" }} />
                <ImportPermissionNumberField control={control} setData={setData} data={data} style={{ width: "100%" }} />
              </div>
            </div>
            <div className="bmc-row-card-header" style={{ width: "50%" }}>
              <div className="bmc-card-row">
                <CustomTable
                  t={t}
                  columns={Tablecolumns}
                  tableClassName=" deonar-scrollable-table"
                  data={tableData}
                  disableSort={false}
                  autoSort={false}
                  manualPagination={false}
                  // onAddClickFunction={openModal}
                  onAddEmployeeClick={openModal}
                  config={myConfig}
                  isLoadingRows={isLoading}
                  getCellProps={(cellInfo) => {
                    return {
                      style: {
                        fontSize: "16px",
                      },
                    };
                  }}
                />
              </div>
            </div>
            <CustomModal isOpen={isModalOpen} onClose={toggleModal} title={t("GENERATE_TOKEN")}>
              <DynamicRowGenerator
                onRowsUpdate={handleAnimalUpdate}
                label="Animal Type"
                dynamicOptionKey="label"
                options={mapAnimalTypesToOptions(animalTypes)}
                inputLabel="Animal Count"
                dropdownLabel="Animal Type"
                generateLabel={t("Generate Token")}
                showMappedRows={false}
                generateButtonLabel="Generate"
                handleGenerateRows={customHandleGenerateRows}
                handleAddRow={customHandleAddRow}
                maxAllowedCount={maxAllowedCount}
                showAddIcon={false}
                allowRemove={false}
                message="Token generated successfully"
                messageDuration={3000}
              />
              {/* <div className="bmc-card-row" style={{ textAlign: "end", paddingBottom: "1rem" }}>
                <button type="button" className="bmc-card-button" style={{ borderBottom: "3px solid black", outline: "none" }} onClick={handleAdd}>
                  Add
                </button>
              </div> */}
            </CustomModal>
          </div>
          <SubmitButtonField control={control} disabled={!isAnimalAdded || !formModified} />
        </form>
      </div>
      {toast && (
        <Toast
          error={toast.key === "error"}
          label={
            toast.key === "success" ? (
              <span>
                {toast.action}
                {toast.uuid && (
                  <span
                    style={{
                      display: "inline-block",
                      backgroundColor: "white",
                      padding: "2px 6px",
                      borderRadius: "4px",
                      border: "1px solid #ccc",
                      marginLeft: "8px",
                      color: "black",
                    }}
                  >
                    {toast.uuid}{" "}
                    <button
                      onClick={() => {
                        copyToClipboard(toast.uuid);
                        // Immediately clear the UUID and icon
                        setToast({ ...toast, uuid: null });
                      }}
                      style={{
                        background: "none",
                        border: "none",
                        cursor: "pointer",
                        color: "#007bff",
                        marginLeft: "4px",
                      }}
                      title="Copy UUID"
                    >
                      📋
                    </button>
                  </span>
                )}
              </span>
            ) : (
              toast.action
            )
          }
          onClose={() => setToast(null)}
          style={{ maxWidth: "670px" }}
        />
      )}
    </React.Fragment>
  );
};

export default SecurityCheckPage;
