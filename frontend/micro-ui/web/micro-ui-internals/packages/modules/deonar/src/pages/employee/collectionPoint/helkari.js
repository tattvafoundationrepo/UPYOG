import React, { Fragment, useEffect, useMemo, useState } from "react";
import { useTranslation } from "react-i18next";
import { useForm } from "react-hook-form";
import MainFormHeader from "../commonFormFields/formMainHeading";
import { columns, generateTokenNumber } from "./utils";
import useDeonarCommon from "@upyog/digit-ui-libraries/src/hooks/deonar/useCommonDeonar";
import CustomModal from "../commonFormFields/customModal";
import CustomTable from "../commonFormFields/customTable";
import TableCard from "../commonFormFields/tableCard";
import MultiColumnDropdown from "../commonFormFields/multiColumnDropdown";
import SubmitButtonField from "../commonFormFields/submitBtn";
import { Toast } from "@upyog/digit-ui-react-components";
import ConfirmationDialog from "../commonFormFields/confirmationDialog";

const Helkari = () => {
  const { t } = useTranslation();

  const [defaults, setDefaults] = useState({});
  const [selectedUUID, setSelectedUUID] = useState();
  const [animalCount, setAnimalCount] = useState([]);
  const [shopKeeper, setShopKeeper] = useState([]);

  const [gawaltable, setGawaltable] = useState([]);
  const [helkari, setHelkari] = useState([]);
  const [helkariOption, setHelkariOption] = useState([]);

  const [totalRecords, setTotalRecords] = useState(0);

  const [isModalOpen, setIsModalOpen] = useState(false);

  const [isMobileView, setIsMobileView] = useState(window.innerWidth < 768);

  const [isGlobalHelkariEnabled, setIsGlobalHelkariEnabled] = useState(false);
  const [globalHelkari, setGlobalHelkari] = useState(null);

  const [toast, setToast] = useState(null);
  const [showIndividualMessage, setShowIndividualMessage] = useState(false);
  const [isDirty, setIsDirty] = useState(false);
  const [isConfirmModalOpen, setIsConfirmModalOpen] = useState(false);

  const {
    control,
    handleSubmit,
    formState: { errors, isValid },
  } = useForm({ defaultValues: defaults, mode: "onChange" });

  const { searchDeonarCommon, saveStablingDetails, fetchHelkariList } = useDeonarCommon();
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

  const helkariData = useFetchOptions("helkari");
  const shopkeeperData = useFetchOptions("shopkeeper");

  const { data: fetchedData, isLoading } = fetchHelkariList();

  useEffect(() => {
    if (fetchedData) {
      setAnimalCount(fetchedData.SecurityCheckDetails);
      setTotalRecords();
    }
  }, [fetchedData]);

  useEffect(() => {
    if (helkariData.length) {
      const combinedData = helkariData.map((item) => {
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
      if (JSON.stringify(combinedData) !== JSON.stringify(helkari)) {
        setHelkari(combinedData);
      }
    }
  }, [helkariData, helkari]);

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
    { key: "entryUnitId", label: "Arrival UUID", isClickable: true },
    { key: "traderName", label: "Trader Name" },
    { key: "licenceNumber", label: "License Number" },
    { key: "vehicleNumber", label: "Vehicle Number" },
    { key: "dateOfArrival", label: "Arrival Date" },
    { key: "timeOfArrival", label: "Arrival Time" },
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
        }))
      );
      setGawaltable(mappedAnimalCount);
    }
  }, [fetchedData]);

  const filteredGawaltable = gawaltable.filter((animal) => animal.arrivalId === selectedUUID);

  const handleHelkariSelect = (rowIndex, selectedOptionsForDropdown) => {
    setHelkariOption((prevSelectedOptions) => ({
      ...prevSelectedOptions,
      [rowIndex]: selectedOptionsForDropdown,
    }));
    setIsDirty(true);
  };

  const isAfterStablingVisibleColumns = [
    { Header: "Animal Type", accessor: "animalType" },
    {
      Header: "Animal Token",
      accessor: "count",
      Cell: ({ row }) => {
        const animalType = row.original.animalType;
        const index = row.index;
        return generateTokenNumber(animalType, row.original.count);
      },
    },
    {
      accessor: "helkariDetails",
      Header: "Assign Helkari",
      Cell: ({ row }) => (
        <MultiColumnDropdown
          options={helkari} // Use appropriate options for Helkari
          selected={helkariOption[row.index] || []}
          onSelect={(e, selected) => handleHelkariSelect(row.index, selected)}
          defaultLabel="Select Helkari"
          displayKeys={["label", "licenceNumber", "mobileNumber"]}
          optionsKey="value"
          defaultUnit="Options"
          autoCloseOnSelect={true}
          showColumnHeaders={true}
          headerMappings={{
            label: "Name",
            licenceNumber: "License",
            mobileNumber: "Mobile Number",
          }}
        />
      ),
    },
  ];

  const onSubmit = async (formData) => {
    try {
      const filteredAnimalAssignments = gawaltable
        .filter((animal) => animal.arrivalId === selectedUUID)
        .map((animal, index) => {
          const selectedHelkariArray = helkariOption[index] || [];
          const selectedHelkari = selectedHelkariArray[0];
          const assignments = [];
          if (selectedHelkari) {
            assignments.push({
              animalTypeId: animal.animalTypeId,
              token: animal.count,
              assignedStakeholder: selectedHelkari.value,
            });
          }
          return assignments.length > 0 ? assignments : null;
        })
        .filter((entry) => entry !== null)
        .flat();
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

  const handleGlobalHelkariToggle = (e) => {
    setIsGlobalHelkariEnabled(e.target.checked);

    if (e.target.checked) {
      setShowIndividualMessage(true);
      if (globalHelkari) {
        applyGlobalHelkari(globalHelkari);
      }
    } else {
      setShowIndividualMessage(false);
      setHelkariOption(gawaltable.map(() => []));
    }
  };

  const applyGlobalHelkari = (helkari) => {
    setHelkariOption(gawaltable.map(() => [helkari]));
  };

  const handleGlobalHelkariSelect = (selected) => {
    const selectedHelkari = selected[0] || null;
    setGlobalHelkari(selectedHelkari);

    if (isGlobalHelkariEnabled && selectedHelkari) {
      applyGlobalHelkari(selectedHelkari);
    }
  };

  const applicableHelkari = useMemo(() => {
    const allAnimalTypesInTable = gawaltable.map((row) => row.animalType.toLowerCase());
    return helkari.filter((helkari) =>
      helkari.animalTypes
        .toLowerCase()
        .split(", ")
        .some((animalType) => allAnimalTypesInTable.includes(animalType))
    );
  }, [gawaltable, helkari]);

  const resetModal = () => {
    setSelectedUUID(undefined);
    setHelkariOption([]);
    setIsGlobalHelkariEnabled(false)
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
          <MainFormHeader title={"DEONAR_HELKARI"} />
          <div className="bmc-card-row">
            <div className="bmc-row-card-header">
              {isMobileView && animalCount.map((data, index) => <TableCard data={data} key={index} fields={fields} onUUIDClick={handleUUIDClick} />)}
              <CustomTable
                t={t}
                columns={columns(handleUUIDClick)}
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
                      <div className="bmc-col3-card">
                        <div style={{ display: "flex", gap: "20px", justifyContent: "space-between", marginBottom: "40px" }}>
                          <div style={{ flex: 1, marginBottom: "20px" }}>
                            <label style={{ display: "flex", gap: "10px" }}>
                              <input type="checkbox" checked={isGlobalHelkariEnabled} onChange={handleGlobalHelkariToggle} />
                              <h3 style={{ fontWeight: "500", fontSize: "16px" }}>Apply selected Helkari for all animals</h3>
                            </label>
                            <MultiColumnDropdown
                              options={applicableHelkari} // Ensure you have this data populated
                              selected={globalHelkari ? [globalHelkari] : []}
                              onSelect={(e, selected) => handleGlobalHelkariSelect(selected)}
                              defaultLabel="Select Helkari"
                              displayKeys={["label", "licenceNumber", "mobileNumber"]}
                              optionsKey="value"
                              autoCloseOnSelect={true}
                              headerMappings={{
                                label: "Name",
                                licenceNumber: "License",
                                mobileNumber: "Mobile Number",
                              }}
                            />
                          </div>
                        </div>
                      </div>

                      <div className="no-uuid-message" style={{ flex: 1 }}>
                        {showIndividualMessage && <p style={{ fontSize: "20px" }}>Note - You can select the Helkari individually also.</p>}
                      </div>
                    </div>
                    <div className="bmc-card-row">
                      <CustomTable
                        t={t}
                        columns={isAfterStablingVisibleColumns}
                        data={filteredGawaltable}
                        totalRecords={totalRecords}
                        autoSort={false}
                        manualPagination={false}
                        tableClassName={"deonar-scrollable-table"}
                        isLoadingRows={isLoading}
                      />
                    </div>
                    <SubmitButtonField control={control} />
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
          label={t(toast.key === "success" ? "TRADING_DATA_SAVED_SUCCESSFULLY" : toast.action)}
          onClose={() => setToast(null)}
          style={{ maxWidth: "670px" }}
        />
      )}
    </React.Fragment>
  );
};

export default Helkari;
