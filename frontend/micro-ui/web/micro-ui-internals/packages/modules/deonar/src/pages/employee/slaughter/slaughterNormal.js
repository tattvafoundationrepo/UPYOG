import React, { useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import MainFormHeader from "../commonFormFields/formMainHeading";
import CustomTable from "../commonFormFields/customTable";
import TableCard from "../commonFormFields/tableCard";
import { Toast, CheckBox } from "@upyog/digit-ui-react-components";
import { useForm } from "react-hook-form";
import useCollectionPoint from "@upyog/digit-ui-libraries/src/hooks/deonar/useCollectionPoint";
import SubmitButtonField from "../commonFormFields/submitBtn";
import useDeonarCommon from "@upyog/digit-ui-libraries/src/hooks/deonar/useCommonDeonar";
import { generateTokenNumber } from "../collectionPoint/utils";
import CustomModal from "../commonFormFields/customModal";

const SlaughteringNormal = () => {
  const { t } = useTranslation();
  const [slaughterList, setSlaughterList] = useState([]);
  const [slaughterAnimalListData, setSlaughterAnimalListData] = useState([]);
  const [selectedUUID, setSelectedUUID] = useState(null);
  const [toast, setToast] = useState(null);
  const [totalRecords, setTotalRecords] = useState(0);
  const [isMobileView, setIsMobileView] = useState(window.innerWidth < 768);
  const [selectedRows, setSelectedRows] = useState([]);
  const [isSubmitted, setIsSubmitted] = useState(false);
  const [isModalOpen, setIsModalOpen] = useState(false);

  const { fetchSlaughterCollectionList } = useCollectionPoint({});
  const { data: SlaughterListData } = fetchSlaughterCollectionList({}, { executeOnLoad: true });
  const { saveSlaughterListData } = useDeonarCommon();
  const [animalAssignmentData, setAnimalAssignmentData] = useState("");
  const [isCheckboxChecked, setIsCheckboxChecked] = useState(false);

  const {
    control,
    setValue,
    handleSubmit,
    getValues,
    reset,
    formState: { errors, isDirty },
  } = useForm({
    defaultValues: {
      slaughter: "",
      slaughterType: {},
      slaughterUnit: {},
    },
  });

  const handleUUIDClick = (ddReference, entryUnitId) => {
    setSelectedUUID(entryUnitId);
    setIsModalOpen(!isModalOpen);
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
    if (selectedSlaughter?.animalAssignmentDetailsList) {
      const formattedData = selectedSlaughter.animalAssignmentDetailsList.map((animal) => ({
        animalType: animal.animalType,
        count: animal.token,
        animalTypeId: animal.animalTypeId,
        animalTokenNumber: generateTokenNumber(animal.animalType, animal.token),
      }));
      setAnimalAssignmentData(formattedData);
      setTotalRecords(formattedData.length);
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
    console.log("formData", formData);
    const selectedSlaughter = slaughterList.find((item) => item.ddReference === selectedUUID);
    const slaughterDetails = slaughterAnimalListData
      .filter((item) => selectedRows.includes(item.animalTokenNumber))
      .map((item) => ({
        animalTypeId: item.animalTypeId,
        token: item.token,
      }));

    if (selectedSlaughter) {
      const payload = {
        slaughterDetails: {
          arrivalId: selectedSlaughter?.arrivalId,
          slaughterType: "Normal",
          details: slaughterDetails,
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
          setIsSubmitted(true);
          toggleModal();
        },
        onError: (error) => {
          showToast("error", t("DEONAR_SLAUGHTERING_DATA_NOT_SUBMITTED_SUCCESSFULY"));
        },
      });
    }
  };

  const handleRowCheckboxChange = (animalTokenNumber) => {
    setSelectedRows((prevSelectedRows) => {
      let updatedSelectedRows = [];
      if (prevSelectedRows.includes(animalTokenNumber)) {
        updatedSelectedRows = prevSelectedRows.filter((row) => row !== animalTokenNumber);
      } else {
        updatedSelectedRows = [...prevSelectedRows, animalTokenNumber];
      }

      setIsCheckboxChecked(updatedSelectedRows.length > 0);
      return updatedSelectedRows;
    });
  };

  useEffect(() => {
    if (SlaughterListData) {
      setSlaughterList(SlaughterListData.SlaughterList || []);
      setTotalRecords(SlaughterListData.SlaughterList.length);
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
  const toggleModal = () => {
    setIsModalOpen(!isModalOpen);
  };

  const isVisibleColumns2 = [
    {
      Header: (
        <CheckBox
          onChange={(e) => {
            if (e.target.checked) {
              setSelectedRows(animalAssignmentData.map((row) => row.animalTokenNumber));
            } else {
              setSelectedRows([]);
            }
            setIsCheckboxChecked(e.target.checked);
          }}
          checked={animalAssignmentData.length > 0 && animalAssignmentData.length === selectedRows.length}
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
    },
    {
      Header: t("Animal Token"),
      accessor: "animalTokenNumber",
    },
  ];

  return (
    <React.Fragment>
      <div className="bmc-card-full">
        <form onSubmit={handleSubmit(onSubmit)}>
          <MainFormHeader title={t("DEONAR_SLAUGHTERING_NORMAL")} />
          <div className="bmc-card-row" style={{ overflowY: "auto", maxHeight: "511px" }}>
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

          {isModalOpen && (
            <CustomModal isOpen={isModalOpen} onClose={toggleModal} selectedUUID={selectedUUID} style={{ width: "100%" }}>
              <div className="bmc-card-row">
                <CustomTable
                  t={t}
                  columns={isVisibleColumns2}
                  manualPagination={false}
                  data={animalAssignmentData}
                  totalRecords={totalRecords}
                  tableClassName={"deonar-scrollable-table"}
                  autoSort={false}
                  isLoadingRows={""}
                  getCellProps={() => ({
                    style: {
                      fontSize: "14px",
                    },
                  })}
                />
              </div>
              <SubmitButtonField control={control} disabled={!isCheckboxChecked || isDirty} />
            </CustomModal>
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

export default SlaughteringNormal;
