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
  const [isLoading, setIsLoading] = useState(false)

  const handleStakeholderClick = (row) => {
    setSelectedUUID(row.original.UUID);
    setIsModalOpen(true);
  };
  const handleAddEmployee = () => {
    setIsModalOpen(!isModalOpen);
  };

  const visibleColumns = [
    {
      Header: t("Shopkeeper's Name"),
      accessor: "ownerName",
      Cell: ({ row }) => (
        <span
          onClick={() => handleStakeholderClick(row)}
          style={{ cursor: "pointer", color: "blue", textDecoration: "underline" }}
        >
          {t(row.original.ownerName) || "N/A"}
        </span>
      ),
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

  ]
  useEffect(() => {
    if (RemovalList) {
      const transformedData = RemovalList.SecurityCheckDetails.map((item) => ({
        ownerName: item.shopkeepername || "N/A",
        UUID: item.entryUnitId || "N/A",
        removalType: item.animalDetails?.[0]?.removaltype || "N/A",
        animalType: item.animalDetails?.[0]?.animalType || "N/A",
        reference: item.ddreference || "N/A",
        licenseNumber: item.licenceNumber || "N/A",
        animalToken: item.animalDetails?.[0]?.token || "N/A",
        date: typeof item.dateOfRemoval === "string" ? item.dateOfRemoval : "Invalid Date",
        time: (item.timeOfRemoval || "N/A").replace(/:/g, "."),
        mobileNumber: item.mobilenumber || "N/A",
      }));
      setGetApplicationData(transformedData);
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
        <CustomTable
          t={t}
          pageSizeLimit={10}
          columns={visibleColumns}
          data={getApplicationData || []}
          manualPagination={false}
          totalRecords={getApplicationData.length}
          tableClassName={"ebe-custom-scroll"}
          showSearch={true}
          showText={true}
          isLoading={isLoading}
        />
      </div>
      {isModalOpen && (
        <div className="bmc-card-row">
          
            <CustomModal isOpen={isModalOpen} onClose={handleCloseModal} selectedUUID={selectedUUID} style={{ width: "100%" }} tableClassName={"ebe-custom-scroll"}>
              <h3>{t("Assigned Stakeholder Details")}</h3>
              <p>{t("Selected UUID")}: {selectedUUID || "N/A"}</p>
              <div className="bmc-card-row">

                <CustomTable
                  t={t}
                  columns={isVisibleColumns2}
                  manualPagination={false}
                  data={getApplicationData || []}
                  totalRecords={totalRecords}
                  tableClassName={"deonar-scrollable-table"}
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

