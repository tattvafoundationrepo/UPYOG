import React, { useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { useForm, Controller } from "react-hook-form";
import SearchButtonField from "../commonFormFields/searchBtn";
import SubmitPrintButtonFields from "../commonFormFields/submitPrintBtn";
import ReceiverField from "../commonFormFields/receiverName";
import MultiColumnDropdown from "../commonFormFields/multiColumnDropdown";
import CustomTable from "../commonFormFields/customTable";
import useDeonarCommon from "@upyog/digit-ui-libraries/src/hooks/deonar/useCommonDeonar";
import MainFormHeader from "../commonFormFields/formMainHeading";
import { Label, Toast, Dropdown, LabelFieldPair, CardLabel } from "@upyog/digit-ui-react-components";
import useCollectionPoint from "@upyog/digit-ui-libraries/src/hooks/deonar/useCollectionPoint";
import CustomModal from "../commonFormFields/customModal";

const GatePass = () => {
  const { t } = useTranslation();
  const [data, setData] = useState({});
  const [shopKeeper, setShopKeeper] = useState([]);
  const [globalShopkeeper, setGlobalShopkeeper] = useState(null);
  const [tableGateData, setTableGateData] = useState([]);
  const [referenceNumberMessages, setReferenceNumberMessages] = useState(null);
  const [toast, setToast] = useState(null);
  const [parkingDetails, setParkingDetails] = useState([]);
  const [selectedVehicleNumber, setSelectedVehicleNumber] = useState(null);
  const [isModalOpen, setIsModalOpen] = useState(false);

  const { fetchGatePassSearchData, saveGatePassData, searchDeonarCommon } = useDeonarCommon();
  const { fetchParkingCollectionDetails } = useCollectionPoint({});

  const { data: parkingDetailsData } = fetchParkingCollectionDetails();

  const gatePassSearchData = fetchGatePassSearchData();
  const gatePassSaveData = saveGatePassData();

  const {
    control,
    setValue,
    handleSubmit,
    getValues,
    formState: { errors, isValid },
  } = useForm({
    defaultValues: {
      vehicleNumber: "",
      receiverName: "",
      receiverContact: "",
      typeOfAnimal: "",
      weight: 0,
      ddReference: "",
      referenceNumber: "",
    },
    mode: "onChange",
  });

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

  const shopkeeperData = useFetchOptions("shopkeeper");

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
  const toggleModal = () => {
    setIsModalOpen(!isModalOpen);
  };

  const handleSearch = async () => {
    const payload = {
      criteria: {
        shopkeeper: globalShopkeeper?.label || "",
        licenceNumber: globalShopkeeper?.licenceNumber || "",
      },
    };
    gatePassSearchData.mutate(payload, {
      onSuccess: (data) => {
        setTableGateData(data);
      },
      onError: (error) => {
        console.error("Failed to fetch data", error);
      },
    });
  };

  // const handleSearch = async () => {
  //   setIsModalOpen(true);
  // };

  const tableData = tableGateData?.gatePassDetails
    ? tableGateData?.gatePassDetails.map((item) => ({
        ddReference: item.ddReference,
        typeOfAnimal: item.animalType,
        carcas: item.carcassKenaecount,
        kena: item.carcassKenaecount,
        total: (item.carcassKenaecount || 0) + (item.carcassKenaecount || 0),
      }))
    : [];

  const showToast = (type, message, duration = 5000) => {
    setToast({ key: type, action: message });
    setTimeout(() => {
      setToast(null);
    }, duration);
  };

  useEffect(() => {
    if (parkingDetailsData) {
      setParkingDetails(parkingDetailsData.VehicleParkedCheckDetails || []);
    }
  }, [parkingDetailsData]);

  const onSubmit = (formData) => {
    const animalDetails = tableData.map((item) => ({
      ddReference: item.ddReference,
      animalType: item.typeOfAnimal,
      carcasscount: item.carcas || 0,
      kenaecount: item.kena || 0,
    }));

    const total = animalDetails.reduce((acc, item) => acc + item.carcasscount + item.kenaecount, 0);

    const payload = {
      gatePassDetails: {
        vehicleNumber: formData?.vehicleNumber || "",
        receiverName: formData?.receiverName || "",
        receiverContact: formData?.receiverContact || "",
        shopkeeper: globalShopkeeper?.value || "",
        animalDetails: animalDetails,
        total: total,
      },
    };

    gatePassSaveData.mutate(payload, {
      onSuccess: (data) => {
        const referenceNumber = data?.SavedGatePassDetails?.gatePassReference;
        setReferenceNumberMessages(referenceNumber);
        showToast("success", t("DEONAR_GATE_PASS_GENERATED_SUCCESSFULY"));
        if (data?.gatePassDetails) {
          setTableGateData(data.gatePassDetails);
        }
      },
      onError: (error) => {
        showToast("error", t("DEONAR_GATE_PASS_GENERATED_NOT_SUCCESSFULY"));
        console.error("Failed to fetch data", error);
      },
    });
  };

  const grandTotal = tableData.reduce((acc, item) => acc + item.total, 0);

  const handleGlobalShopkeeperSelect = (selected) => {
    const selectedShopkeeper = selected[0] || null;
    setGlobalShopkeeper(selectedShopkeeper);
  };

  const tableColumns = [
    {
      Header: t("Type of Animal"),
      accessor: "typeOfAnimal",
    },
    {
      Header: t("Carcas"),
      accessor: "carcas",
    },
    {
      Header: t("Kena"),
      accessor: "kena",
    },
    {
      Header: t("Total"),
      accessor: "total",
    },
  ];

  const tableBalanceColumns = [
    {
      Header: t("Type of Service"),
      accessor: "typeOfAnimal",
    },
    {
      Header: t("Total"),
      accessor: "carcas",
    },
    {
      Header: t("Paid"),
      accessor: "kena",
    },
    {
      Header: t("Balance"),
      accessor: "total",
    },
  ];

  return (
    <React.Fragment>
      <div className="bmc-card-full">
        <MainFormHeader title={t("DEONAR_GATE_PASS")} />
        <form onSubmit={handleSubmit(onSubmit)}>
          <div className="bmc-row-card-header">
            <div className="bmc-card-row">
              <div className="bmc-col3-card">
                <Label>{t("DEONAR_SHOPKEEPER NAME")}</Label>
                <MultiColumnDropdown
                  options={shopKeeper}
                  selected={globalShopkeeper ? [globalShopkeeper] : []}
                  onSelect={(e, selected) => handleGlobalShopkeeperSelect(selected)}
                  defaultLabel="Select Shopkeeper"
                  displayKeys={["label", "licenceNumber", "mobileNumber"]}
                  optionsKey="value"
                  placeholder={t("Select the Shopkeeper")}
                  autoCloseOnSelect={true}
                  showColumnHeaders={true}
                  headerMappings={{
                    label: t("Name"),
                    licenceNumber: t("License"),
                    mobileNumber: t("Mobile Number"),
                  }}
                />
              </div>

              <SearchButtonField onSearch={handleSearch} disabled={!globalShopkeeper} />
              <CustomModal isOpen={isModalOpen} onClose={toggleModal} title={t("DEONAR_GATE_PASS_DETAILS")}>
                <CustomTable
                  t={t}
                  columns={tableBalanceColumns}
                  tableClassName="deonar-scrollable-table"
                  data={tableData}
                  disableSort={true}
                  autoSort={true}
                  manualPagination={false}
                  showSearch={false}
                  showTotalRecords={false}
                  showPagination={false}
                  getCellProps={(cellInfo) => ({
                    style: { fontSize: "16px" },
                  })}
                />
                <div className="bmc-card-row">
                  <SubmitPrintButtonFields isValid={isValid} />
                </div>
              </CustomModal>
            </div>
          </div>
          <div className="bmc-row-card-header">
            <div className="bmc-card-row">
              <div className="bmc-col3-card">
                <LabelFieldPair>
                  <CardLabel className="bmc-label">{t("DEONAR_vehicleNumber")}</CardLabel>
                  <Controller
                    control={control}
                    name="vehicleNumber"
                    render={(props) => (
                      <Dropdown
                        option={parkingDetails.map((item) => item.vehicleNumber)}
                        select={(selectedVehicle) => {
                          setSelectedVehicleNumber(selectedVehicle);
                          props.onChange(selectedVehicle);
                        }}
                        selected={props.value}
                        t={t}
                        onBlur={props.onBlur}
                        placeholder={t("DEONAR_vehicleNumber")}
                      />
                    )}
                  />
                </LabelFieldPair>
              </div>
              <ReceiverField control={control} setData={setData} data={data} label={t("DEONAR_RECEIVERNAME")} name={"receiverName"} />
              <ReceiverField control={control} setData={setData} data={data} label={t("DEONAR_RECEIVERCONTACT")} name={"receiverContact"} />
            </div>
          </div>
          <div id="gatePassContent">
            <div className="bmc-row-card-header">
              <div
                style={{
                  border: "3px dotted #ccc",
                  padding: "20px",
                  borderRadius: "10px",
                  margin: "20px auto",
                  backgroundColor: "#fff",
                  boxShadow: "0px 0px 10px rgba(0, 0, 0, 0.1)",
                }}
              >
                <div style={{ borderBottom: "2px solid #ddd", paddingBottom: "10px", marginBottom: "20px" }}>
                  <h2 style={{ textAlign: "center", fontSize: "24px", margin: "0" }}>{t("DEONAR")}</h2>
                  <p style={{ textAlign: "center", fontSize: "16px", margin: "10px 0" }}>
                    {t("DEONAR_DATE")}: {new Date().toLocaleDateString()}
                  </p>
                  <p style={{ textAlign: "center", fontSize: "16px", margin: "0" }}>
                    {t("DEONAR_REFERENCENUMBER")}: {referenceNumberMessages || "N/A"}
                  </p>
                </div>

                <div className="bmc-card-row" style={{ borderBottom: "2px solid #ddd", paddingBottom: "10px", marginBottom: "20px" }}>
                  <div className="bmc-col2-card">
                    <Label>
                      {t("DEONAR_RECEIVERNAME")}: {data.receiverName || "N/A"}
                    </Label>
                    <Label>
                      {t("DEONAR_RECEIVERCONTACT")}: {data.receiverContact || "N/A"}
                    </Label>
                  </div>
                  <div className="bmc-col2-card">
                    <Label>
                      {t("DEONAR_SHOPKEEPER")}: {globalShopkeeper?.label ? globalShopkeeper?.label : "N/A"}
                    </Label>
                    <Label>
                      {t("DEONAR_VEHICLENUMBER")}: {selectedVehicleNumber || "N/A"}
                    </Label>
                  </div>
                </div>

                <CustomTable
                  t={t}
                  columns={tableColumns}
                  tableClassName="deonar-scrollable-table"
                  data={tableData}
                  disableSort={true}
                  autoSort={true}
                  manualPagination={false}
                  showSearch={false}
                  showTotalRecords={false}
                  showPagination={true}
                  getCellProps={(cellInfo) => ({
                    style: { fontSize: "16px" },
                  })}
                />

                <div style={{ borderTop: "2px solid #ddd", paddingTop: "10px", marginTop: "20px" }}>
                  <p style={{ fontWeight: "bold", fontSize: "18px" }}>
                    {t("Total")}: {grandTotal || "0"}
                  </p>
                </div>
              </div>
            </div>
          </div>

          <SubmitPrintButtonFields isValid={isValid} />
        </form>
      </div>
      {toast && (
        <Toast
          error={toast.key === t("error")}
          label={t(toast.key === t("success") ? t("DEONAR_GATE_PASS_GENERATED_SUCCESSFULY") : toast.action)}
          onClose={() => setToast(null)}
          style={{ maxWidth: "670px" }}
        />
      )}
    </React.Fragment>
  );
};

export default GatePass;
