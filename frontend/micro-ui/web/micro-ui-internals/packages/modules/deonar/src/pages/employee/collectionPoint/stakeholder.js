import React, { use, useMemo } from "react";
import CustomTable from "@tattvafoundation/digit-ui-module-bmc/src/components/CustomTable";
import useBMCCommon from "@upyog/digit-ui-libraries/src/hooks/bmc/useBMCCommon";
import useDeonarCommon from "@upyog/digit-ui-libraries/src/hooks/deonar/useCommonDeonar";
import { useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import CustomModal from "../commonFormFields/customModal";
import { CardLabel, LabelFieldPair, Dropdown, TextInput, Toast } from "@upyog/digit-ui-react-components";
import { Controller, useForm } from "react-hook-form";
import ReceiverField from "../commonFormFields/receiverName";
import LicenseNumberField from "../commonFormFields/licenseNumber";
import ReferenceNumberField from "../commonFormFields/referenceNumber";
import EmailInputField from "../commonFormFields/EmailInputField";
import PhoneNumberField from "../commonFormFields/PhoneNumberField";
import PinCodeField from "../commonFormFields/PinCodeField";
import MainFormHeader from "../commonFormFields/formMainHeading";
import { add } from "lodash";
const Stakeholder = () => {
  const { t } = useTranslation();
  const [totalRecords, setTotalRecords] = useState(0);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [getStakeholderDetails, setStakeholderDetails] = useState([]);
  const [isEditing, setIsEditing] = useState(false);
  const [toast, setToast] = useState("");
  const [data, setData] = useState({});
  const [options, setOptions] = useState([]);
  const [animal, setAnimal] = useState([]);
  const [selectedOption, setSelectedOption] = useState([]);
  const { saveStakeholderDetails } = useDeonarCommon({});
  const { searchStakeholder } = useDeonarCommon();
  const { data: stakeData } = searchStakeholder();
  const initialDefaultValues = useMemo(() => ({
      stakeholderName: "",
      stakeholderTypeId: "",
      animalTypeIds: [],
      referenceNumber: "",
      licenseNumber: [],
      mobileNumber: "",
      address1: "",
      address2: "",
      pincode: "",
      email: " ",
    }),
    []
  );
 
  const {
    control,
    handleSubmit,
    reset,
    formState: { errors, isValid, dirtyFields },
  } = useForm({
    defaultValues: initialDefaultValues,
     mode: "onChange",
  });


  useEffect(() => {
    if (stakeData) {
      const StakeholderData = stakeData.StakeholderCheckDetails.map((detail) => ({
        name: detail.stakeholdername || "N/A",
        address: detail.address || "N/A",
        licenceNumber: detail.licencenumber || "N/A",
        animalType: detail.animaltype || "N/A",
        mobileNumber: detail.mobilenumber || "N/A",
        email: detail.email || "N/A",
        stakeholderType: detail.stakeholdertype|| "N/A", 
        registrationNumber: detail.registrationnumber || "N/A",
        pincode: detail.pincode || "N/A",
        address1: detail.address1 || "N/A",
        address2: detail.address2 || "N/A",
      }));
      setStakeholderDetails(StakeholderData);
    }
  }, [stakeData]);
  

  const handleRowClick = (rowData) => {
    setData(rowData);
    reset({
      stakeholderName: rowData.name,
      stakeholderTypeId: rowData.tradertype,
      animalTypeIds: rowData.animalType,
      referenceNumber: rowData.registrationNumber,
      licenseNumber: rowData.licenceNumber,
      mobileNumber: rowData.mobileNumber,
      address1: rowData.address1,
      address2: rowData.address2,
      pincode: rowData.pincode,
      email: rowData.email,
    });
    setIsModalOpen(true);
  };

  const visibleColumns = [
    {
      Header: t("Name"),
      accessor: "name",
      Cell: ({ row }) => (
        <span style={{ cursor: "pointer" }} onClick={() => handleRowClick(row.original)}>
          {t(row.original.name) || "N/A"}
        </span>
      ),
    },
    {
      Header: t("Stakeholder Name"),
      accessor: "stakeholderType",
      Cell: ({ row }) => t(row.original.stakeholderType) || "N/A",
    },
    {
      Header: t("Animal Type"),
      accessor: "animalType",
      Cell: ({ row }) => t(row.original.animalType) || "N/A",
    },
    {
      Header: t("Mobile Number"),
      accessor: "mobileNumber",
      Cell: ({ row }) => t(row.original.mobileNumber) || "N/A",
    },
    {
      Header: t("Licence Number"),
      accessor: "licenceNumber",
      Cell: ({ row }) => t(row.original.licenceNumber) || "N/A",
    },
    {
      Header: t("Registration Number"),
      accessor: "registrationNumber",
      Cell: ({ row }) => t(row.original.registrationNumber) || "N/A",
    },
    {
      Header: t("Email"),
      accessor: "email",
      Cell: ({ row }) => t(row.original.email) || "N/A",
    },
    {
      Header: t("Pincode"),
      accessor: "pincode",
      Cell: ({ row }) => t(row.original.pincode) || "N/A",
    },
    {
      Header: t("Address 1"),
      accessor: "address1",
      Cell: ({ row }) => t(row.original.address1) || "N/A",
    },
    {
      Header: t("Address 2"),
      accessor: "address2",
      Cell: ({ row }) => t(row.original.address2) || "N/A",
    },
  ];

  const handleAddEmployee = () => {
    setIsModalOpen(!isModalOpen);
  };
  const myConfig = {
    elements: [
      {
        label: { heading: t("BMC_ADD_QUALIFICATION"), cancel: "Cancel", submit: "Submit" },
        type: "p",
        text: t("Add Stakeholder"),
        style: { textDecoration: "underline", cursor: "pointer" },
        onClick: "onAddDocumentClick",
      },
    ],
  };

  const { fetchDeonarCommon } = useDeonarCommon();
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
  const stakeholderAnimal = useFetchOptions("animal");

  const toggleModal = () => {
    setIsModalOpen(!isModalOpen);
  };

  const onSubmit = async (formData) => {
    const payload = {
      Stakeholders: {
        stakeholderName: formData.stakeholderName || "",
        address1: formData.address1 || "",
        address2: formData.address2 || "",
        pincode: Number.isInteger(parseInt(formData.pincode, 10)) ? parseInt(formData.pincode, 10) : 0,
        mobileNumber: Number.isInteger(parseInt(formData.mobileNumber, 10)) ? parseInt(formData.mobileNumber, 10) : 0,
        contactNumber: Number.isInteger(parseInt(formData.contactNumber, 10)) ? parseInt(formData.contactNumber, 10) : 0,
        email: formData.email || "",
        stakeholderTypeId: formData.traderType?.value,
        animalTypeIds: Array.isArray(formData.animalType?.value)
          ? formData.animalType.value
          : formData.animalType?.value
          ? [formData.animalType.value]
          : [],
        licenceNumbers: Array.isArray(formData.licenseNumber) ? formData.licenseNumber : formData.licenseNumber ? [formData.licenseNumber] : [],

        registrationNumber: formData?.referenceNumber || undefined,
        validfromdate: formData.validfromdate || 1735211747272,
        validtodate: formData.validtodate || 1795211680468,
      },
    };
    saveStakeholderDetails.mutate(payload, {
      onSuccess: () => {
         setToastWithTimeout("success", t("DEONAR_STAKEHOLDER_DATA_SAVED_SUCCESSFULY"));

        setIsModalOpen(false);
        reset(initialDefaultValues);
      },
      onError: () => {
         setToastWithTimeout("error", t("DEONAR_STAKEHOLDER_DATA_NOT_SAVED_SUCCESSFULY"));
      },
    });
  };
  const setToastWithTimeout = (key, action) => {
    setToast({ key, action });

    setTimeout(() => {
      setToast(null);
    }, 3000);
  };

  return (
    <React.Fragment>
      <div className="bmc-card-full">
        <form onSubmit={handleSubmit(onSubmit)}>
          <MainFormHeader title={"DEONAR_STAKEHOLDER"} />
          {/* <div className="bmc-row-card-header"> */}
          <div className="bmc-card-row"></div>
          <div className="bmc-row-card-header">
            <CustomTable
              t={t}
              pageSizeLimit={10}
              columns={visibleColumns}
              data={getStakeholderDetails || []}
              manualPagination={false}
              totalRecords={getStakeholderDetails?.length}
              onAddEmployeeClick={handleAddEmployee}
              handleRowClick={handleRowClick}
              config={myConfig}
              sortParams={{}}
              tableClassName={"ebe-custom-scroll"}
              showSearch={true}
              showText={true}
            />
            <CustomModal
              isOpen={isModalOpen}
              onClose={() => {
                toggleModal();
                // setIsEditing(false);
                 reset(initialDefaultValues);
              }}
              title={t("DEONAR_ADD_STAKEHOLDER")}
            >
              <div className="bmc-card-row">
                <div className="bmc-col3-card">
                  <LabelFieldPair>
                    <CardLabel className="bmc-label">{t("Stakeholder_Type")}</CardLabel>
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
                            t={t}
                            placeholder={t("DEONAR_STACKHOLDER_TYPE")}
                          />
                        </div>
                      )}
                    />
                  </LabelFieldPair>
                </div>
                <div className="bmc-col3-card">
                  <LabelFieldPair>
                    <CardLabel className="bmc-label">{t("Deonar_Animal_Type")}</CardLabel>
                    <Controller
                      control={control}
                      name="animalType"
                      rules={{ required: t("CORE_COMMON_REQUIRED_ERRMSG") }}
                      render={(props) => (
                        <div>
                          <Dropdown
                            name="animalType"
                            selected={props.value}
                            select={(value) => {
                              props.onChange(value);
                              const newData = {
                                ...data,
                                animalType: value,
                              };
                              setData(newData);
                            }}
                            onBlur={props.onBlur}
                            option={stakeholderAnimal}
                            optionKey="name"
                            t={t}
                            placeholder={t("DEONAR_ANIMAL_TYPE")}
                          />
                        </div>
                      )}
                    />
                  </LabelFieldPair>
                </div>
                <ReceiverField control={control} setData={setData} data={data} label={t("Stakeholder Name")} name={"stakeholderName"} />
                <LicenseNumberField control={control} data={data} setData={setData} disabled={false} style={null} name={"licenseNumber"} />
              </div>

              <div className="bmc-card-row">
                <EmailInputField control={control} setData={setData} data={data} label={t("Stakeholder Email")} name={"email"} />
                <ReferenceNumberField control={control} data={data} setData={setData} label={t("Registration Number")} name={"referenceNumber"} />
                <PhoneNumberField control={control} setData={setData} data={data} label={t("Mobile Number")} name={"mobileNumber"} />
                <PinCodeField control={control} setData={setData} data={data} label={t("Pin Code")} name={"pincode"} />
              </div>
              <div className="bmc-card-row">
                <ReceiverField control={control} setData={setData} data={data} label={t("Address-1")} name={"address1"} />
                <ReceiverField control={control} setData={setData} data={data} label={t("Address-2")} name={"address2"} />
              </div>

              <div className="bmc-card-row">
                <div style={{ float: "right", paddingBottom: "1rem", textAlign: "end" }}>
                  <button
                    type="submit"
                    className="bmc-card-button"
                    style={{
                      marginRight: "1rem",
                      borderBottom: "3px solid black",
                    }}
                  >
                    {t("Deonar_SAVE")}
                  </button>
                </div>
              </div>
            </CustomModal>
          </div>
        </form>
        {toast && <Toast error={toast.key === "error"} label={t(toast.action)} onClose={() => setToast(null)} style={{ maxWidth: "670px" }} />}
      </div>
    </React.Fragment>
  );
};

export default Stakeholder;
