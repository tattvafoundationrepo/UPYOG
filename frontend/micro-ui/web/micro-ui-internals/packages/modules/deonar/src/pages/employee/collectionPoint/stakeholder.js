import React, { useMemo } from "react";
import CustomTable from "../commonFormFields/customTable";
import useDeonarCommon from "@upyog/digit-ui-libraries/src/hooks/deonar/useCommonDeonar";
import { useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import CustomModal from "../commonFormFields/customModal";
import { CardLabel, LabelFieldPair, Dropdown, DatePicker, Toast } from "@upyog/digit-ui-react-components";
import { Controller, useForm } from "react-hook-form";
import ReceiverField from "../commonFormFields/receiverName";
import LicenseNumberField from "../commonFormFields/licenseNumber";
import ReferenceNumberField from "../commonFormFields/referenceNumber";
import EmailInputField from "../commonFormFields/EmailInputField";
import PhoneNumberField from "../commonFormFields/PhoneNumberField";
import PinCodeField from "../commonFormFields/PinCodeField";
import MainFormHeader from "../commonFormFields/formMainHeading";

const Stakeholder = () => {
  const { t } = useTranslation();
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [getStakeholderDetails, setStakeholderDetails] = useState([]);
  const [toast, setToast] = useState("");
  const [data, setData] = useState({});
  const [selectedOption, setSelectedOption] = useState(null);
  const { saveStakeholderDetails } = useDeonarCommon({});
  const { searchStakeholder } = useDeonarCommon();
  const { data: stakeData } = searchStakeholder();
  const [filteredStakeholderDetails, setFilteredStakeholderDetails] = useState([]);
  const [showPlaceholderMessage, setShowPlaceholderMessage] = useState(true);
  const initialDefaultValues = useMemo(
    () => ({
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
      asigndate: "",
      validtodate: "",
    }),
    []
  );

  const {
    control,
    handleSubmit,
    reset,
    formState: { errors, isValid, dirtyFields, isDirty },
    watch,
  } = useForm({
    defaultValues: initialDefaultValues,
    mode: "onChange",
  });
  const formValues = watch();

  const isFormValid = () => {
    const isCitizen = formValues.traderType?.name === "CITIZEN";

    // Different required fields based on stakeholder type
    const requiredFields = isCitizen
      ? ["stakeholderName", "traderType", "email", "mobileNumber", "pincode", "address1", "address2"]
      : ["stakeholderName", "traderType", "animalType", "licenseNumber", "email", "mobileNumber", "pincode", "address1", "address2"];

    return requiredFields.every((field) => {
      const value = formValues[field];

      // Handle different types of values
      if (Array.isArray(value)) {
        return value.length > 0;
      }
      if (typeof value === "object" && value !== null) {
        return value.value !== undefined;
      }
      return !!value;
    });
  };
  useEffect(() => {
    if (stakeData) {
      console.log(stakeData, "stakeData");
      const StakeholderData = stakeData.StakeholderCheckDetails.map((detail) => ({
        name: detail.stakeholdername || "N/A",
        address: detail.address || "N/A",
        licenceNumber: detail.licencenumber || "N/A",
        animalType: detail.animaltype || "N/A",
        mobileNumber: detail.mobilenumber || "N/A",
        email: detail.email || "N/A",
        stakeholderType: detail.stakeholdertype || "N/A",
        registrationNumber: detail.registrationnumber || "N/A",
        pincode: detail.pincode || "N/A",
        address1: detail.address1 || "N/A",
        address2: detail.address2 || "N/A",
      }));
      setStakeholderDetails(StakeholderData);
      setFilteredStakeholderDetails(StakeholderData);
    }
  }, [stakeData]);

  useEffect(() => {
    if (selectedOption) {
      setShowPlaceholderMessage(false);
      const filteredData = getStakeholderDetails.filter((detail) => detail.stakeholderType.toLowerCase().includes(selectedOption.name.toLowerCase()));
      setFilteredStakeholderDetails(filteredData);
    } else {
      setShowPlaceholderMessage(true);
      setFilteredStakeholderDetails([]);
    }
  }, [selectedOption, getStakeholderDetails]);

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

  console.log(stakeholderData, "stakeholderData");

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
        validfromdate: formData.asigndate || "",
        validtodate: formData.validtodate || "",
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
          <div className="bmc-row-card-header">
            <div className="bmc-card-row" style={{ overflowY: "auto", maxHeight: "511px" }}>
              <CustomTable
                t={t}
                pageSizeLimit={10}
                columns={visibleColumns}
                data={filteredStakeholderDetails || []}
                manualPagination={false}
                totalRecords={getStakeholderDetails?.length}
                onAddEmployeeClick={handleAddEmployee}
                handleRowClick={handleRowClick}
                config={myConfig}
                // sortParams={{}}
                // tableClassName={"ebe-custom-scroll"}
                showSearch={true}
                showDropdown={true}
                dropdownOptions={[
                  {
                    label: t("Stakeholder Name"),
                    selectedOption: selectedOption,
                    setSelectedOption: setSelectedOption,
                    options: stakeholderData,
                    optionKey: "name",
                    placeholder: t("Select Stakeholder"),
                  },
                ]}
                showText={true}
              />
              {showPlaceholderMessage && (
                <div
                  style={{
                    position: "absolute",
                    left: "50%",
                    transform: "translate(-50%, -50%)",
                    textAlign: "center",
                    fontSize: "16px",
                    color: "#333",
                  }}
                >
                  <p>{t("Please select a stakeholder type from the dropdown above.")}</p>
                </div>
              )}
            </div>
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
                            placeholder={t("DEONAR_STACKHOLDER_TYPE")}
                          />
                        </div>
                      )}
                    />
                  </LabelFieldPair>
                </div>
                {data.traderType?.name !== "CITIZEN" && (
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
                )}
                <ReceiverField control={control} setData={setData} data={data} label={t("Stakeholder Name")} name={"stakeholderName"} />

                <EmailInputField control={control} setData={setData} data={data} label={t("Stakeholder Email")} name={"email"} />
              </div>

              <div className="bmc-card-row">
                {data.traderType?.name !== "CITIZEN" && (
                  <LicenseNumberField control={control} data={data} setData={setData} disabled={false} style={null} name={"licenseNumber"} />
                )}
                <ReferenceNumberField control={control} data={data} setData={setData} label={t("Registration Number")} name={"referenceNumber"} />
                <PhoneNumberField control={control} setData={setData} data={data} label={t("Mobile Number")} name={"mobileNumber"} />
                <PinCodeField control={control} setData={setData} data={data} label={t("Pin Code")} name={"pincode"} />
              </div>
              <div className="bmc-card-row">
                {data.traderType?.name !== "CITIZEN" && (
                  <div className="bmc-col3-card">
                    <LabelFieldPair>
                      <CardLabel className="bmc-label">{t("DEONAR_ASSIGN_DATE")}</CardLabel>
                      <Controller
                        control={control}
                        name="asigndate"
                        rules={{ required: t("CORE_COMMON_REQUIRED_ERRMSG") }}
                        render={(props) => (
                          <div>
                            <DatePicker
                              date={props.value}
                              onChange={(e) => {
                                props.onChange(e);
                              }}
                              onBlur={props.onBlur}
                              placeholder={t("DEONAR_ASSIGN_DATE")}
                            />
                          </div>
                        )}
                      />
                    </LabelFieldPair>
                  </div>
                )}
                {data.traderType?.name !== "CITIZEN" && (
                  <div className="bmc-col3-card">
                    <LabelFieldPair>
                      <CardLabel className="bmc-label">{t("DEONAR_VALID_DATE")}</CardLabel>
                      <Controller
                        control={control}
                        name="validtodate"
                        rules={{ required: t("CORE_COMMON_REQUIRED_ERRMSG") }}
                        render={(props) => (
                          <div>
                            <DatePicker
                              date={props.value}
                              onChange={(e) => {
                                props.onChange(e);
                              }}
                              onBlur={props.onBlur}
                              placeholder={t("DEONAR_VALID_DATE")}
                            />
                          </div>
                        )}
                      />
                    </LabelFieldPair>
                  </div>
                )}
                <ReceiverField control={control} setData={setData} data={data} label={t("Address-1")} name={"address1"} />
                <ReceiverField control={control} setData={setData} data={data} label={t("Address-2")} name={"address2"} />
              </div>

              <div className="bmc-card-row">
                <div style={{ float: "right", paddingBottom: "1rem", textAlign: "end" }}>
                  <button
                    type="submit"
                    className="bmc-card-button"
                    disabled={!isFormValid()}
                    style={{
                      marginRight: "1rem",
                      borderBottom: "3px solid black",
                      backgroundColor: !isFormValid() ? "grey" : "#f47738",
                      cursor: !isFormValid() ? "not-allowed" : "pointer",
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
