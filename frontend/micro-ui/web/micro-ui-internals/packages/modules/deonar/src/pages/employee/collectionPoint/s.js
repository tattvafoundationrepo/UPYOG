import React, { Fragment, useEffect, useMemo, useState } from "react";
import { useTranslation } from "react-i18next";
import { Controller, useForm } from "react-hook-form";
import MainFormHeader from "../commonFormFields/formMainHeading";
import useSubmitForm from "../../../hooks/useSubmitForm";
import { COLLECTION_POINT_ENDPOINT } from "../../../constants/apiEndpoints";
import { columns, generateTokenNumber } from "./utils";
import useDeonarCommon from "@upyog/digit-ui-libraries/src/hooks/deonar/useCommonDeonar";
import CustomModal from "../commonFormFields/customModal";
import CustomTable from "../commonFormFields/customTable";
import TableCard from "../commonFormFields/tableCard";
import MultiColumnDropdown from "../commonFormFields/multiColumnDropdown";
import SubmitButtonField from "../commonFormFields/submitBtn";
import { Toast } from "@upyog/digit-ui-react-components";
import ConfirmationDialog from "../commonFormFields/confirmationDialog";

const Trading = () => {
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

  const [isGlobalShopkeeperEnabled, setIsGlobalShopkeeperEnabled] = useState(false);

  const [globalShopkeeper, setGlobalShopkeeper] = useState(null);
  const [toast, setToast] = useState(null);
  const [showIndividualMessage, setShowIndividualMessage] = useState(false);
  const [showConfirmDialog, setShowConfirmDialog] = useState(false);
  const [isConfirmModalOpen, setIsConfirmModalOpen] = useState(false);
  const [isDirty, setIsDirty] = useState(false);

  const {
    control,
    setValue,
    handleSubmit,
    getValues,
    formState: { errors, isValid },
  } = useForm({ defaultValues: defaults, mode: "onChange" });

  const { submitForm, isSubmitting, response, error } = useSubmitForm(COLLECTION_POINT_ENDPOINT);

  const { fetchTradingList, fetchDeonarCommon, searchDeonarCommon, saveStablingDetails } = useDeonarCommon();
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
  const shopkeeperData = useFetchOptions("shopkeeper");

  // const { data: fetchedData, isLoading } = fetchTradingList({});
  const { data: fetchedData, isLoading } = fetchTradingList({}, { executeOnLoad: true, executeOnRadioSelect: false });


  useEffect(() => {
    if (fetchedData) {
      setAnimalCount(fetchedData.SecurityCheckDetails);
      setTotalRecords();
    }
  }, [fetchedData]);

  useEffect(() => {
    if (dawanwalaData.length) {
      const combinedData = dawanwalaData.map((item) => {
        const animalTypes = item.animalType.map((animal) => animal.name).join(", ");
        return {
          label: item.name || "Unknown",
          licenceNumber: item.licenceNumber,
          mobileNumber: item.mobileNumber,
          registrationNumber: item.registrationNumber,
          animalTypes: animalTypes || "No Animal Type",
          traderType: item.traderType,
          value: item.value,
        };
      });
      if (JSON.stringify(combinedData) !== JSON.stringify(dawanwala)) {
        setDawanwala(combinedData);
      }
    }
  }, [dawanwalaData, dawanwala]);

  useEffect(() => {
    if (shopkeeperData.length) {
      const combinedData = shopkeeperData.map((item) => {
        const animalTypes = item.animalType.map((animal) => animal.name).join(", ");
        return {
          label: item.name || "Unknown",
          licenceNumber: item.licenceNumber,
          mobileNumber: item.mobileNumber,
          registrationNumber: item.registrationNumber,
          animalTypes: animalTypes || "No Animal Type",
          traderType: item.traderType,
          value: item.value,
        };
      });
      if (JSON.stringify(combinedData) !== JSON.stringify(shopKeeper)) {
        setShopKeeper(combinedData);
      }
    }
  }, [shopkeeperData, shopKeeper]);

  const handleUUIDClick = (entryUnitId) => {
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

  useEffect(() => {
    if (fetchedData && fetchedData.SecurityCheckDetails) {
      const securityCheckDetails = fetchedData.SecurityCheckDetails;
      const mappedAnimalCount = securityCheckDetails.flatMap((detail) =>
        detail.animalDetails.map((animal) => ({
          arrivalId: detail.entryUnitId,
          animalTypeId: animal.animalTypeId,
          animalType: animal.animalType,
          count: animal.token,
          tradable: animal.tradable,
          stable: animal.stable,
        }))
      );
      setGawaltable(mappedAnimalCount);
    }
  }, [fetchedData]);

  const filteredGawaltable = gawaltable.filter((animal) => animal.arrivalId === selectedUUID);

  const handleShopkeeperSelect = (rowIndex, selectedOptionsForDropdown) => {
    setShopkeeperOption((prevSelectedOptions) => ({
      ...prevSelectedOptions,
      [rowIndex]: selectedOptionsForDropdown,
    }));
    setIsDirty(true);
  };

  const handleRemovalSelect = (rowIndex, selectedOptionsForDropdown) => {
    setRemovalType((prevSelectedOptions) => ({
      ...prevSelectedOptions,
      [rowIndex]: selectedOptionsForDropdown,
    }));
    setIsDirty(true);
  };

  const dropdownOptions = [
    { label: t("SLAUGHTER_IN_ABBATOIR"), value: "test1", id: 1 },
    { label: t("RELIGIOUS_PERSONAL_PURPOSE"), value: "test2", id: 2 },
    { label: t("SALSETTE_REMOVAL"), value: "test3", id: 3 },
  ];

  const isAfterStablingVisibleColumns = [
    { Header: t("Animal Type"), accessor: "animalType" },
    {
      Header: t("Animal Token"),
      accessor: "count",
      Cell: ({ row }) => {
        const animalType = row.original.animalType;
        const index = row.index;
        return generateTokenNumber(animalType, row.original.count);
      },
    },
    {
      accessor: "shopKeeperDetails",
      Header: t("Assign Shopkeeper"),
      Cell: ({ row }) => (
        <MultiColumnDropdown
          options={shopKeeper}
          selected={shopkeeperOption[row.index] || []}
          onSelect={(e, selected) => handleShopkeeperSelect(row.index, selected)}
          defaultLabel="Select Shopkeeper"
          displayKeys={["label", "licenceNumber", "mobileNumber"]}
          optionsKey="value"
          defaultUnit="Options"
          autoCloseOnSelect={true}
          showColumnHeaders={true}
          headerMappings={{
            label: t("Name"),
              licenceNumber: t("License"),
              mobileNumber: t("Mobile Number"),
          }}
        />
      ),
    },
    {
      accessor: "deonarRemovalTypeDetails",
      Header: t("Deonar Removal Type"),
      Cell: ({ row }) => {
        const filteredOptions =
          row.original.tradable && row.original.stable
            ? dropdownOptions
            : dropdownOptions.filter((option) => option.label === t("SLAUGHTER_IN_ABBATOIR"));

        return (
          <MultiColumnDropdown
            options={filteredOptions}
            selected={removalType[row.index] || []}
            onSelect={(e, selected) => handleRemovalSelect(row.index, selected)}
            defaultLabel="Select Removal Type"
            displayKeys={["label"]}
            optionsKey="value"
            defaultUnit="Options"
            autoCloseOnSelect={true}
            showColumnHeaders={true}
            headerMappings={{
              label: t("Type"),
            }}
          />
        );
      },
    },
  ];

  const onSubmit = async (formData) => {
    if (!isValid) {
      setToast({ key: "error", action: t("Please fill in all required fields.") });
      return;
    }
    try {
      const filteredAnimalAssignments = gawaltable
        .filter((animal) => animal.arrivalId === selectedUUID)
        .map((animal, index) => {
          const selectedShopkeeperArray = shopkeeperOption[index] || [];
          const selectedShopkeeper = selectedShopkeeperArray[0];

          const selectedRemovalTypeArray = removalType[index] || [];
          const selectedRemovalType = selectedRemovalTypeArray[0];

          const assignments = {};

          // Add shopkeeper and removal type details if selected
          if (selectedShopkeeper) {
            assignments.assignedStakeholder = selectedShopkeeper.value;
          }
          if (selectedRemovalType) {
            assignments.deonarRemovalType = selectedRemovalType.id; // <-- Change to use `id`
          }

          if (selectedShopkeeper || selectedRemovalType) {
            assignments.animalTypeId = animal.animalTypeId;
            assignments.token = animal.count;
          }

          return Object.keys(assignments).length > 0 ? assignments : null;
        })
        .filter((entry) => entry !== null);

      const payload = {
        ...formData,
        animalAssignments: filteredAnimalAssignments,
        arrivalId: selectedUUID,
      };

      saveStablingDetails(payload, {
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

  const applyGlobalShopkeeper = (shopkeeper) => {
    if (shopkeeper) {
      setShopkeeperOption(gawaltable.map(() => [shopkeeper]));
    }
  };

  const handleGlobalShopkeeperSelect = (selected) => {
    const selectedShopkeeper = selected[0] || null;
    setGlobalShopkeeper(selectedShopkeeper);

    if (isGlobalShopkeeperEnabled && selectedShopkeeper) {
      applyGlobalShopkeeper(selectedShopkeeper);
    }
    setIsDirty(true);
  };

  const handleGlobalShopkeeperToggle = (e) => {
    setIsGlobalShopkeeperEnabled(e.target.checked);

    if (e.target.checked) {
      setShowIndividualMessage(true);

      if (globalShopkeeper) {
        applyGlobalShopkeeper(globalShopkeeper);
        setIsDirty(true);
      }
    } else {
      setShowIndividualMessage(false);
      setShopkeeperOption(gawaltable.map(() => []));
      setIsDirty(false);
    }
  };

  const resetModal = () => {
    setSelectedUUID(undefined);
    setRemovalType([]);
    setGlobalShopkeeper([]);
    setShopkeeperOption([]);
    setIsGlobalShopkeeperEnabled(false);
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

  return (
    <React.Fragment>
      <div className="bmc-card-full">
        <form onSubmit={handleSubmit(onSubmit)}>
          <MainFormHeader title={t("DEONAR_TRADING")} />
          {/* <div className="bmc-row-card-header"> */}
          <div className="bmc-card-row" style={{overflowY:"auto", maxHeight:"511px"}}>
            {/* <StablingTypeOptionsField setStablingFormType={setStablingFormType} control={control} data={data} setData={setData} /> */}
            <div className="bmc-row-card-header">
              {isMobileView && animalCount.map((data, index) => <TableCard data={data} key={index} fields={fields} onUUIDClick={handleUUIDClick} />)}

              <CustomTable
                t={t}
                columns={columns(handleUUIDClick, t)}
                data={animalCount}
                manualPagination={false}
                tableClassName={"deonar-scrollable-table"}
                totalRecords={totalRecords}
                autoSort={false}
                isLoadingRows={isLoading}
              />
              {isModalOpen && (
                <CustomModal isOpen={isModalOpen} onClose={toggleModal} selectedUUID={selectedUUID} style={{ width: "100%" }}>
                  <Fragment>
                    <div className="bmc-card-row">
                      <div style={{ display: "flex", gap: "20px", justifyContent: "space-between", marginBottom: "40px" }}>
                        {/* <div style={{ display: "flex", gap: "20px", justifyContent: "space-between" }}> */}
                        <div style={{ marginBottom: "20px" }}>
                          <label style={{ display: "flex", alignItems: "center", gap: "10px", justifyContent: "center" }}>
                            <input type="checkbox" checked={isGlobalShopkeeperEnabled} onChange={handleGlobalShopkeeperToggle} />
                            <h3 style={{ fontWeight: "500", fontSize: "16px" }}>{t("Apply selected Shopkeeper for all animals")}</h3>
                          </label>
                          <MultiColumnDropdown
                            options={shopKeeper}
                            selected={globalShopkeeper ? [globalShopkeeper] : []}
                            onSelect={(e, selected) => handleGlobalShopkeeperSelect(selected)}
                            defaultLabel={t("Select Shopkeeper")}
                            displayKeys={["label", "licenceNumber", "mobileNumber"]}
                            optionsKey="value"
                            autoCloseOnSelect={true}
                            showColumnHeaders={true}
                            headerMappings={{
                              label: t("Name"),
                              licenceNumber: t("License"),
                              mobileNumber: t("Mobile Number"),
                            }}
                          />
                        </div>
                        <div className="no-uuid-message">
                          {showIndividualMessage && <p style={{ fontSize: "20px" }}>{t("Note - You can select the Stakeholders individually also.")}</p>}
                        </div>
                        {/* </div> */}
                      </div>
                      {/* <div className="bmc-row-card-header" style={{ overflowY: "auto", maxHeight: "300px" }}> */}
                      {/* {isMobileView && data.map((data, index) => <TableCard data={data} key={index} fields={fields} onUUIDClick={handleUUIDClick} />)} */}

                      <CustomTable
                        t={t}
                        columns={isAfterStablingVisibleColumns}
                        data={filteredGawaltable}
                        totalRecords={totalRecords}
                        autoSort={false}
                        manualPagination={false}
                        tableClassName={"deonar-scrollable-table"}
                        />
                      {/* </div> */}
                    </div>
                    <SubmitButtonField control={control} disabled={!isDirty} />
                    {/* <SubmitPrintButtonFields /> */}
                  </Fragment>
                </CustomModal>
              )}
            </div>{" "}
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

export default Trading;
