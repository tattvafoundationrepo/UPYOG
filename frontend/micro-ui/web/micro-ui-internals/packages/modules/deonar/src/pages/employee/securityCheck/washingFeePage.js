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

const WashingFeePage = () => {
  const { t } = useTranslation();
  const [data, setData] = useState();
  const [washingStatus, setWashingStatus] = useState(false);
  const [toast, setToast] = useState(null);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [tableData, setTableData] = useState([]);
  const [isLoading, setIsLoading] = useState(false);
  const [disabledRows, setDisabledRows] = useState([]);
  const [responseData, setResponseData] = useState();
  const queryClient = useQueryClient(); 

  const { fetchWashingCollectionDetails, saveWashingDetails } = useCollectionPoint({});

  const { data: washingDetailsData } = fetchWashingCollectionDetails();

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
    formState: { errors, isValid },
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
    setWashingStatus(status);
    setIsModalOpen(true);
  };
  const toggleModal = () => {
    setIsModalOpen(!isModalOpen);
  };


  const onSubmit = async (formData) => {
  setIsLoading(true);

  const payload = {
    vehicleWashing: {
      vehicleNumber: formData.vehicleNumber,
      vehicleType: formData.vehicleType.value,
      IN: true,
      OUT: false,
    },
  };

  saveWashingDetails.mutate(payload, {
    
    onSuccess: () => {
      queryClient.invalidateQueries("WashingDetails");
      showToast("success", t("DEONAR_WASHING_DATA_UPDATED_SUCCESSFULY"));
      reset();
      setIsLoading(false);
      setIsModalOpen(false);
    },
    onError: () => {
      showToast("error", t("DEONAR_WASHING_DATA_NOT_UPDATED_SUCCESSFULY"));
      setIsLoading(false);
    },
  });
};


  const handleDeparture = async (formData) => {
    setIsLoading(true);
    let washingTime;
    if (typeof formData.washingTime === "string") {
      const [hours, minutes, seconds] = formData.washingTime.split(":");
      washingTime = new Date().setHours(hours, minutes, seconds.split(".")[0], seconds.split(".")[1] || 0);
    } else {
      washingTime = formData.washingTime;
    }
    const payload = {
      vehicleWashing: {
        vehicleNumber: formData.vehicleNumber,
        vehicleType: formData.vehicleType,
        washingTime: washingTime,
        IN: false,
        OUT: true,
      },
    };


    saveWashingDetails(payload, {
      onSuccess: (data) => {
        queryClient.invalidateQueries("WashingDetails");
        showToast("success", t("DEONAR_WASHING_DATA_UPDATED_SUCCESSFULY"));
        reset();
        setResponseData(data);
        setDisabledRows((prev) => [...prev, formData.vehicleNumber]);
        setIsLoading(false);
      },
      onError: () => {
        showToast("error", t("DEONAR_WASHING_DATA_NOT_UPDATED_SUCCESSFULY"));
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

 
  

  return (
    <React.Fragment>
      <div className="bmc-card-full">
        <form onSubmit={handleSubmit(onSubmit)}>
          <MainFormHeader title={"DEONAR_WASHING"} />
          <div className="bmc-row-card-header">
            <div className="bmc-card-row" style={{overflowY:"auto", maxHeight:"511px"}}>
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
          <CustomModal isOpen={isModalOpen} onClose={toggleModal}>
            <div className="bmc-card-row">
              <div className="bmc-col2-card">
                <VehicleTypeDropdownField control={control} data={data} setData={setData} t={t} />
              </div>
              <div className="bmc-col2-card">
                <VehicleNumberField control={control} setData={setData} data={data} t={t} />
              </div>
            </div>

            <div style={{ float: "right", paddingBottom: "1rem", textAlign: "end" }}>
              <button
                className="bmc-card-button"
                style={{ borderBottom: "3px solid black", outline: "none" }}
                type="submit"
                onClick={handleSubmit(onSubmit)}
              >
                {t("Submit")}
              </button>
            </div>
          </CustomModal>
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
