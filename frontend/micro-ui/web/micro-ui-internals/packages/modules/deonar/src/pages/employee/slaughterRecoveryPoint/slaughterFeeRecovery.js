import React, { useEffect, useMemo, useState } from "react";
import { useTranslation } from "react-i18next";
import { Controller, useForm } from "react-hook-form";
import MainFormHeader from "../commonFormFields/formMainHeading";
import NumberOfAliveAnimalsField from "../commonFormFields/numberOfAliveAnimals";
import AnimalTokenNumberField from "../commonFormFields/animalTokenNumber";
import HealthStatDropdownField from "../commonFormFields/healthStatDropdown";
import ShopkeeperNameField from "../commonFormFields/shopkeeperName";
import HelkariNameField from "../commonFormFields/helkariName";
import DawanwalaNameField from "../commonFormFields/dawanwalaName";
import SlaughterFeeAmountField from "../commonFormFields/slaughterFeeAmount";
import PaymentModeField from "../commonFormFields/paymentMode";
import ReferenceNumberField from "../commonFormFields/referenceNumber";
import SubmitPrintButtonFields from "../commonFormFields/submitPrintBtn";
import NumberOfAnimalsField from "../commonFormFields/numberOfAnimals";
import useSubmitForm from "../../../hooks/useSubmitForm";
import { PARKING_ENDPOINT } from "../../../constants/apiEndpoints";
import { slaughterSession, slaughterType, slaughterUnit } from "../../../constants/dummyData";
import CustomTable from "../commonFormFields/customTable";
import { columns, generateTokenNumber } from "../collectionPoint/utils";
import useDeonarCommon from "@upyog/digit-ui-libraries/src/hooks/deonar/useCommonDeonar";
import CustomModal from "../commonFormFields/customModal";
import MultiColumnDropdown from "../commonFormFields/multiColumnDropdown";
import SubmitButtonField from "../commonFormFields/submitBtn";

const SlaughterFeeRecoveryPage = () => {
  const { t } = useTranslation();
  const [data, setData] = useState({});
  const [slaughterTypeOptions, setSlaughterTypeOptions] = useState([]);
  const [slaughterUnitOptions, setSlaughterUnitOptions] = useState([]);
  const [slaughterSessionOptions, setSlaughterSessionOptions] = useState([]);
  const [selectedUUID, setSelectedUUID] = useState();
  const [animalCount, setAnimalCount] = useState([]);
  const [totalRecords, setTotalRecords] = useState(0);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [helkariDropdown, setHelkariDropdown] = useState([]);
  const [helkariTable, setHelkariTable] = useState([]);
  const [selectedOption, setSelectedOption] = useState([]);

  const {
    control,
    setValue,
    handleSubmit,
    getValues,
    formState: { errors, isValid },
  } = useForm({
    defaultValues: {
      slaughterType: {},
      slaughterUnit: {},
      slaughterSession: {},
      shopkeeperName: {},
      dawanwalaName: {},
      helkariName: {},
      numberOfAnimals: 0,
      animalTokenNumber: 0,
      slaughterFeeAmount: 0,
      paymentMode: {},
      referenceNumber: 0,
    },
    mode: "onChange",
  });

  useEffect(() => {
    setSlaughterTypeOptions(slaughterType);
    setSlaughterUnitOptions(slaughterUnit);
    setSlaughterSessionOptions(slaughterSession);
  }, []);

  const { submitForm, isSubmitting, response, error } = useSubmitForm(PARKING_ENDPOINT);

  const { fetchEntryFeeDetailsbyUUID, fetchDeonarCommon, searchDeonarCommon, saveStablingDetails } = useDeonarCommon();

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

  const { data: fetchedData, isLoading } = fetchEntryFeeDetailsbyUUID({ Tradable: true });

  const helkariData = useFetchOptions("helkari");

  useEffect(() => {
    if (fetchedData) {
      setAnimalCount(fetchedData.SecurityCheckDetails);
      setTotalRecords();
    }
  }, [fetchedData]);

  useEffect(() => {
    if (fetchedData && fetchedData.SecurityCheckDetails) {
      const securityCheckDetails = fetchedData.SecurityCheckDetails;
      const mappedAnimalCount = securityCheckDetails.flatMap((detail) =>
        detail.animalDetails.map((animal) => ({
          arrivalId: detail.entryUnitId,
          animalTypeId: animal.animalTypeId,
          animalType: animal.animalType,
          count: animal.count,
        }))
      );
      setHelkariTable(mappedAnimalCount);
    }
  }, [fetchedData]);

  const filteredHelkaritable = helkariTable.filter((animal) => animal.arrivalId === selectedUUID);

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
      if (JSON.stringify(combinedData) !== JSON.stringify(helkariDropdown)) {
        setHelkariDropdown(combinedData);
      }
    }
  }, [helkariData, helkariDropdown]);

  const handleUUIDClick = (entryUnitId) => {
    setSelectedUUID(entryUnitId);
    setIsModalOpen(!isModalOpen);
  };

  const toggleModal = () => {
    setIsModalOpen(!isModalOpen);
  };

  const isVisibleColumns = [
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
      accessor: "animalDetails",
      Header: "Assign Helkari",
      Cell: ({ row }) => {
        const animalType = row.original?.animalType?.toLowerCase();
        const filteredhelkariDropdown = helkariDropdown.filter((helkari) => helkari.animalTypes.toLowerCase().includes(animalType));
        return (
          <MultiColumnDropdown
            options={filteredhelkariDropdown}
            selected={selectedOption[row.index] || []}
            onSelect={(e, selected) => handleSelect(row.index, selected)}
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
        );
      },
    },
  ];

  const applicableHelkari = useMemo(() => {
    const allAnimalTypesInTable = helkariTable.map((row) => row.animalType.toLowerCase());
    return helkariDropdown.filter((helkari) =>
      helkari.animalTypes
        .toLowerCase()
        .split(", ")
        .some((animalType) => allAnimalTypesInTable.includes(animalType))
    );
  }, [helkariTable, helkariDropdown]);
  const onSubmit = async (formData) => {
    try {
      const result = await submitForm(formData);
      console.log("Form successfully submitted:", result);
      alert("Form submission successful !");
    } catch (error) {
      console.error("Error submitting form:", error);
      alert("Form submission failed !");
    }
  };

  return (
    <React.Fragment>
      <div className="bmc-card-full">
        <form onSubmit={handleSubmit(onSubmit)}>
          <MainFormHeader title={"DEONAR_SLAUGHTER_RECOVERY"} />
          <div className="bmc-row-card-header">
            <CustomTable
              t={t}
              columns={columns(handleUUIDClick)}
              data={animalCount}
              manualPagination={false}
              tableClassName={"deonar-custom-scroll"}
              totalRecords={totalRecords}
              sortBy={["entryUnitId:asc"]}
              autoSort={false}
              isLoadingRows={isLoading}

              //  fileName="YourCustomFileName"
            />
            <CustomModal isOpen={isModalOpen} onClose={toggleModal} title={"Slaughter Details"} style={{ width: "100%" }}>
              {selectedUUID && (
                <div style={{ paddingBottom: "20px", display: "flex", gap: "12px", alignItems: "center" }}>
                  <h3 style={{ fontWeight: "600", fontSize: "20px" }}>Active Arrival UUID: </h3>
                  <span style={{ fontWeight: "bold", backgroundColor: "rgb(204, 204, 204)", borderRadius: "10px", padding: "8px", fontSize: "22px" }}>
                    {selectedUUID}
                  </span>
                </div>
              )}
              <div className="bmc-row-card-header" style={{ marginBottom: "40px" }}>
                <div style={{ display: "flex", gap: "20px", justifyContent: "space-between" }}></div>
              </div>
              <div className="bmc-card-row">
                <div className="bmc-row-card-header" style={{ overflowY: "auto", maxHeight: "300px" }}>
                  {/* {isMobileView && data.map((data, index) => <TableCard data={data} key={index} fields={fields} onUUIDClick={handleUUIDClick} />)} */}
                  <CustomTable
                    t={t}
                    columns={isVisibleColumns}
                    manualPagination={false}
                    data={filteredHelkaritable}
                    totalRecords={totalRecords}
                    tableClassName={"ebe-custom-scroll"}
                    autoSort={false}
                    isLoadingRows={isLoading}
                  />
                </div>
              </div>

              <div className="bmc-row-card-header">
            <div className="bmc-card-row">
              <HealthStatDropdownField
                name="slaughterType"
                label="DEONAR_SLAUGHTER_TYPE"
                control={control}
                data={data}
                setData={setData}
                options={slaughterTypeOptions}
              />
              <HealthStatDropdownField
                name="slaughterUnit"
                label="DEONAR_SLAUGHTER_UNIT"
                control={control}
                data={data}
                setData={setData}
                options={slaughterUnitOptions}
              />
              <HealthStatDropdownField
                name="slaughterSession"
                label="DEONAR_SLAUGHTER_SESSION"
                control={control}
                data={data}
                setData={setData}
                options={slaughterSessionOptions}
              />
              <SubmitPrintButtonFields />
            </div>
          </div>
              <SubmitButtonField control={control} />

              {/* <Fragment>
                  <div className="bmc-row-card-header" style={{ marginBottom: "40px" }}>
                    <SubmitPrintButtonFields />
                  </div>
                </Fragment> */}
            </CustomModal>
          </div>
        </form>
      </div>
    </React.Fragment>
  );
};

export default SlaughterFeeRecoveryPage;
