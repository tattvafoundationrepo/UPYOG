import React from "react";
import CustomTable from "../commonFormFields/customTable";
import { useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import MainFormHeader from "../commonFormFields/formMainHeading";
import CustomModal from "../commonFormFields/customModal";
import useCollectionPoint from "@upyog/digit-ui-libraries/src/hooks/deonar/useCollectionPoint";

const RemovalReport = () => {
  const { t } = useTranslation();
  const [totalRecords, setTotalRecords] = useState(0);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [getApplicationData, setGetApplicationData] = useState([]);
  const [selectedUUID, setSelectedUUID] = useState(null);
  const { fetchRemovalReport } = useCollectionPoint({});
  const { data: RemovalList } = fetchRemovalReport({}, { executeOnLoad: true });
  const [isLoading, setIsLoading] = useState(false);

  const handleStakeholderClick = (row) => {
    setSelectedUUID(row.original.UUID);
    setIsModalOpen(true);
  };
  const handleAddEmployee = () => {
    setIsModalOpen(!isModalOpen);
  };

  const visibleColumns = [
    {
      Header: t("Stakeholder's Name"),
      accessor: "ownerName",
      Cell: ({ row }) => (
        <span onClick={() => handleStakeholderClick(row)} style={{ cursor: "pointer", textDecoration: "underline" }}>
          {t(row.original.ownerName) || "N/A"}
        </span>
      ),
    },
    {
      Header: t("Stakeholder's Type"),
      accessor: "stakeholderTypeName",
      Cell: ({ row }) => t(row.original.stakeholderTypeName) || "N/A",
    },
    {
      Header: t("Arrival UUID"),
      accessor: "UUID",
      Cell: ({ row }) => t(row.original.UUID) || "N/A",
    },
    {
      Header: t("DD Reference"),
      accessor: "reference",
      Cell: ({ row }) => t(row.original.reference) || "N/A",
    },
    {
      Header: t("License Number"),
      accessor: "licenseNumber",
      Cell: ({ row }) => t(row.original.licenseNumber) || "N/A",
    },
    {
      Header: t("Mobile Number"),
      accessor: "mobileNumber",
      Cell: ({ row }) => t(row.original.mobileNumber) || "N/A",
    },
  ];
  const isVisibleColumns2 = [
    {
      Header: t("Animal Type"),
      accessor: "animalType",
      Cell: ({ row }) => t(row.original.animalType) || "N/A",
    },
    {
      Header: t("Animal Token"),
      accessor: "animalToken",
      Cell: ({ row }) => t(row.original.animalToken) || "N/A",
    },
    {
      Header: t("Removal Type"),
      accessor: "removalType",
      Cell: ({ row }) => t(row.original.removalType) || "N/A",
    },
    {
      Header: t("Removal Date"),
      accessor: "date",
      Cell: ({ row }) => t(row.original.date) || "N/A",
    },
    {
      Header: t("Removal Time"),
      accessor: "time",
      Cell: ({ row }) => t(row.original.time) || "N/A",
    },
  ];
  useEffect(() => {
    if (RemovalList) {
      const mainTableData = RemovalList.SecurityCheckDetails.map((item) => ({
        ownerName: item.shopkeepername || "N/A",
        UUID: item.entryUnitId || "N/A",
        reference: item.ddreference || "N/A",
        licenseNumber: item.licenceNumber || "N/A",
        mobileNumber: item.mobilenumber || "N/A",
        stakeholderTypeName: item.stakeholderTypeName,
      }));

      const detailedData = RemovalList.SecurityCheckDetails.flatMap((item) =>
        item.animalDetails.map((animalDetail) => ({
          ownerName: item.shopkeepername || "N/A",
          UUID: item.entryUnitId || "N/A",
          stakeholderTypeName: item.stakeholderTypeName,
          removalType: animalDetail.removaltype || "N/A",
          animalType: animalDetail.animalType || "N/A",
          reference: item.ddreference || "N/A",
          licenseNumber: item.licenceNumber || "N/A",
          animalToken: animalDetail.token || "N/A",
          date: typeof item.dateOfRemoval === "string" ? item.dateOfRemoval : "Invalid Date",
          time: (item.timeOfRemoval || "N/A").replace(/:/g, "."),
          mobileNumber: item.mobilenumber || "N/A",
        }))
      );

      setGetApplicationData({ mainTableData, detailedData });
    }
  }, [RemovalList]);

  const handleCloseModal = () => {
    setIsModalOpen(false);
    setSelectedUUID(null);
  };

  return (
    <React.Fragment>
      <MainFormHeader title={"DEONAR_REMOVAL_REPORT"} />
      <div className="bmc-row-card-header">
        <div className="bmc-card-row" style={{ overflowY: "auto", maxHeight: "511px" }}>
          <CustomTable
            t={t}
            pageSizeLimit={10}
            searchPlaceholder={t("Search")}
            columns={visibleColumns}
            data={getApplicationData?.mainTableData || []}
            manualPagination={false}
            // tableClassName={"ebe-custom-scroll"}
            showSearch={true}
            showText={true}
            isLoading={isLoading}
          />
        </div>
      </div>
      {isModalOpen && (
        <div className="bmc-card-row">
          <CustomModal
            isOpen={isModalOpen}
            onClose={handleCloseModal}
            selectedUUID={selectedUUID}
            style={{ width: "100%" }}
            tableClassName={"ebe-custom-scroll"}
          >
            <div className="bmc-card-row" style={{ overflowY: "auto", maxHeight: "511px" }}>
              <CustomTable
                t={t}
                columns={isVisibleColumns2}
                manualPagination={false}
                data={getApplicationData?.detailedData.filter((item) => item.UUID === selectedUUID) || []}
                totalRecords={totalRecords}
                searchPlaceholder={t("Search")}
                // tableClassName={"deonar-scrollable-table"}
                autoSort={false}
                isLoadingRows={false}
              />
            </div>
          </CustomModal>
        </div>
      )}
    </React.Fragment>
  );
};

export default RemovalReport;
