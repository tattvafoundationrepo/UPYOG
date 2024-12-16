import React, { Fragment, useEffect, useMemo, useState } from "react";
import { useTranslation } from "react-i18next";
import { Controller, useForm } from "react-hook-form";
import MainFormHeader from "../commonFormFields/formMainHeading";
import useSubmitForm from "../../../hooks/useSubmitForm";
import { COLLECTION_POINT_ENDPOINT } from "../../../constants/apiEndpoints";
import { helkariColumns, generateTokenNumber } from "./utils";
import useDeonarCommon from "@upyog/digit-ui-libraries/src/hooks/deonar/useCommonDeonar";
import CustomModal from "../commonFormFields/customModal";
import CustomTable from "../commonFormFields/customTable";
import TableCard from "../commonFormFields/tableCard";
import MultiColumnDropdown from "../commonFormFields/multiColumnDropdown";
import SubmitButtonField from "../commonFormFields/submitBtn";
import { Toast } from "@upyog/digit-ui-react-components";
import ConfirmationDialog from "../commonFormFields/confirmationDialog";

const S = () => {
  const { t } = useTranslation();

  const [defaults, setDefaults] = useState({});
  const [selectedUUID, setSelectedUUID] = useState();
  const [animalCount, setAnimalCount] = useState([]);
  const [shopKeeper, setShopKeeper] = useState([]);

  const [gawaltable, setGawaltable] = useState([]);
  const [dawanwala, setDawanwala] = useState([]);
  //const [selectedOption, setSelectedOption] = useState([]);
  const [dawanwalaOption, setDawanwalaOption] = useState([]);

  const [totalRecords, setTotalRecords] = useState(0);

  const [isModalOpen, setIsModalOpen] = useState(false);

  const [isMobileView, setIsMobileView] = useState(window.innerWidth < 768);

  const [globalDawanwala, setGlobalDawanwala] = useState(null);

  const [isGlobalDawanwalaEnabled, setIsGlobalDawanwalaEnabled] = useState(false);

  const [toast, setToast] = useState(null);
  const [showIndividualMessage, setShowIndividualMessage] = useState(false);
  const [isDirty, setIsDirty] = useState(false);
  const [isConfirmModalOpen, setIsConfirmModalOpen] = useState(false);

  const {
    control,
    setValue,
    handleSubmit,
    getValues,
    formState: { errors, isValid, isDirty: formIsDirty },
  } = useForm({ defaultValues: defaults, mode: "onChange", shouldUnregister: true });

  const { searchDeonarCommon, saveStablingDetails, fetchDawanwalaList } = useDeonarCommon();
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

  const { data: fetchedData, isLoading } = fetchDawanwalaList();

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
          arrivalId: detail.ddreference,
          animalTypeId: animal.animalTypeId,
          animalType: animal.animalType,
          count: animal.token,
        }))
      );
      setGawaltable(mappedAnimalCount);
      //console.log("mappedAnimalCount",gawaltable);
    }
  }, [fetchedData]);

  const filteredGawaltable = gawaltable.filter((animal) => animal.arrivalId === selectedUUID);

  const handleDawanwalaSelect = (rowIndex, selectedOptionsForDropdown) => {
    setDawanwalaOption((prevSelectedOptions) => ({
      ...prevSelectedOptions,
      [rowIndex]: selectedOptionsForDropdown,
    }));
    setIsDirty(true);
  };

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
      accessor: "dawanWalaDetails",
      Header: t("Assign Dawanwala"),
      Cell: ({ row }) => (
        <MultiColumnDropdown
          options={dawanwala}
          selected={dawanwalaOption[row.index] || []}
          onSelect={(e, selected) => handleDawanwalaSelect(row.index, selected)}
          defaultLabel={t("Select Dawanwala")}
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
  ];

  const onSubmit = async (formData) => {
    try {
      const filteredAnimalAssignments = gawaltable
        .filter((animal) => animal.arrivalId === selectedUUID)
        .map((animal, index) => {
          const selectedDawanwalaArray = dawanwalaOption[index] || [];
          const selectedDawanwala = selectedDawanwalaArray[0];
          const assignments = [];
          if (selectedDawanwala) {
            assignments.push({
              animalTypeId: animal.animalTypeId,
              token: animal.count,
              assignedStakeholder: selectedDawanwala.value,
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
  const handleGlobalDawanwalaToggle = (e) => {
    setIsGlobalDawanwalaEnabled(e.target.checked);

    if (e.target.checked) {
      setShowIndividualMessage(true);
      if (globalDawanwala) {
        applyGlobalDawanwala(globalDawanwala);
        setIsDirty(true);
      }
    } else {
      setShowIndividualMessage(false);
      setDawanwalaOption(gawaltable.map(() => []));
      setIsDirty(false);
    }
  };

  const applyGlobalDawanwala = (dawanwala) => {
    setDawanwalaOption(gawaltable.map(() => [dawanwala]));
  };

  const handleGlobalDawanwalaSelect = (selected) => {
    const selectedDawanwala = selected[0] || null;
    setGlobalDawanwala(selectedDawanwala);

    if (isGlobalDawanwalaEnabled && selectedDawanwala) {
      applyGlobalDawanwala(selectedDawanwala);
      setIsDirty(true);
    }
  };

  const applicableDawanwala = useMemo(() => {
    const allAnimalTypesInTable = gawaltable.map((row) => row.animalType.toLowerCase());
    return dawanwala.filter((dawanwala) =>
      dawanwala.animalTypes
        .toLowerCase()
        .split(", ")
        .some((animalType) => allAnimalTypesInTable.includes(animalType))
    );
  }, [gawaltable, dawanwala]);

  const resetModal = () => {
    setSelectedUUID(undefined);
    setIsGlobalDawanwalaEnabled(false);
    setDawanwalaOption([]);
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
          <div className="bmc-card-row">
            <div className="bmc-row-card-header">
              {isMobileView && animalCount.map((data, index) => <TableCard data={data} key={index} fields={fields} onUUIDClick={handleUUIDClick} />)}
              <CustomTable
                t={t}
                columns={helkariColumns(handleUUIDClick, t)}
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
                      {/* <div
                        className="bmc-row-card-header"
                        style={{ display: "flex", gap: "20px", justifyContent: "space-between", padding: "13px 1rem" }}
                      > */}
                      <div>
                        <div style={{ display: "flex", gap: "20px", justifyContent: "space-between", marginBottom: "40px" }}>
                          <div style={{ marginBottom: "20px" }}>
                            <label style={{ display: "flex", alignItems: "center", gap: "10px", justifyContent: "center" }}>
                              <input type="checkbox" checked={isGlobalDawanwalaEnabled} onChange={handleGlobalDawanwalaToggle} />
                              <h3 style={{ fontWeight: "500", fontSize: "16px" }}>{t("Apply selected Dawanwala for all animals")}</h3>
                            </label>
                            <MultiColumnDropdown
                              options={applicableDawanwala}
                              selected={globalDawanwala ? [globalDawanwala] : []}
                              onSelect={(e, selected) => handleGlobalDawanwalaSelect(selected)}
                              defaultLabel={t("Select Dawanwala")}
                              displayKeys={["label", "licenceNumber", "mobileNumber"]}
                              optionsKey="value"
                              autoCloseOnSelect={true}
                              headerMappings={{
                                label: t("Name"),
                                licenceNumber: t("License"),
                                mobileNumber: t("Mobile Number"),
                              }}
                            />
                          </div>
                          <div className="no-uuid-message">
                            {showIndividualMessage && (
                              <p style={{ fontSize: "20px" }}>{t("Note - You can select the Stakeholders individually also.")}.</p>
                            )}
                          </div>
                        </div>
                      </div>
                      {/* </div> */}

                      {/* <div className="bmc-row-card-header" style={{ overflowY: "auto", maxHeight: "300px" }}> */}
                      {/* {isMobileView && data.map((data, index) => <TableCard data={data} key={index} fields={fields} onUUIDClick={handleUUIDClick} />)} */}
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
                          showPdfDownload={true}
                          showExcelDownload={true}
                          pdfButtonText="Download PDF"
                          excelButtonText="Download Excel"
                          customDownloadButtonStyles={{
                            fontSize: "14px",
                            fontWeight: "bold",
                          }}
                        />
                      </div>
                      {/* </div> */}
                    </div>
                    <SubmitButtonField control={control} disabled={!isDirty} />
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

export default S;
