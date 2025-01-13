import React, { useEffect, useState } from "react";
import MainFormHeader from "../commonFormFields/formMainHeading";
import { useTranslation } from "react-i18next";
import { CardLabel, DatePicker, Dropdown, LabelFieldPair } from "@upyog/digit-ui-react-components";
import { Controller, useForm } from "react-hook-form";
import SearchButtonField from "../commonFormFields/searchBtn";
import ReceiverField from "../commonFormFields/receiverName";
import LicenseNumberField from "../commonFormFields/licenseNumber";
import useDeonarCommon from "@upyog/digit-ui-libraries/src/hooks/deonar/useCommonDeonar";
import CustomTable from "../commonFormFields/customTable";
import CustomModal from "../commonFormFields/customModal";
import AssignDateField from "../commonFormFields/assignDate";
import ArrivalDateField from "../commonFormFields/arrivalDate";
import MultiColumnDropdown from "../commonFormFields/multiColumnDropdown";

const StakeholderDetails = () => {
  const { t } = useTranslation();
  const [data, setData] = useState({});
  const [assignedStakeholder, setAssignedStakeholder] = useState();
  const [selectedAnimalDetails, setSelectedAnimalDetails] = useState([]);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [totalRecords, setTotalRecords] = useState(0);
  const [selectedStakeholder, setSelectedStakeholder] = useState(null);
  const [shopKeeper, setShopKeeper] = useState([]);
  const [globalShopkeeper, setGlobalShopkeeper] = useState(null);

  const {
    control,
    setValue,
    handleSubmit,
    getValues,
    watch,
    formState: { errors, isValid },
  } = useForm({ mode: "onChange" });
  const assignDate = watch("assignedFromDate");
  const { fetchDeonarCommon, fetchAssignedStakeholder,searchDeonarCommon } = useDeonarCommon();
  const useFetchOption = (optionType) => {
    const { data } = fetchDeonarCommon({
      CommonSearchCriteria: {
        Option: optionType,
      },
    });
    return data
      ? data.CommonDetails.map((item) => ({
          name: item.name,
          value: item.id,
        }))
      : [];
  };

  const stakeholderData = useFetchOption("stakeholder");
  const handleStakeholderSelect = (selected) => {
    setSelectedStakeholder(selected);
  };

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

  const shopkeeperData = useFetchOptions(selectedStakeholder?.name);

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

  const handleGlobalShopkeeperSelect = (selected) => {
    const selectedShopkeeper = selected[0] || null;
    setGlobalShopkeeper(selectedShopkeeper);
  };

  const response = fetchAssignedStakeholder();
  const handleSearch = () => {
    const formData = getValues();

    const payload = {
      StakeholderAssignedCriteria: {
        StakeholderName: formData.receiverName,
        MobileNumber: formData.receiverContact,
        LicenseNumber: formData.licenseNumber,
        StakeholderTypeId: formData.traderType?.value,
      },
    };
    response.mutate(payload, {
      onSuccess: (data) => {
        setAssignedStakeholder(data?.StakeholderAssignedDetails || []);
      },
      onError: (error) => {
        console.error("Error fetching assigned stakeholder:", error);
      },
    });
  };

  const handleStakeholderClick = (animalDetails) => {
    setSelectedAnimalDetails(animalDetails);
    setIsModalOpen(true);
  };

  const handleCloseModal = () => {
    setIsModalOpen(false); // Close modal
    setSelectedAnimalDetails([]);
  };
  const columns = [
    {
      Header: t("Arrival Id"),
      accessor: "ArrivalId",
      Cell: ({ row }) => (
        <span onClick={() => handleStakeholderClick(row.original.AnimalDetails)} style={{ cursor: "pointer", textDecoration: "underline" }}>
          {t(row.original.ArrivalId) || "N/A"}
        </span>
      ),
    },
    {
      Header: t("Stakeholder Name"),
      accessor: "StakeholderName",
    },
    {
      Header: t("Stakeholder Type"),
      accessor: "StakeholderType",
    },
    {
      Header: t("DD Reference"),
      accessor: "DDReference",
    },
    {
      Header: t("License Number"),
      accessor: "LicenceNumber",
    },
    {
      Header: t("Date"),
      accessor: "Date",
    },
    {
      Header: t("Time"),
      accessor: "Time",
    },
  ];

  const animalColumns = [
    {
      Header: t("Animal Type"),
      accessor: "AnimalType",
    },
    {
      Header: t("Token"),
      accessor: "Token",
    },
    {
      Header: t("Citizen Name"),
      accessor: "CitizenName",
    },
  ];
  return (
    <React.Fragment>
      <div className="bmc-card-full">
        <MainFormHeader title={t("Stakeholder Account Details")} />
        <div className="bmc-row-card-header">
          <div className="bmc-card-row">
            <div className="bmc-col3-card">
              <LabelFieldPair>
                <CardLabel className="bmc-label">{t("DEONAR_STACKHOLDER_TYPE")}</CardLabel>
                <Controller
                  control={control}
                  name="traderType"
                  rules={{ required: t("CORE_COMMON_REQUIRED_ERRMSG") }}
                  render={(props) => (
                    <div>
                      <Dropdown
                        name="traderType"
                        defaultValues={props.value}
                        selected={props.value}
                        select={(value) => {
                          props.onChange(value);
                          const newData = {
                            ...data,
                            traderType: value,
                          };
                          setData(newData);
                          handleStakeholderSelect(value);
                        }}
                        onBlur={props.onBlur}
                        option={stakeholderData}
                        optionKey="name"
                        placeholder={t("DEONAR_STACKHOLDER_TYPE")}
                      />
                    </div>
                  )}
                />
              </LabelFieldPair>
            </div>
            <div className="bmc-col3-card">
              <CardLabel>{t("DEONAR_NAME")}</CardLabel>
              <MultiColumnDropdown
                options={shopKeeper}
                selected={globalShopkeeper ? [globalShopkeeper] : []}
                onSelect={(e, selected) => handleGlobalShopkeeperSelect(selected)}
                defaultLabel={t("Select Shopkeeper")}
                displayKeys={["label","mobileNumber"]}
                optionsKey="value"
                autoCloseOnSelect={true}
                showColumnHeaders={true}
                headerMappings={{
                  label: t("Name"),
                  mobileNumber: t("Mobile Number"),
                }}
              />
            </div>
            <div className="bmc-col3-card">
              <LabelFieldPair>
                <CardLabel className="bmc-label">{t("Assigned From Date")}</CardLabel>
                <Controller
                  control={control}
                  name="assignedFromDate"
                  rules={{ required: t("CORE_COMMON_REQUIRED_ERRMSG") }}
                  render={(props) => (
                    <div>
                      <DatePicker
                        date={props.value}
                        onChange={(e) => {
                          props.onChange(e);
                          const toDate = watch("assignedToDate");
                          if (toDate && new Date(toDate) < new Date(e)) {
                            setValue("assignedToDate", null);
                          }
                        }}
                        onBlur={props.onBlur}
                        placeholder={t("DEONAR_ASSIGN_FROM_DATE")}
                      />
                    </div>
                  )}
                />
              </LabelFieldPair>
            </div>
            <div className="bmc-col3-card">
              <LabelFieldPair>
                <CardLabel className="bmc-label">{t("Assigned To Date")}</CardLabel>
                <Controller
                  control={control}
                  name="assignedToDate"
                  rules={{
                    required: t("CORE_COMMON_REQUIRED_ERRMSG"),
                    validate: (value) => {
                      if (!assignDate) return true;
                      const fromDate = new Date(assignDate);
                      const toDate = new Date(value);
                      return toDate >= fromDate || true; // Always return true to prevent error message
                    }
                  }}
                  render={(props) => (
                    <div>
                      <DatePicker
                        date={props.value}
                        onChange={props.onChange}
                        onBlur={props.onBlur}
                        placeholder={t("DEONAR_ASSIGN_TO_DATE")}
                        min={assignDate ? new Date(assignDate).toISOString().split("T")[0] : undefined}
                        disable={!assignDate}
                      />
                    </div>
                  )}
                />
              </LabelFieldPair>
            </div>
          </div>
          <div className="bmc-card-row" style={{ display: "flex", justifyContent: "flex-end", marginTop: "-42px" }}>
            <SearchButtonField onSearch={handleSearch} disabled={!isValid} />
          </div>
        </div>
        <div className="bmc-row-card-header">
          <div className="bmc-card-row" style={{ overflowY: "auto", maxHeight: "511px" }}>
            {assignedStakeholder?.length > 0 && (
              <CustomTable
                t={t}
                columns={columns}
                data={assignedStakeholder}
                pageSizeLimit={10}
                searchPlaceholder={t("Search")}
                manualPagination={false}
              />
            )}
          </div>
        </div>
      </div>
      {isModalOpen && (
        <div className="bmc-card-row">
          <CustomModal
            isOpen={isModalOpen}
            onClose={handleCloseModal}
            // selectedUUID={selectedUUID}
            style={{ width: "100%" }}
            tableClassName={"ebe-custom-scroll"}
          >
            <div className="bmc-card-row" style={{ overflowY: "auto", maxHeight: "511px" }}>
              <CustomTable
                t={t}
                columns={animalColumns}
                manualPagination={false}
                data={selectedAnimalDetails}
                totalRecords={totalRecords}
                // tableClassName={"deonar-scrollable-table"}
                autoSort={false}
                isLoadingRows={false}
              />
            </div>
          </CustomModal>
        </div>
      )}
    </React.Fragment>
  );
};
export default StakeholderDetails;
