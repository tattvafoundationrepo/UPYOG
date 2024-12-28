import React, { useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { useForm, Controller } from "react-hook-form";
import SearchButtonField from "../commonFormFields/searchBtn";
import MultiColumnDropdown from "../commonFormFields/multiColumnDropdown";
import CustomTable from "../commonFormFields/customTable";
import useDeonarCommon from "@upyog/digit-ui-libraries/src/hooks/deonar/useCommonDeonar";
import MainFormHeader from "../commonFormFields/formMainHeading";
import { Toast, Dropdown, LabelFieldPair, CardLabel } from "@upyog/digit-ui-react-components";

const tableData = [
  {
    typeOfAnimal: "Entry Fee",
    carcas: 100,
    kena: 50,
    total: 50,
  },
  {
    typeOfAnimal: "Stabling Fee",
    carcas: 200,
    kena: 150,
    total: 50,
  },
];

const GatePassIssue = () => {
  const { t } = useTranslation();
  const [data, setData] = useState({});
  const [shopKeeper, setShopKeeper] = useState([]);
  const [globalShopkeeper, setGlobalShopkeeper] = useState(null);
  const [tableGateData, setTableGateData] = useState([]);
  const [toast, setToast] = useState(null);
  const [options, setOptions] = useState([]);
  const [selectedStakeholder, setSelectedStakeholder] = useState(null);

  const { fetchGatePassSearchData, saveGatePassData, searchDeonarCommon, fetchDeonarCommon } = useDeonarCommon();

  const gatePassSearchData = fetchGatePassSearchData();
  const gatePassSaveData = saveGatePassData();

  const {
    control,
    setValue,
    handleSubmit,
    getValues,
    formState: { errors, isValid },
  } = useForm({
    defaultValues: {},
    mode: "onChange",
  });

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

  const handleStakeholderSelect = (selected) => {
    setSelectedStakeholder(selected);
  };

  useEffect(() => {
    setOptions(stakeholderData.map((item) => item.name || []));
  }, []);

  const filteredStakeholderData = stakeholderData.filter((item) => item.value === 1 || item.value === 2);

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

  // const tableData = tableGateData?.gatePassDetails
  //   ? tableGateData?.gatePassDetails.map((item) => ({
  //       ddReference: item.ddReference,
  //       typeOfAnimal: item.animalType,
  //       carcas: item.carcassKenaecount,
  //       kena: item.carcassKenaecount,
  //       total: (item.carcassKenaecount || 0) + (item.carcassKenaecount || 0),
  //     }))
  //   : [];

  const showToast = (type, message, duration = 5000) => {
    setToast({ key: type, action: message });
    setTimeout(() => {
      setToast(null);
    }, duration);
  };

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

  const handleGlobalShopkeeperSelect = (selected) => {
    const selectedShopkeeper = selected[0] || null;
    setGlobalShopkeeper(selectedShopkeeper);
  };

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
                <LabelFieldPair>
                  <CardLabel className="bmc-label">{t("DEONAR_STACKHOLDER_TYPE")}</CardLabel>
                  <Controller
                    control={control}
                    name="stackholderName"
                    rules={{ required: t("CORE_COMMON_REQUIRED_ERRMSG") }}
                    render={(props) => (
                      <div>
                        <Dropdown
                          name="stackholderName"
                          selected={props.value}
                          select={(value) => {
                            props.onChange(value);
                            const newData = {
                              ...data,
                              stackholderName: value,
                            };
                            setData(newData);
                            handleStakeholderSelect(value);
                          }}
                          onBlur={props.onBlur}
                          option={filteredStakeholderData}
                          optionKey="name"
                          t={t}
                          placeholder={t("DEONAR_STACKHOLDER_NAME")}
                        />
                      </div>
                    )}
                  />
                </LabelFieldPair>
              </div>
              <div className="bmc-col3-card">
                <CardLabel>{t("DEONAR_NAME")}</CardLabel>
                <MultiColumnDropdown
                  options={shopkeeperData}
                  selected={globalShopkeeper ? [globalShopkeeper] : []}
                  onSelect={(e, selected) => handleGlobalShopkeeperSelect(selected)}
                  defaultLabel="Select Shopkeeper"
                  displayKeys={["name", "licenceNumber", "mobileNumber"]}
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
            </div>
          </div>
          <div className="bmc-row-card-header">
            <div className="bmc-card-row">
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
            </div>
          </div>
          <div style={{ float: "right", paddingBottom: "1rem", textAlign: "end" }}>
            <button
              type="button"
              className="bmc-card-button"
              style={{
                marginRight: "1rem",
                borderBottom: "3px solid black",
              }}
            >
              {t("Deonar_PRINT")}
            </button>
          </div>
        </form>
      </div>
      {/* {toast && (
        <Toast
          error={toast.key === t("error")}
          label={t(toast.key === t("success") ? t("DEONAR_GATE_PASS_GENERATED_SUCCESSFULY") : toast.action)}
          onClose={() => setToast(null)}
          style={{ maxWidth: "670px" }}
        />
      )} */}
    </React.Fragment>
  );
};

export default GatePassIssue;
