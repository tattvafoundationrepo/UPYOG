import React, { useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import MainFormHeader from "../commonFormFields/formMainHeading";
import CustomTable from "../commonFormFields/customTable";
import TableCard from "../commonFormFields/tableCard";
import { CardLabel, LabelFieldPair, Toast, Dropdown, CheckBox } from "@upyog/digit-ui-react-components";
import { Controller, useForm } from "react-hook-form";
import useCollectionPoint from "@upyog/digit-ui-libraries/src/hooks/deonar/useCollectionPoint";
import SubmitButtonField from "../commonFormFields/submitBtn";
import useDeonarCommon from "@upyog/digit-ui-libraries/src/hooks/deonar/useCommonDeonar";
import { generateTokenNumber } from "../collectionPoint/utils";

const Slaughtering = () => {
  const { t } = useTranslation();
  const [slaughterList, setSlaughterList] = useState([]);
  const [slaughterAnimalListData, setSlaughterAnimalListData] = useState([]);
  const [selectedUUID, setSelectedUUID] = useState(null);
  const [toast, setToast] = useState(null);
  const [totalRecords, setTotalRecords] = useState(0);
  const [isMobileView, setIsMobileView] = useState(window.innerWidth < 768);
  const [selectedRows, setSelectedRows] = useState([]);
  const [slaughtering, setSlaughtering] = useState([
    { code: "1", name: "YES" },
    { code: "2", name: "NO" },
  ]);

  const {
    control,
    setValue,
    handleSubmit,
    getValues,
    reset,
    formState: { errors },
  } = useForm({
    defaultValues: {
      slaughter: "",
      slaughterType: {},
      slaughterUnit: {},
    },
  });

  const { fetchSlaughterCollectionList } = useCollectionPoint({});
  const { data: SlaughterListData } = fetchSlaughterCollectionList({});
  const { saveSlaughterListData } = useDeonarCommon();

  const handleUUIDClick = (ddReference) => {
    setSelectedUUID(ddReference);
    const selectedSlaughter = slaughterList.find((item) => item.ddReference === ddReference);
    if (selectedSlaughter) {
      setValue("arrivalId", selectedSlaughter.arrivalId);
      setValue("animalType", selectedSlaughter.animalAssignmentDetailsList?.animalType || "");
      setValue("token", selectedSlaughter.animalAssignmentDetailsList?.token || "");
      setValue("slaughter", selectedSlaughter.animalAssignmentDetailsList?.slaughtering || "");

      const updatedAnimalList =
        selectedSlaughter.animalAssignmentDetailsList?.map((item) => ({
          ...item,
          animalTokenNumber: generateTokenNumber(item.animalType, item.token),
        })) || [];

      setSlaughterAnimalListData(updatedAnimalList || []);
    }
  };

  const saveSlaughterData = saveSlaughterListData();

  const showToast = (type, message, duration = 5000) => {
    setToast({ key: type, action: message });
    setTimeout(() => {
      setToast(null);
    }, duration);
  };

  const onSubmit = async (formData) => {
    const selectedSlaughter = slaughterList.find((item) => item.ddReference === selectedUUID);

    // const slaughterDetails = slaughterAnimalListData.map((item) => ({
    //   animalTypeId: item.animalTypeId,
    //   token: item.token,
    // }))

    const slaughterDetails = slaughterAnimalListData.filter((item) =>
      selectedRows.includes(item.animalTokenNumber)
    ).map((item) => ({
      animalTypeId: item.animalTypeId,
      token: item.token,
    }));

    if (selectedSlaughter) {
      const slaughteringValue = formData?.slaughtering?.name === "YES" ? true : false;
      // const selectedTokens = slaughterAnimalListData
      //   .filter((animal) => selectedRows.includes(animal.animalTokenNumber))
      //   .map((animal) => animal.token);

      const payload = {
        slaughterDetails: {
          arrivalId: selectedSlaughter?.arrivalId,
          ddReference: selectedSlaughter?.ddReference,
          shopkeeperName: selectedSlaughter?.shopkeeperName,
          licenceNumber: selectedSlaughter?.licenceNumber,
          // animalTypeId: slaughterAnimalListData?.[0]?.animalTypeId,
          // token: selectedTokens,
          details:slaughterDetails,
          slaughtering: slaughteringValue,
        },
      };

      saveSlaughterData.mutate(payload, {
        onSuccess: (data) => {
          reset({
            slaughter: "",
            slaughterType: {},
            slaughterUnit: {},
          });
          setSelectedRows([]);
          showToast("success", t("DEONAR_SLAUGHTERING_DATA_SUBMITTED_SUCCESSFULY"));
        },
        onError: (error) => {
          showToast("error", t("DEONAR_SLAUGHTERING_DATA_NOT_SUBMITTED_SUCCESSFULY"));
        },
      });
    }
  };

  const handleRowCheckboxChange = (ddReference) => {
    setSelectedRows((prevSelectedRows) => {
      if (prevSelectedRows.includes(ddReference)) {
        return prevSelectedRows.filter((row) => row !== ddReference);
      } else {
        return [...prevSelectedRows, ddReference];
      }
    });
  };

  const currentRows = slaughterAnimalListData;

  useEffect(() => {
    if (SlaughterListData) {
      setSlaughterList(SlaughterListData.slaughterLists || []);
      setTotalRecords(SlaughterListData.slaughterLists.length);
    }
  }, [SlaughterListData]);

  useEffect(() => {
    if (toast) {
      const timer = setTimeout(() => setToast(null), 3000);
      return () => clearTimeout(timer);
    }
  }, [toast]);

  const Tablecolumns = [
    {
      Header: t("ID"),
      accessor: "id",
      Cell: ({ row }) => row.index + 1,
      isVisible: false,
    },
    {
      Header: t("Deonar_DD_Reference"),
      accessor: "ddReference",
      Cell: ({ row }) => (
        <span onClick={() => handleUUIDClick(row.original.ddReference)} style={{ cursor: "pointer", color: "blue" }}>
          {row.original.ddReference}
        </span>
      ),
    },
    {
      Header: t("DEONAR_SHOPKEEPER_NAME"),
      accessor: "shopkeeperName",
    },
    {
      Header: t("DEONAR_LICENSE_NUMBER"),
      accessor: "licenceNumber",
    },
    {
      Header: t("DEONAR_ARRIVAL_UUID"),
      accessor: "arrivalId",
    },
  ];

  const tableColumnsAnimal = [
    {
      Header: (
        <CheckBox
          onChange={(e) => {
            if (e.target.checked) {
              setSelectedRows(currentRows.map((row) => row.animalTokenNumber));
            } else {
              setSelectedRows([]);
            }
          }}
          checked={currentRows.length > 0 && currentRows.length === selectedRows.length}
        />
      ),
      accessor: "checkbox",
      Cell: ({ row }) => {
        const isChecked = selectedRows.includes(row.original.animalTokenNumber);
        return <CheckBox checked={isChecked} onChange={() => handleRowCheckboxChange(row.original.animalTokenNumber)} />;
      },
      disableSortBy: true,
    },

    {
      Header: t("Animal Type"),
      accessor: "animalType",
      disableSortBy: true,
    },
    {
      Header: t("Animal Token"),
      accessor: "animalTokenNumber",
      disableSortBy: true,
    },
  ];

  return (
    <React.Fragment>
      <div className="bmc-card-full">
        <form onSubmit={handleSubmit(onSubmit)}>
          <MainFormHeader title={t("DEONAR_SLAUGHTERING")} />
          <div className="bmc-card-row">
            <div className="bmc-row-card-header">
              {isMobileView &&
                slaughterList.map((data, index) => (
                  <TableCard
                    data={data}
                    key={index}
                    fields={[
                      { key: "ddReference", label: t("Deonar_DD_Reference"), isClickable: true },
                      { key: "shopkeeperName", label: t("DEONAR_TRADER_NAME") },
                      { key: "licenceNumber", label: t("DEONAR_LICENSE_NUMBER") },
                      { key: "arrivalId", label: t("DEONAR_ARRIVAL_UUID") },
                    ]}
                    onUUIDClick={handleUUIDClick}
                  />
                ))}
              <CustomTable
                t={t}
                columns={Tablecolumns}
                data={slaughterList}
                manualPagination={false}
                tableClassName={"deonar-scrollable-table"}
                totalRecords={totalRecords}
                autoSort={false}
              />
            </div>
          </div>

          {selectedUUID && (
            <div className="bmc-row-card-header">
              <div style={{ paddingBottom: "20px", display: "flex", gap: "12px", alignItems: "center" }}>
                <h3 style={{ fontWeight: "600", fontSize: "20px" }}>{t("ACTIVE_DD_REFERENCENO")}: </h3>
                <span
                  style={{
                    fontWeight: "bold",
                    backgroundColor: "rgb(204, 204, 204)",
                    borderRadius: "10px",
                    padding: "8px",
                    fontSize: "22px",
                  }}
                >
                  {selectedUUID}
                </span>
              </div>
              <div className="bmc-card-row">
                <CustomTable
                  t={t}
                  columns={tableColumnsAnimal}
                  data={slaughterAnimalListData}
                  manualPagination={false}
                  tableClassName={"deonar-scrollable-table"}
                  getCellProps={() => ({
                    style: {
                      fontSize: "14px",
                    },
                  })}
                />
              </div>
              <div className="bmc-row-card">
                <div className="bmc-col3-card">
                  <LabelFieldPair>
                    <CardLabel className="bmc-label">{t("DEONAR_Slaughter")}</CardLabel>
                    <Controller
                      control={control}
                      name="slaughtering"
                      render={(props) => (
                        <Dropdown
                          value={props.value}
                          selected={props.value}
                          select={(value) => {
                            props.onChange(value);
                          }}
                          onBlur={props.onBlur}
                          optionKey="name"
                          option={slaughtering}
                          placeholder={t("DEONAR_Slaughter")}
                          t={t}
                        />
                      )}
                    />
                  </LabelFieldPair>
                </div>
                <SubmitButtonField control={control} />
              </div>
            </div>
          )}
        </form>
      </div>
      {toast && (
        <Toast
          error={toast.key === t("error")}
          label={t(toast.key === t("success") ? t("DEONAR_SLAUGHTERING_DATA_SUBMITTED_SUCCESSFULY") : toast.action)}
          onClose={() => setToast(null)}
          style={{ maxWidth: "670px" }}
        />
      )}
    </React.Fragment>
  );
};

export default Slaughtering;
