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

const CitizenAssignment = () => {
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

  const {
    control,
    setValue,
    handleSubmit,
    getValues,
    formState: { errors, isValid },
  } = useForm({ defaultValues: defaults, mode: "onChange" });

  const { fetchTradingList, searchDeonarCommon, saveStablingDetails } = useDeonarCommon();
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

  const dropdownOptions = [{ label: t("RELIGIOUS_PERSONAL_PURPOSE"), value: "test2", id: 2 }];

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
      Header: t("Assign Citizen"),
      Cell: ({ row }) => (
        <MultiColumnDropdown
          options={shopKeeper}
          selected={shopkeeperOption[row.index] || []}
          onSelect={(e, selected) => handleShopkeeperSelect(row.index, selected)}
          defaultLabel="Select Citizen"
          displayKeys={["label", "mobileNumber"]}
          optionsKey="value"
          defaultUnit="Options"
          autoCloseOnSelect={true}
          showColumnHeaders={true}
          headerMappings={{
            label: t("Name"),
            mobileNumber: t("Mobile Number"),
          }}
        />
      ),
    },
    {
      accessor: "deonarRemovalTypeDetails",
      Header: t("Deonar Removal Type"),
      Cell: ({ row }) => {
        return (
          <MultiColumnDropdown
            options={dropdownOptions}
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

  return (
    <React.Fragment>
      <div className="bmc-card-full">
        <form onSubmit={handleSubmit(onSubmit)}>
          <MainFormHeader title={t("DEONAR_CITIZEN_ASSIGNMENT")} />
          <div className="bmc-card-row">
            <div className="bmc-row-card-header">
              {isMobileView && animalCount.map((data, index) => <TableCard data={data} key={index} fields={fields} onUUIDClick={handleUUIDClick} />)}
              <div className="bmc-card-row" style={{ overflowY: "auto", maxHeight: "511px" }}>

                <CustomTable
                  t={t}
                  columns={CitizenColumns(handleUUIDClick, t)}
                  data={animalCount}
                  manualPagination={false}
                  // tableClassName={"deonar-scrollable-table"}
                  totalRecords={totalRecords}
                  autoSort={false}
                  isLoadingRows={isLoading}
                />
              </div>
              {isModalOpen && (
                <CustomModal isOpen={isModalOpen} onClose={toggleModal} selectedUUID={selectedUUID} style={{ width: "100%" }}>
                  <Fragment>
                    <div className="bmc-card-row" style={{ overflowY: "auto", maxHeight: "511px" }}>
                      <div style={{ display: "flex", gap: "20px", justifyContent: "space-between", marginBottom: "40px" }}></div>
                      <CustomTable
                        t={t}
                        columns={isAfterStablingVisibleColumns}
                        data={filteredGawaltable}
                        totalRecords={totalRecords}
                        autoSort={false}
                        manualPagination={false}
                      // tableClassName={"deonar-scrollable-table"}
                      />
                    </div>
                    <SubmitButtonField control={control} disabled={!isDirty} />
                  </Fragment>
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

export default CitizenAssignment;
