import React, { useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { useForm } from "react-hook-form";
import MainFormHeader from "../commonFormFields/formMainHeading";
import { Toast, Loader } from "@upyog/digit-ui-react-components";
import CustomTable from "../commonFormFields/customTable";
import CustomModal from "../commonFormFields/customModal";
import useCollectionPoint from "@upyog/digit-ui-libraries/src/hooks/deonar/useCollectionPoint";
import VehicleNumberField from "../commonFormFields/vehicleNumber";
import VehicleTypeDropdownField from "../commonFormFields/vehicleTypeDropdown";
import { useQueryClient } from "react-query";
import SubmitButtonField from "../commonFormFields/submitBtn";

const ParkingFeePage = () => {
  const { t } = useTranslation();
  const [data, setData] = useState();
  const [parkingStatus, setParkingStatus] = useState(false);
  const [toast, setToast] = useState(null);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [tableData, setTableData] = useState([]);
  const [isLoading, setIsLoading] = useState(false);
  const [disabledRows, setDisabledRows] = useState([]);
  const [responseData, setResponseData] = useState();
  const queryClient = useQueryClient(); // Initialize queryClient

  const { fetchParkingCollectionDetails, saveParkingDetails } = useCollectionPoint({});

  const { data: parkingDetailsData } = fetchParkingCollectionDetails();

  useEffect(() => {
    if (parkingDetailsData?.VehicleParkedCheckDetails) {
      const vehicleParkedCheckDetails = parkingDetailsData.VehicleParkedCheckDetails.map((detail) => ({
        vehicleType: detail.vehicleType,
        vehicleNumber: detail.vehicleNumber,
        parkingTime: detail.parkingTime,
        parkingDate: detail.parkingDate,
        departureTime: detail.departureTime,
        departureDate: detail.departureDate,
      }));
  
      setTableData(vehicleParkedCheckDetails);
    }
  }, [parkingDetailsData]);
  
  const {
    control,
    handleSubmit,
    reset,
    formState: { errors, isDirty },
  } = useForm({
    defaultValues: {
      vehicleType: "",
      vehicleNumber: "",
    },
    mode: "onChange",
  });

  const openModal = (vehicleData, status) => {
    setData({
      vehicleNumber: vehicleData.vehicleNumber,
      vehicleType: vehicleData.vehicleType,
    });
    setParkingStatus(status);
    setIsModalOpen(true);
  };
  const toggleModal = () => {
    setIsModalOpen(!isModalOpen);
  };

  // const savePrakingData = Digit.Hooks.deonar.useSavePrakingDetail();

  const onSubmit = async (formData) => {
    setIsLoading(true);

    const payload = {
      vehicleParking: {
        vehicleNumber: formData.vehicleNumber,
        vehicleType: formData.vehicleType.value,
        IN: true,
        OUT: false,
      },
    };

    saveParkingDetails.mutate(payload, {
      onSuccess: () => {
        queryClient.invalidateQueries("ParkingDetails");
        // refetch();
        showToast("success", t("DEONAR_PARKING_DATA_UPDATED_SUCCESSFULY"));
        reset();
        setIsLoading(false);
        setIsModalOpen(false);
      },
      onError: () => {
        showToast("error", t("DEONAR_PARKING_DATA_NOT_UPDATED_SUCCESSFULY"));
        setIsLoading(false);
      },
    });
  };

  const handleDeparture = async (formData) => {
    setIsLoading(true);
    let parkingTime;
    if (typeof formData.parkingTime === "string") {
      const [hours, minutes, seconds] = formData.parkingTime.split(":");
      parkingTime = new Date().setHours(hours, minutes, seconds.split(".")[0], seconds.split(".")[1] || 0);
    } else {
      parkingTime = formData.parkingTime;
    }
    const payload = {
      vehicleParking: {
        vehicleNumber: formData.vehicleNumber,
        vehicleType: formData.vehicleType,
        parkingTime: parkingTime,
        IN: false,
        OUT: true,
      },
    };

    console.log("payloadOUT", payload);

    saveParkingDetails(payload, {
      onSuccess: (data) => {
        queryClient.invalidateQueries("ParkingDetails");
        showToast("success", t("DEONAR_PARKING_DATA_UPDATED_SUCCESSFULY"));
        reset();
        setResponseData(data);
        setDisabledRows((prev) => [...prev, formData.vehicleNumber]);
        setIsLoading(false);
      },
      onError: () => {
        showToast("error", t("DEONAR_PARKING_DATA_NOT_UPDATED_SUCCESSFULY"));
        setIsLoading(false);
      },
    });
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
      Header: t("Parking Time"),
      accessor: "parkingTime",
    },
    {
      Header: t("Parking Date"),
      accessor: "parkingDate",
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
        text: t("Add Parking Details"),
        style: { textDecoration: "underline", cursor: "pointer" },
        onClick: "onAddClickFunction",
      },
    ],
  };

 
  

  return (
    <React.Fragment>
      <div className="bmc-card-full">
        <form onSubmit={handleSubmit(onSubmit)}>
          <MainFormHeader title={"DEONAR_PARKING"} />
          <div className="bmc-row-card-header">
            <div className="bmc-card-row">
              {isLoading ? (
                <Loader />
              ) : (
                <CustomTable
                  t={t}
                  columns={Tablecolumns}
                  data={tableData}
                  tableClassName={"deonar-scrollable-table"}
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
          <CustomModal isOpen={isModalOpen} onClose={toggleModal} title={t("Add Parking Details")}>
            <div className="bmc-card-row">
              <div className="bmc-col2-card">
                <VehicleTypeDropdownField control={control} data={data} setData={setData} t={t} />
              </div>
              <div className="bmc-col2-card">
                <VehicleNumberField control={control} setData={setData} data={data} t={t} />
              </div>
            </div>

            {/* <div style={{ float: "right", paddingBottom: "1rem", textAlign: "end" }}>
              <button
                className="bmc-card-button"
                style={{ borderBottom: "3px solid black", outline: "none" }}
                type="submit"
                onClick={handleSubmit(onSubmit)}
              >
                {t("Submit")}
              </button>
            </div> */}
            <SubmitButtonField control={control} disabled={!isDirty} />
          </CustomModal>
        </form>
      </div>
      {toast && (
        <Toast
          error={toast.key === t("error")}
          label={t(toast.key === t("success") ? t("DEONAR_PARKING_DATA_UPDATED_SUCCESSFULY") : toast.action)}
          onClose={() => setToast(null)}
          style={{ maxWidth: "670px" }}
        />
      )}
    </React.Fragment>
  );
};

export default ParkingFeePage;
