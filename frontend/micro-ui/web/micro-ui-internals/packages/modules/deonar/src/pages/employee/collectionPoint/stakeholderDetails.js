import React, { useState } from "react";
import MainFormHeader from "../commonFormFields/formMainHeading";
import { useTranslation } from "react-i18next";
import { CardLabel, Dropdown, LabelFieldPair } from "@upyog/digit-ui-react-components";
import { Controller, useForm } from "react-hook-form";
import SearchButtonField from "../commonFormFields/searchBtn";
import ReceiverField from "../commonFormFields/receiverName";
import LicenseNumberField from "../commonFormFields/licenseNumber";
import useDeonarCommon from "@upyog/digit-ui-libraries/src/hooks/deonar/useCommonDeonar";
import CustomTable from "../commonFormFields/customTable";
import CustomModal from "../commonFormFields/customModal";

const StakeholderDetails = () => {
  const { t } = useTranslation();
  const [data, setData] = useState({});
  const [assignedStakeholder, setAssignedStakeholder] = useState();
  const [selectedAnimalDetails, setSelectedAnimalDetails] = useState([]); 
  const [isModalOpen, setIsModalOpen] = useState(false);
const [totalRecords, setTotalRecords] = useState(0);
  const { fetchDeonarCommon,fetchAssignedStakeholder } = useDeonarCommon();
  const useFetchOptions = (optionType) => {
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

  const stakeholderData = useFetchOptions("stakeholder");
  const {
    control,
    setValue,
    handleSubmit,
    getValues,
    formState: { errors, isValid },
  } = useForm({ mode: "onChange" });

  console.log(stakeholderData, "sssssssssssssssssssssssss");
  const response =  fetchAssignedStakeholder();
  

  const handleSearch =  () => {
    const formData = getValues();
    const payload = {
      StakeholderAssignedCriteria: {
        StakeholderName: formData.receiverName, 
        MobileNumber: formData.receiverContact,
        LicenseNumber: formData.licenseNumber, 
        StakeholderTypeId: formData.traderType?.value,
      },
    };
    console.log("Payload to Send:", payload);
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
        <MainFormHeader title={t("DEONAR_STAKEHOLDER_DETAILS")} />
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
            <ReceiverField control={control} setData={setData} data={data} label={t("DEONAR_RECEIVERNAME")} name={"receiverName"} />
            <LicenseNumberField control={control} setData={setData} data={data} />
            <ReceiverField control={control} setData={setData} data={data} label={t("DEONAR_RECEIVERCONTACT")} name={"receiverContact"} />
            <SearchButtonField onSearch={handleSearch}/>
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
