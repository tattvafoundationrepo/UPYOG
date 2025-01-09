import React, { use, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { useForm } from "react-hook-form";
import MainFormHeader from "../commonFormFields/formMainHeading";
import { Toast, Loader, CheckBox } from "@upyog/digit-ui-react-components";
import CustomTable from "../commonFormFields/customTable";
import CustomModal from "../commonFormFields/customModal";
import useCollectionPoint from "@upyog/digit-ui-libraries/src/hooks/deonar/useCollectionPoint";
import VehicleNumberField from "../commonFormFields/vehicleNumber";
import VehicleTypeDropdownField from "../commonFormFields/vehicleTypeDropdown";
import { useQueryClient } from "react-query";
import SubmitButtonField from "../commonFormFields/submitBtn";

const WashingFeePage = () => {
  const { t } = useTranslation();
  const [data, setData] = useState();
  const [washingStatus, setWashingStatus] = useState(false);
  const [toast, setToast] = useState(null);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [tableData, setTableData] = useState([]);
  const [parkingDetails, setParkingDetails] = useState([]);
  const [isLoading, setIsLoading] = useState(false);
  const [disabledRows, setDisabledRows] = useState([]);
  const [selectedRows, setSelectedRows] = useState([]);
  const [isCheckboxChecked, setIsCheckboxChecked] = useState(false);
  const [responseData, setResponseData] = useState();
  const queryClient = useQueryClient();
  const { fetchWashingCollectionDetails, fetchParkingCollectionDetails, saveWashingDetails } = useCollectionPoint({});

  const { data: washingDetailsData } = fetchWashingCollectionDetails();

  const { data: parkingDetailsData } = fetchParkingCollectionDetails();

  const handleRowCheckboxChange = (vehicleNumber) => {
    setSelectedRows((prevSelectedRows) => {
      let updatedSelectedRows = [];
      if (prevSelectedRows.includes(vehicleNumber)) {
        updatedSelectedRows = prevSelectedRows.filter((row) => row !== vehicleNumber);
      } else {
        updatedSelectedRows = [...prevSelectedRows, vehicleNumber];
      }

      setIsCheckboxChecked(updatedSelectedRows.length > 0);
      return updatedSelectedRows;
    });
  };
  useEffect(() => {
    if (parkingDetailsData?.VehicleParkedCheckDetails) {
      const vehicleParkedCheckDetails = parkingDetailsData.VehicleParkedCheckDetails.map((detail) => ({
        vehicleType: detail.vehicleType,
        vehicleNumber: detail.vehicleNumber,
        vehicleId: detail.vehicleId,
      }));

      setParkingDetails(vehicleParkedCheckDetails);
    }
  }, [parkingDetailsData]);

  useEffect(() => {
    if (washingDetailsData?.VehicleWashedCheckDetails) {
      const vehicleWashedCheckDetails = washingDetailsData.VehicleWashedCheckDetails.map((detail) => ({
        vehicleType: detail.vehicleType,
        vehicleNumber: detail.vehicleNumber,
        washingTime: detail.washingTime,
        washingDate: detail.washingDate,
        departureTime: detail.departureTime,
        departureDate: detail.departureDate,
      }));

      setTableData(vehicleWashedCheckDetails);
    }
  }, [washingDetailsData]);

  const {
    control,
    handleSubmit,
    reset,
    formState: { errors, isValid, isDirty },
  } = useForm({
    defaultValues: {
      vehicleType: "",
      vehicleNumber: "",
      IN: true,
      OUT: false,
    },
    mode: "onChange",
  });

  const openModal = (vehicleData, status) => {
    setData({
      vehicleNumber: vehicleData.vehicleNumber,
      vehicleType: vehicleData.vehicleType,
    });
    setWashingStatus(status);
    setIsModalOpen(true);
  };
  const toggleModal = () => {
    setIsModalOpen(!isModalOpen);
  };
  const onSubmit = async () => {
    const selectedDetails = parkingDetails.filter((detail) => selectedRows.includes(detail.vehicleNumber));
    const payload = {
      vehicleWashing: selectedDetails.map((detail) => ({
        vehicleType: detail.vehicleId,
        vehicleNumber: detail.vehicleNumber,
        IN: true,
        OUT: false,
      })),
    };
    saveWashingDetails.mutate(payload, {
      onSuccess: () => {
        queryClient.invalidateQueries("WashingDetails");
        showToast("success", t("DEONAR_WASHING_DATA_UPDATED_SUCCESSFULY"));
      },
      onError: () => {
        showToast("error", t("DEONAR_WASHING_DATA_NOT_UPDATED_SUCCESSFULY"));
      },
    });

    reset();
    setIsLoading(false);
    setIsModalOpen(false);
    setSelectedRows([]);
    setIsCheckboxChecked(false);
  };

  const Tablecolumns = [
    {
      Header: t("Vehicle Number"),
      accessor: "vehicleNumber",
    },
    {
      Header: t("Vehicle Type"),
      accessor: "vehicleType",
    },
    {
      Header: t("Washing Time"),
      accessor: "washingTime",
    },
    {
      Header: t("Washing Date"),
      accessor: "washingDate",
    },
  ];

  const showToast = (type, message, duration = 5000) => {
    setToast({ key: type, action: message });
    setTimeout(() => {
      setToast(null);
    }, duration);
  };

  const myConfig = {
    elements: [
      {
        type: "p",
        text: t("Add Washing Details"),
        style: { textDecoration: "underline", cursor: "pointer" },
        onClick: "onAddClickFunction",
      },
    ],
  };

  const isVisibleColumns2 = [
    {
      Header: (
        <CheckBox
          onChange={(e) => {
            if (e.target.checked) {
              setSelectedRows(parkingDetails.map((row) => row.vehicleNumber));
            } else {
              setSelectedRows([]);
            }
            setIsCheckboxChecked(e.target.checked);
          }}
          checked={parkingDetails.length > 0 && parkingDetails.length === selectedRows.length}
        />
      ),
      accessor: "checkbox",
      Cell: ({ row }) => {
        const isChecked = selectedRows.includes(row.original.vehicleNumber);
        return <CheckBox checked={isChecked} onChange={() => handleRowCheckboxChange(row.original.vehicleNumber)} />;
      },
      disableSortBy: true,
    },
    {
      Header: t("Vehicle Number"),
      accessor: "vehicleNumber",
    },
    {
      Header: t("Vehicle Type"),
      accessor: "vehicleType",
    },
  ];
  return (
    <React.Fragment>
      <div className="bmc-card-full">
        <form onSubmit={handleSubmit(onSubmit)}>
          <MainFormHeader title={"DEONAR_WASHING"} />
          <div className="bmc-row-card-header">
            <div className="bmc-card-row" style={{ overflowY: "auto", maxHeight: "511px" }}>
              {isLoading ? (
                <Loader />
              ) : (
                <CustomTable
                  t={t}
                  columns={Tablecolumns}
                  data={tableData}
                  searchPlaceholder={t("Search")}
                  // tableClassName={"deonar-scrollable-table"}
                  disableSort={false}
                  autoSort={false}
                  manualPagination={false}
                  onAddEmployeeClick={openModal}
                  config={myConfig}
                  isLoadingRows={isLoading}
                  getCellProps={(cellInfo) => {
                    return {
                      style: {
                        fontSize: "16px",
                      },
                    };
                  }}
                />
              )}
            </div>
          </div>
          {isModalOpen && (
            <CustomModal isOpen={isModalOpen} onClose={toggleModal} style={{ width: "100%" }} title={t("Washing Details")}>
              <div className="bmc-card-row" style={{ overflowY: "auto", maxHeight: "511px" }}>
                <CustomTable
                  t={t}
                  columns={isVisibleColumns2}
                  manualPagination={false}
                  data={parkingDetails}
                  searchPlaceholder={t("Search")}
                  // totalRecords={totalRecords}
                  // tableClassName={"deonar-scrollable-table"}
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
          label={t(toast.key === t("success") ? t("DEONAR_WASHING_DATA_UPDATED_SUCCESSFULY") : toast.action)}
          onClose={() => setToast(null)}
          style={{ maxWidth: "670px" }}
        />
      )}
    </React.Fragment>
  );
};

export default WashingFeePage;
