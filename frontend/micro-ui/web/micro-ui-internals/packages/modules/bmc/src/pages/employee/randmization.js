import React, { useState, useEffect } from "react";
import { useTranslation } from "react-i18next";
import RandomizeApplications from "../../components/RandomizeApplications";
import Title from "../../components/title";
import CustomTable from "../../components/CustomTable";

const RandmizationPage = ({ Comment, isLoading }) => {
  const { t } = useTranslation();
  const actions = ["RANDOMIZE"];
  const availableActions = ["RANDOMIZE"];
  const [rowsPerPage, setRowsPerPage] = useState(10);
  const [totalRecords, setTotalRecords] = useState(0);
  const [currentPage, setCurrentPage] = useState(1);
  const [applications, setApplications] = useState(() => {
    const savedApplications = sessionStorage.getItem("applications");
    return savedApplications ? JSON.parse(savedApplications) : [];
  });
  const [selectedAction, setSelectedAction] = useState(availableActions[0]);

  const getVerifierApplications = Digit.Hooks.bmc.useVerifierSchemeDetail();

  const indexOfLastRow = currentPage * rowsPerPage;
  const indexOfFirstRow = indexOfLastRow - rowsPerPage;
  const currentRows = applications.slice(indexOfFirstRow, indexOfLastRow);

  const handleSearchCriteria = (criteria) => {
    getVerifierApplications.mutate(
      {
        schemeId: criteria.schemeID,
        detailID: criteria.detailID,
        type: criteria.type,
        action: availableActions,
        number: Number(criteria.machineNumber),
      },
      {
        onSuccess: (data) => {
          if (data && data.Applications) {
            setApplications(data.Applications);
            setTotalRecords(data.Applications.length);
            sessionStorage.setItem("applications", JSON.stringify(data.Applications));
          } else {
            setApplications([]);
            setTotalRecords(0);
            sessionStorage.setItem("applications", JSON.stringify([]));
          }
        },
        onError: (err) => {
          console.error("Error fetching applications:", err);
        },
      }
    );
  };

  useEffect(() => {
    return () => {
      sessionStorage.removeItem("applications");
    };
  }, []);

  const Tablecolumns = [
    {
      Header: t("Name"),
      accessor: "aadharName",
    },
    {
      Header: t("Application Number"),
      accessor: "applicationNumber",
    },
    {
      Header: t("BMC_WARD_NAME"),
      accessor: "ward",
    },
    {
      Header: t("Gender"),
      accessor: "gender",
    },
    {
      Header: t("Pincode"),
      accessor: "pincode",
    },
  ];

  const filteredData = applications.slice(indexOfFirstRow, indexOfLastRow).map((application) => {
    const applicantDetails = application.ApplicantDetails?.[0];
    return {
      aadharName: applicantDetails?.AadharUser?.aadharName || "N/A",
      applicationNumber: application.applicationNumber || "N/A",
      ward: applicantDetails?.UserOtherDetails?.ward || "N/A",
      gender: applicantDetails?.AadharUser?.gender || "N/A",
      pincode: applicantDetails?.address?.pincode || "N/A",
    };
  });

  return (
    <React.Fragment>
      <Title text={t(`BMC_Application_for_${selectedAction}`)} />
      <div className="bmc-card-full">
        <RandomizeApplications onUpdate={handleSearchCriteria} actions={actions} />
        <div className="bmc-row-card-header" style={{ padding: "0" }}>
          <div className="bmc-card-row">
            <div className="bmc-table-container">
              <CustomTable
                t={t}
                columns={Tablecolumns}
                data={filteredData || []}
                manualPagination={false}
                totalRecords={totalRecords}
                isLoadingRows={isLoading}
                getCellProps={(cellInfo) => ({
                  style: {
                    fontSize: "16px",
                  },
                })}
              />
            </div>
          </div>
        </div>
      </div>
    </React.Fragment>
  );
};
export default RandmizationPage;
